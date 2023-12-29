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
import android.widget.RadioButton
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
import com.summonelec.inspectiongenie.databinding.ActivityTubeInspectionBinding
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

class TubeInspectionActivity : AppCompatActivity() {
    lateinit var binding: ActivityTubeInspectionBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri: Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var iv_logo_tube_image = false
    private var iv_tube_length_image = false
    private var iv_issue_tube_image = false
    private var iv_direction_tube_image = false
    private var str_logo_tube_radio_group: String? = null
    private var str_tube_length_radio_group: String? = null
    private var str_issue_tube_radio_group: String? = null
    private var str_et_tube_damage: String? = null
    private var str_driection_tube_radio_group: String? = null
    private var str_quantity_tubes_radio_group: String? = null
    private var str_logo_tube_image: String? = null
    private var str_tube_length_image: String? = null
    private var str_issue_tube_image: String? = null
    private var str_direction_tube_image: String? = null
    private var File_logo_tube_image: File? = null
    private var File_tube_length_image: File? = null
    private var File_issue_tube_image: File? = null
    private var File_direction_tube_image: File? = null
    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tube_inspection)
        binding = ActivityTubeInspectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.logoTubeRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_logo_tube_radio_group = rb.text.toString()
            }

        }
        binding.tubeLengthRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_tube_length_radio_group = rb.text.toString()
            }

        }
        binding.issueTubeRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_issue_tube_radio_group = rb.text.toString()
            }

        }
        binding.driectionTubeRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_driection_tube_radio_group = rb.text.toString()
            }

        }
        binding.quantityTubesRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_quantity_tubes_radio_group = rb.text.toString()
            }

        }
        progressAlert = ProgressAlert(this)
        binding.logoTubeImage.setOnClickListener(View.OnClickListener {
            iv_logo_tube_image = true
             iv_tube_length_image = false
             iv_issue_tube_image = false
             iv_direction_tube_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.tubeLengthImage.setOnClickListener(View.OnClickListener {
            iv_logo_tube_image = false
            iv_tube_length_image = true
            iv_issue_tube_image = false
            iv_direction_tube_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.issueTubeImage.setOnClickListener(View.OnClickListener {
            iv_logo_tube_image = false
            iv_tube_length_image = false
            iv_issue_tube_image = true
            iv_direction_tube_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.directionTubeImage.setOnClickListener(View.OnClickListener {
            iv_logo_tube_image = false
            iv_tube_length_image = false
            iv_issue_tube_image = false
            iv_direction_tube_image = true
            showImageChooseAlert()
        })
         str_et_tube_damage = binding.etTubeDamage.text.toString()
    }
    private fun showImageChooseAlert() {

        val dialog = Dialog(this@TubeInspectionActivity)
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
            val imagefilepath: String? = BitmapUtils.saveImage(this@TubeInspectionActivity, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> "+imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            if (iv_logo_tube_image) {
                File_logo_tube_image = destination
                binding.logoTubeImage.setImageBitmap(bitmap)
                uploadImage(File_logo_tube_image!!)
            }
            else if(iv_tube_length_image) {
                File_tube_length_image = destination
                binding.tubeLengthImage.setImageBitmap(bitmap)
                uploadImage(File_tube_length_image!!)
            }
            else if(iv_direction_tube_image){
                File_direction_tube_image = destination
                binding.directionTubeImage.setImageBitmap(bitmap)
                uploadImage(File_direction_tube_image!!)
            }
            else if (iv_issue_tube_image)
                File_issue_tube_image = destination
            binding.issueTubeImage.setImageBitmap(bitmap)
            uploadImage(File_issue_tube_image!!)

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
        if (ContextCompat.checkSelfPermission(this@TubeInspectionActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                val filePath = uriPathHelper.getPath(this@TubeInspectionActivity, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                if (iv_logo_tube_image) {
                    File_logo_tube_image = destination
                    binding.logoTubeImage.setImageBitmap(bitmap)
                    uploadImage(File_logo_tube_image!!)
                }
                else if(iv_tube_length_image) {
                    File_tube_length_image = destination
                    binding.tubeLengthImage.setImageBitmap(bitmap)
                    uploadImage(File_tube_length_image!!)
                }
                else if(iv_direction_tube_image){
                    File_direction_tube_image = destination
                    binding.directionTubeImage.setImageBitmap(bitmap)
                    uploadImage(File_direction_tube_image!!)
                }
                else if (iv_issue_tube_image){
                    File_issue_tube_image = destination
                    binding.issueTubeImage.setImageBitmap(bitmap)
                    uploadImage(File_issue_tube_image!!)
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }


        }
    }
    private fun uploadImage(file_image:File) {
        progressAlert.showAlert()
        val filepath = Constants.FOLDER_NAME+file_image.name
        val trans = TransferUtility.builder().context(applicationContext).s3Client(s3Client).build()
        val observer: TransferObserver = trans.upload(Constants.BUCKET_NAME,filepath, file_image,
            CannedAccessControlList.PublicRead)//manual storage permission
        var s3FileUrl = ""
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    Log.d("msg","success")
                    s3FileUrl = "https://"+Constants.BUCKET_NAME+".s3.amazonaws.com/"+filepath
                    if(iv_logo_tube_image){
                        str_logo_tube_image = s3FileUrl
                    }
                    else if (iv_tube_length_image){
                        str_tube_length_image = s3FileUrl
                    }
                    else if (iv_direction_tube_image){
                        str_direction_tube_image = s3FileUrl
                    }
                    else if (iv_issue_tube_image){
                        str_issue_tube_image = s3FileUrl
                    }
                    /*   str_iv_bag_seal = s3FileUrl
                       str_iv_bag_overall_image = s3FileUrl
                       str_iv_open_box = s3FileUrl */

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

        binding.btnNxt.setOnClickListener(View.OnClickListener {
            // Bag Seal Image is selected or not required, proceed with data insertion
            insertDataIntoDatabase(str_logo_tube_radio_group!!)

        })

    }
    fun insertDataIntoDatabase(str_logo_tube_radio_group:String)
    {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    var statement = connection.createStatement()
                    val inspection_id = SharedHelper.getKey(this@TubeInspectionActivity, "inspection_id")
                    val insertQuery = "INSERT INTO tube_inspection (logo_tube_radio_group, tube_length_radio_group, issue_tube_radio_group, et_tube_damage, direction_tube_radio_group, quantity_tubes, logo_tube_image, tube_length_image, issue_tube_image, direction_tube_image, inspection_id) VALUES ('"+str_logo_tube_radio_group+"', '"+str_tube_length_radio_group+"', '"+str_issue_tube_radio_group+"', '"+str_et_tube_damage+"', '"+str_driection_tube_radio_group+"', '"+str_quantity_tubes_radio_group+"', '"+str_logo_tube_image+"', '"+str_tube_length_image+"', '"+str_issue_tube_image+"', '"+str_direction_tube_image+"', '"+inspection_id+"');"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery)
                    Log.e("insert query status","Data inserted successfully!")
                    runOnUiThread {
                        progressAlert.dismissAlert()
                        val intent = Intent(this@TubeInspectionActivity, Tube_report::class.java)
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