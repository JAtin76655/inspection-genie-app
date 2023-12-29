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
import com.summonelec.inspectiongenie.databinding.ActivityPackageBinding
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

class PackageActivity : AppCompatActivity() {
    lateinit var binding: ActivityPackageBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri : Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    var REQUEST_CHECK_SETTINGS = 0x1
    private var isiv_top = false
    private var isiv_Bottom = false
    private var isiv_left = false
    private var isiv_right = false
    private var isiv_front = false
    private var isiv_back = false
    private var file_iv_top: File? = null
    private var file_iv_Bottom: File? = null
    private var file_iv_left: File? = null
    private var file_iv_right: File? = null
    private var file_iv_front: File? = null
    private var file_iv_back: File? = null
    private var str_iv_top: String? = null
    private var str_iv_Bottom: String? = null
    private var str_iv_left: String? = null
    private var str_iv_right: String? = null
    private var str_iv_front: String? = null
    private var str_iv_back: String? = null
    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressAlert = ProgressAlert(this)
        binding.ivTopView.setOnClickListener(View.OnClickListener {
            isiv_top =true
            isiv_Bottom = false
            isiv_left = false
            isiv_right = false
            isiv_front = false
            isiv_back = false
            showImageChooseAlert()
        })
        binding.ivBottomView.setOnClickListener(View.OnClickListener {
            isiv_top =false
            isiv_Bottom = true
            isiv_left = false
            isiv_right = false
            isiv_front = false
            isiv_back = false
            showImageChooseAlert()
        })
        binding.ivLeftView.setOnClickListener(View.OnClickListener {
            isiv_top =false
            isiv_Bottom = false
            isiv_left = true
            isiv_right = false
            isiv_front = false
            isiv_back = false
            showImageChooseAlert()
        })
        binding.ivRightView.setOnClickListener(View.OnClickListener {
            isiv_top =false
            isiv_Bottom = false
            isiv_left = false
            isiv_right = true
            isiv_front = false
            isiv_back = false
            showImageChooseAlert()
        })
        binding.ivFrontView.setOnClickListener(View.OnClickListener {
            isiv_top =false
            isiv_Bottom = false
            isiv_left = false
            isiv_right = false
            isiv_front = true
            isiv_back = false
            showImageChooseAlert()
        })
        binding.ivBackview.setOnClickListener(View.OnClickListener {
            isiv_top =false
            isiv_Bottom = false
            isiv_left = false
            isiv_right = false
            isiv_front = false
            isiv_back = true
            showImageChooseAlert()
        })
        binding.submitNext.setOnClickListener(View.OnClickListener {
            if (str_iv_top.equals(""))
            {
                Toast.makeText(this@PackageActivity, "Please select Top Image", Toast.LENGTH_SHORT).show()
            }
            else if (str_iv_Bottom.equals(""))
            {
                Toast.makeText(this@PackageActivity, "Please select Bottom Image", Toast.LENGTH_SHORT).show()
            }
            else if (str_iv_left.equals(""))
            {
                Toast.makeText(this@PackageActivity, "Please select Left Image", Toast.LENGTH_SHORT).show()
            }
            else if (str_iv_right.equals(""))
            {
                Toast.makeText(this@PackageActivity, "Please select Right Image", Toast.LENGTH_SHORT).show()
            }
            else if (str_iv_front.equals(""))
            {
                Toast.makeText(this@PackageActivity, "Please select Front Image", Toast.LENGTH_SHORT).show()
            }
            else if (str_iv_back.equals(""))
            {
                Toast.makeText(this@PackageActivity, "Please select Back Image", Toast.LENGTH_SHORT).show()
            }
            else
            {
                insertDataIntoDatabase()
            }

        })
    }

    private fun showImageChooseAlert() {
        val dialog = Dialog(this@PackageActivity)
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
            val imagefilepath: String? = BitmapUtils.saveImage(this@PackageActivity, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> "+imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            if (isiv_top) {
                file_iv_top = destination
                binding.ivTopView.setImageBitmap(bitmap)
                uploadImage(file_iv_top!!)
            }
            else if(isiv_Bottom)
            {
                file_iv_Bottom = destination
                binding.ivBottomView.setImageBitmap(bitmap)
                uploadImage(file_iv_Bottom!!)
            }
            else if(isiv_left)
            {
                file_iv_left = destination
                binding.ivLeftView.setImageBitmap(bitmap)
                uploadImage(file_iv_left!!)
            }
            else if(isiv_right)
            {
                file_iv_right = destination
                binding.ivRightView.setImageBitmap(bitmap)
                uploadImage(file_iv_right!!)
            }
            else if(isiv_front)
            {
                file_iv_front = destination
                binding.ivFrontView.setImageBitmap(bitmap)
                uploadImage(file_iv_front!!)
            }
            else if(isiv_back)
            {
                file_iv_back = destination
                binding.ivBackview.setImageBitmap(bitmap)
                uploadImage(file_iv_back!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        if (ContextCompat.checkSelfPermission(this@PackageActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                val filePath = uriPathHelper.getPath(this@PackageActivity, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                if (isiv_top) {
                    file_iv_top = destination
                    binding.ivTopView.setImageBitmap(bitmap)
                    uploadImage(file_iv_top!!)
                }
                else if(isiv_Bottom)
                {
                    file_iv_Bottom = destination
                    binding.ivBottomView.setImageBitmap(bitmap)
                    uploadImage(file_iv_Bottom!!)
                }
                else if(isiv_left)
                {
                    file_iv_left = destination
                    binding.ivLeftView.setImageBitmap(bitmap)
                    uploadImage(file_iv_left!!)
                }
                else if(isiv_right)
                {
                    file_iv_right = destination
                    binding.ivRightView.setImageBitmap(bitmap)
                    uploadImage(file_iv_right!!)
                }
                else if(isiv_front)
                {
                    file_iv_front = destination
                    binding.ivFrontView.setImageBitmap(bitmap)
                    uploadImage(file_iv_front!!)
                }
                else if(isiv_back)
                {
                    file_iv_back = destination
                    binding.ivBackview.setImageBitmap(bitmap)
                    uploadImage(file_iv_back!!)
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }


        }
    }

    private fun createImageUri(): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault()).format(Date())
        val imageFileName = "PNG_" + timeStamp + "_.png"
        val image = File(applicationContext.filesDir,imageFileName)
        return FileProvider.getUriForFile(applicationContext,"com.summonelec.inspectiongenie.fileProvider",image)
    }
    private fun uploadImage(file_image:File) {
        progressAlert.showAlert()
        val inspectionId = SharedHelper.getKey(this@PackageActivity, "inspection_id")
        val folderName = if (!inspectionId.isNullOrEmpty()) "inspections/$inspectionId/" else "inspections/"
        val filepath = "$folderName${file_image.name}"
        val trans = TransferUtility.builder().context(applicationContext).s3Client(s3Client).build()
        val observer: TransferObserver = trans.upload(Constants.BUCKET_NAME,filepath, file_image,CannedAccessControlList.PublicRead)//manual storage permission
        var s3FileUrl = ""
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    Log.d("msg","success")
                     s3FileUrl = "https://"+Constants.BUCKET_NAME+".s3.amazonaws.com/"+filepath
                    //https://imagestorage.s3.amazonaws.com/IMG_20230830_134324.jpg
                    if (isiv_top) {

                        str_iv_top = s3FileUrl
                    }
                    else if(isiv_Bottom)
                    {

                        str_iv_Bottom = s3FileUrl
                    }
                    else if(isiv_left)
                    {

                        str_iv_left = s3FileUrl
                    }
                    else if(isiv_right)
                    {

                        str_iv_right = s3FileUrl
                    }
                    else if(isiv_front)
                    {

                        str_iv_front = s3FileUrl
                    }
                    else if(isiv_back)
                    {

                        str_iv_back = s3FileUrl
                    }

                    saveDataToSharedPreferences()
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
    private fun saveDataToSharedPreferences() {
        val sharedPref = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString("str_iv_top", str_iv_top)
        editor.putString("str_iv_Bottom", str_iv_Bottom)
        editor.putString("str_iv_left", str_iv_left)
        editor.putString("str_iv_right", str_iv_right)
        editor.putString("str_iv_front", str_iv_front)
        editor.putString("str_iv_back", str_iv_back)
        editor.apply()
    }
    fun insertDataIntoDatabase()
    {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    val inspection_id = SharedHelper.getKey(this@PackageActivity, "inspection_id")
                    var statement = connection.createStatement()
                    val insertQuery = "INSERT INTO package_receive (top_image, bottom_image,left_image,right_image,front_image,back_image,inspection_id) VALUES ('"+str_iv_top+"', '"+str_iv_Bottom+"','"+str_iv_left+"','"+str_iv_right+"','"+str_iv_front+"','"+str_iv_back+"','"+inspection_id+"')"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery)
                    Log.e("insert query status","Data inserted successfully!")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            val intent = Intent(this@PackageActivity, PackingSlipActivity::class.java)
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