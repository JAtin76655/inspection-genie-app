package com.summonelec.inspectiongenie.Inspection

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.summonelec.inspectiongenie.DB.DatabaseConnection
import com.summonelec.inspectiongenie.Helper.BitmapUtils
import com.summonelec.inspectiongenie.Helper.ProgressAlert
import com.summonelec.inspectiongenie.Helper.SharedHelper
import com.summonelec.inspectiongenie.Helper.URIPathHelper
import com.summonelec.inspectiongenie.R
import com.summonelec.inspectiongenie.databinding.ActivityPackingSlipBinding
import com.summonelec.inspectiongenie.utilities.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class PackingSlipActivity : AppCompatActivity() {
    lateinit var binding: ActivityPackingSlipBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri : Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var str_iv_package: String? = null
    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackingSlipBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressAlert = ProgressAlert(this)
        binding.packageSlip.setOnClickListener(View.OnClickListener {
            showImageChooseAlert()
        })
        binding.btnNxt.setOnClickListener(View.OnClickListener {
            if (str_iv_package.equals(""))
            {
                Toast.makeText(this@PackingSlipActivity, "Please select Image", Toast.LENGTH_SHORT).show()
            }
            else
            {
                saveDataToSharedPreferences()
                insertDataIntoDatabase()
            }
        })
    }
    private fun saveDataToSharedPreferences() {
        val sharedPref = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString("str_iv_package", str_iv_package)
        editor.apply()
    }
    private fun showImageChooseAlert() {
        val dialog = Dialog(this@PackingSlipActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_choose_image)
        val tvCamera = dialog.findViewById<View>(R.id.tv_camera) as TextView
        val tvGallery = dialog.findViewById<View>(R.id.tv_gallery) as TextView
        val tvCancel = dialog.findViewById<View>(R.id.tv_cancel) as TextView
        tvCamera.setOnClickListener {
            dialog.dismiss()
            performCaptureImage()
        }
        tvGallery.setOnClickListener {
            dialog.dismiss()
            pickImageFromGallery()
        }
        tvCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){

        //ivWeight?.setImageURI(imageUri)
        try {
            val stream = contentResolver.openInputStream(imageUri)
            bitmap = BitmapFactory.decodeStream(stream)
            // bitmap = BitmapUtils.resamplePic(requireContext(), imageUri.path)
            val imagefilepath: String? = BitmapUtils.saveImage(this@PackingSlipActivity, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> "+imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            binding.packageSlip.setImageBitmap(bitmap)
            uploadImage(destination!!)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun createImageUri(): Uri? {
        // Retrieve the folder name from SharedPreferences
        val folderName = getSharedPreferences("MySharedPref", MODE_PRIVATE)
            .getString("folder_name", "default_folder_name") ?: "default_folder_name"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault()).format(Date())
        val imageFileName = "PNG_" + timeStamp + "_.png"
        val image = File(applicationContext.filesDir,imageFileName)
        return FileProvider.getUriForFile(applicationContext,"com.summonelec.inspectiongenie.fileProvider",image)
    }
    private fun performCaptureImage() {

        imageUri = createImageUri()!!
        contract.launch(imageUri)

    }
    var requestPermissionLaunchersotreage =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
                val photoPickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                photoPickerIntent.type = "image/*"
                galleryActivityResultLauncher.launch(photoPickerIntent)
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

    private fun pickImageFromGallery() {
        if (ContextCompat.checkSelfPermission(this@PackingSlipActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissionLaunchersotreage.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        }
        else
        {

            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            galleryActivityResultLauncher.launch(photoPickerIntent)

        }
    }



    private var galleryActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult())
    {
            result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {

            val data = result.data
            val selectedImage: Uri? = Objects.requireNonNull(data)?.data
            var imageStream: InputStream? = null
            try {
                imageStream = selectedImage?.let { contentResolver.openInputStream(it) }
                bitmap = BitmapFactory.decodeStream(imageStream)
                val uriPathHelper = URIPathHelper()
                val filePath = uriPathHelper.getPath(this@PackingSlipActivity, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                binding.packageSlip.setImageBitmap(bitmap)
                uploadImage(destination!!)

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }


        }
    }

    private fun uploadImage(file_image:File) {
        val inspectionId = SharedHelper.getKey(this@PackingSlipActivity, "inspection_id")
 val filepath = "inspections/$inspectionId/${file_image.name}"
val trans = TransferUtility.builder().context(applicationContext).s3Client(s3Client).build()
val observer: TransferObserver = trans.upload(Constants.BUCKET_NAME,filepath, file_image,CannedAccessControlList.PublicRead)//manual storage permission
        var s3FileUrl = ""
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    Log.d("msg","success")
                    s3FileUrl = "https://"+Constants.BUCKET_NAME+".s3.amazonaws.com/"+filepath
                    str_iv_package = s3FileUrl

                    // Now you can use the URL to access the uploaded file
                    progressAlert.dismissAlert()

                    Log.e("path",s3FileUrl)
                } else if (state == TransferState.FAILED) {
                    Log.d("msg","fail")
                    progressAlert.dismissAlert()
                }
            }
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {

                if(bytesCurrent == bytesTotal){
                    //imageView!!.setImageResource(R.drawable.upload_image_with_round)
                }
            }
            override fun onError(id: Int, ex: Exception) {
                Log.d("error",ex.toString())
                progressAlert.dismissAlert()
            }
        })

    }
    fun insertDataIntoDatabase()
    {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    var statement = connection.createStatement()
                    val inspection_id = SharedHelper.getKey(this@PackingSlipActivity, "inspection_id")
                    val insertQuery = "INSERT INTO package_slip (package_slip,inspection_id) VALUES ('"+str_iv_package+"','"+inspection_id+"')"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery)
                    Log.e("insert query status","Data inserted successfully!")
                    runOnUiThread {
                        progressAlert.dismissAlert()
                        val intent = Intent(this@PackingSlipActivity, ConditionLineItemActivity::class.java)
                        startActivity(intent)
                    }

                } catch (e: SQLException) {
                    e.printStackTrace()
                }

                it.close() // Don't forget to close the connection when done
            }

        }

    }
}