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
import com.summonelec.inspectiongenie.databinding.ActivityMechanicalInspectionBinding
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

class MechanicalInspection : AppCompatActivity() {
    lateinit var binding: ActivityMechanicalInspectionBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri : Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    private var part_marking_image = false
    private var length_data_image = false
    private var width_data_image = false
    private var thickness_data_image = false
    private var file_part_marking_image: File? = null
    private var file_length_data_image: File? = null
    private var file_width_data_image: File? = null
    private var file_thickness_data_image: File? = null
    private var str_part_marking_image: String? = null
    private var str_length_data_image: String? = null
    private var str_width_data_image: String? = null
    private var str_thickness_data_image: String? = null
    private var str_PartMarking: String? = null
    private var str_WidhtData: String? = null
    private  var str_LengthData: String? = null
    private var str_ThicknessData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mechanical_inspection)
        binding = ActivityMechanicalInspectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressAlert = ProgressAlert(this)
        binding.partMarkingImage.setOnClickListener(View.OnClickListener {
            part_marking_image = true
            length_data_image = false
            width_data_image = false
            thickness_data_image = false
            showImageChooseAlert()
        })
        binding.lengthDataImage.setOnClickListener(View.OnClickListener {
            part_marking_image = false
            length_data_image = true
            width_data_image = false
            thickness_data_image = false
            showImageChooseAlert()
        })
        binding.widthDataImage.setOnClickListener(View.OnClickListener {
            part_marking_image = false
            length_data_image = false
            width_data_image = true
            thickness_data_image = false
            showImageChooseAlert()
        })
        binding.thicknessDataImage.setOnClickListener(View.OnClickListener {
            part_marking_image = false
            length_data_image = false
            width_data_image = false
            thickness_data_image = true
            showImageChooseAlert()
        })
        binding.btnNxt.setOnClickListener(View.OnClickListener {
            val PartMarking =  binding.etPartMarking
                str_PartMarking = PartMarking.text.toString()
            val WidhData =  binding.etWidthData
                str_WidhtData= WidhData.text.toString()
            val LengthData =  binding.etLengthData
                str_LengthData = LengthData.text.toString()
            val ThicknessData =  binding.etThicknessData
                str_ThicknessData = ThicknessData.text.toString()
        })
    }
    private fun showImageChooseAlert() {
        val dialog = Dialog(this@MechanicalInspection)
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
            val imagefilepath: String? = BitmapUtils.saveImage(this@MechanicalInspection, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> "+imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            if (part_marking_image) {
                file_part_marking_image = destination
                binding.partMarkingImage.setImageBitmap(bitmap)
                uploadImage(file_part_marking_image!!)
            }
            else if(length_data_image)
            {
                file_length_data_image = destination
                binding.lengthDataImage.setImageBitmap(bitmap)
                uploadImage(file_length_data_image!!)
            }
            else if(width_data_image)
            {
                file_width_data_image = destination
                binding.widthDataImage.setImageBitmap(bitmap)
                uploadImage(file_width_data_image!!)
            }
            else if(thickness_data_image)
            {
                file_thickness_data_image = destination
                binding.thicknessDataImage.setImageBitmap(bitmap)
                uploadImage(file_thickness_data_image!!)
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
        if (ContextCompat.checkSelfPermission(this@MechanicalInspection, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                val filePath = uriPathHelper.getPath(this@MechanicalInspection, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                if (part_marking_image) {
                    file_part_marking_image = destination
                    binding.partMarkingImage.setImageBitmap(bitmap)
                    uploadImage(file_part_marking_image!!)
                }
                else if(length_data_image)
                {
                    file_length_data_image = destination
                    binding.lengthDataImage.setImageBitmap(bitmap)
                    uploadImage(file_length_data_image!!)
                }
                else if(width_data_image)
                {
                    file_width_data_image = destination
                    binding.widthDataImage.setImageBitmap(bitmap)
                    uploadImage(file_width_data_image!!)
                }
                else if(thickness_data_image)
                {
                    file_thickness_data_image = destination
                    binding.thicknessDataImage.setImageBitmap(bitmap)
                    uploadImage(file_thickness_data_image!!)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }


        }
    }
    private fun uploadImage(file_image:File) {
        progressAlert.showAlert()
        // Create a folder using the inspection_id as the folder name
        val inspectionId = SharedHelper.getKey(this@MechanicalInspection, "inspection_id")
        val folderName = if (!inspectionId.isNullOrEmpty()) "inspections/$inspectionId/" else "inspections/"
        //  val filepath = Constants.FOLDER_NAME+file_image.name
        // Combine the folder name with the file name to get the complete filepath
        val filepath = "$folderName${file_image.name}"
        val trans = TransferUtility.builder().context(applicationContext).s3Client(s3Client).build()
        val observer: TransferObserver = trans.upload(Constants.BUCKET_NAME,filepath, file_image,
            CannedAccessControlList.PublicRead)//manual storage permission
        var s3FileUrl = ""
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    Log.d("msg","success")
                    s3FileUrl = "https://"+Constants.BUCKET_NAME+".s3.amazonaws.com/"+filepath
                    if (part_marking_image) {

                        str_part_marking_image = s3FileUrl
                    }
                    else if(length_data_image)
                    {

                        str_length_data_image = s3FileUrl
                    }
                    else if(width_data_image)
                    {

                        str_width_data_image = s3FileUrl
                    }
                    else if(thickness_data_image)
                    {

                        str_thickness_data_image = s3FileUrl
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
        fun insertDataIntoDatabase()
        {
            progressAlert.showAlert()
            GlobalScope.launch {
                val connection = DatabaseConnection.connectToDatabase()
                connection?.let {
                    try {
                        var statement = connection.createStatement()
                        val inspection_id = SharedHelper.getKey(this@MechanicalInspection, "inspection_id")
                        val insertQuery = "INSERT INTO mechanical_inspection (etPartMarking, etWidthData, etLengthData, etThicknessData, part_marking_image, length_data_image, width_data_image, thickness_data_image, inspection_id) VALUES ('"+str_PartMarking+"', '"+str_WidhtData+"', '"+str_LengthData+"', '"+str_ThicknessData+"', '"+str_part_marking_image+"', '"+str_length_data_image+"', '"+str_width_data_image+"', '"+str_thickness_data_image+"','"+inspection_id+"');"
                        Log.e("insert query",insertQuery)
                        statement.executeUpdate(insertQuery)
                        Log.e("insert query status","Data inserted successfully!")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            val intent = Intent(this@MechanicalInspection, PackageActivity::class.java)
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
}