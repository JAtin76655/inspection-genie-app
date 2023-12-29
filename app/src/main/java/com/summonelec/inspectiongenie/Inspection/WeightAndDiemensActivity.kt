package com.summonelec.inspectiongenie.Inspection

import android.Manifest
import android.app.Dialog
import android.content.Intent
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
import com.summonelec.inspectiongenie.databinding.ActivityWeightAndDiemensBinding
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

class WeightAndDiemensActivity : AppCompatActivity() {
    lateinit var binding: ActivityWeightAndDiemensBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri : Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    private var isiv_scale = false
    private var isiv_measure = false
    private var isiv_height = false
    private var isiv_width = false
    private var file_iv_scale: File? = null
    private var file_iv_measure: File? = null
    private var file_height: File? = null
    private var file_width: File? = null
    private var str_iv_scale: String? = null
    private var str_iv_measure: String? = null
    private var str_height: String? = null
    private var str_width: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeightAndDiemensBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressAlert = ProgressAlert(this)
        binding.ivWeight.setOnClickListener(View.OnClickListener {
            isiv_measure = false
            isiv_scale = true
            isiv_height = false
            isiv_width = false
            showImageChooseAlert()
        })
        binding.ivMeasurement.setOnClickListener(View.OnClickListener {
            isiv_measure = true
            isiv_scale = false
            isiv_height = false
            isiv_width = false
            showImageChooseAlert()
        })
        binding.ivHeight.setOnClickListener(View.OnClickListener {
            isiv_measure = false
            isiv_scale = false
            isiv_height = true
            isiv_width = false
            showImageChooseAlert()
        })
        binding.ivWidth.setOnClickListener(View.OnClickListener {
            isiv_measure = false
            isiv_scale = false
            isiv_height = false
            isiv_width = true
            showImageChooseAlert()
        })

        binding.btnNxt.setOnClickListener(View.OnClickListener {
           val weight =  binding.etWeight.text.toString()
           val length =  binding.etLength.text.toString()
           val height =  binding.etHeight.text.toString()
           val width =  binding.etWidth.text.toString()
            if (weight.equals(""))
            {
                binding.etWeight.error = "Please enter vaild data"
            }
            else if(length.equals(""))
            {
                binding.etLength.error = "Please enter vaild data"
            }
            else if(height.equals(""))
            {
                binding.etHeight.error = "Please enter vaild data"
            }
            else if(width.equals(""))
            {
                binding.etWidth.error = "Please enter vaild data"
            }
            else
            {
                insertDataIntoDatabase(weight,length,height,width)
            }
        })
    }
    private fun showImageChooseAlert() {
        val dialog = Dialog(this@WeightAndDiemensActivity)
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
            val imagefilepath: String? = BitmapUtils.saveImage(this@WeightAndDiemensActivity, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> "+imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            if (isiv_measure) {
                file_iv_measure = destination
                binding.ivMeasurement.setImageBitmap(bitmap)
                uploadImage(file_iv_measure!!)
            }
            else if(isiv_height)
            {
                file_height = destination
                binding.ivHeight.setImageBitmap(bitmap)
                uploadImage(file_height!!)
            }
            else if(isiv_scale)
            {
                file_iv_scale = destination
                binding.ivWeight.setImageBitmap(bitmap)
                uploadImage(file_iv_scale!!)
            }
            else if(isiv_width)
            {
                file_width = destination
                binding.ivWidth.setImageBitmap(bitmap)
                uploadImage(file_width!!)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun createImageUri(): Uri? {
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
        if (ContextCompat.checkSelfPermission(this@WeightAndDiemensActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                val filePath = uriPathHelper.getPath(this@WeightAndDiemensActivity, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                if (isiv_measure) {
                    file_iv_measure = destination
                    binding.ivMeasurement.setImageBitmap(bitmap)
                    uploadImage(file_iv_measure!!)
                }
                else if(isiv_height)
                {
                    file_height = destination
                    binding.ivHeight.setImageBitmap(bitmap)
                    uploadImage(file_height!!)
                }
                else if(isiv_scale)
                {
                    file_iv_scale = destination
                    binding.ivWeight.setImageBitmap(bitmap)
                    uploadImage(file_iv_scale!!)
                }
                else if(isiv_width)
                {
                    file_width = destination
                    binding.ivWidth.setImageBitmap(bitmap)
                    uploadImage(file_width!!)
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }


        }
    }

    private fun uploadImage(file_image:File) {
        progressAlert.showAlert()
        // Create a folder using the inspection_id as the folder name
        val inspectionId = SharedHelper.getKey(this@WeightAndDiemensActivity, "inspection_id")
        val folderName = if (!inspectionId.isNullOrEmpty()) "inspections/$inspectionId/" else "inspections/"
      //  val filepath = Constants.FOLDER_NAME+file_image.name
        // Combine the folder name with the file name to get the complete filepath
        val filepath = "$folderName${file_image.name}"
        val trans = TransferUtility.builder().context(applicationContext).s3Client(s3Client).build()
        val observer: TransferObserver = trans.upload(Constants.BUCKET_NAME,filepath, file_image,CannedAccessControlList.PublicRead)//manual storage permission
        var s3FileUrl = ""
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    Log.d("msg","success")
                    s3FileUrl = "https://"+Constants.BUCKET_NAME+".s3.amazonaws.com/"+filepath
                    if (isiv_measure) {

                        str_iv_measure = s3FileUrl
                    }
                    else if(isiv_scale)
                    {

                        str_iv_scale = s3FileUrl
                    }
                    else if(isiv_height)
                    {

                        str_height = s3FileUrl
                    }
                    else if(isiv_width)
                    {

                        str_width = s3FileUrl
                    }
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
    fun insertDataIntoDatabase(weight:String,length:String,height:String,width:String)
    {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    var statement = connection.createStatement()
                    val inspection_id = SharedHelper.getKey(this@WeightAndDiemensActivity, "inspection_id")
                    val insertQuery = "INSERT INTO weight_dimension (weigth, length,height,width,scale_image,mesaure_image, width_image, height_image, inspection_id) VALUES ('"+weight+"', '"+length+"','"+height+"','"+width+"','"+str_iv_scale+"','"+str_iv_measure+"','"+str_width+"','"+str_height+"','"+inspection_id+"')"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery)
                    Log.e("insert query status","Data inserted successfully!")
                    runOnUiThread {
                        progressAlert.dismissAlert()
                        val intent = Intent(this@WeightAndDiemensActivity, PackageActivity::class.java)
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