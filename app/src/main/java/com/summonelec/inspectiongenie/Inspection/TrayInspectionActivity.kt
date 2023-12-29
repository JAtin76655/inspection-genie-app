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
import com.summonelec.inspectiongenie.databinding.ActivityTrayInspectionBinding
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

class TrayInspectionActivity : AppCompatActivity() {
    lateinit var binding: ActivityTrayInspectionBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri: Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var iv_tray_ori_image = false
    private var iv_tray_missing_chips_image = false
    private var iv_lot_chips_no_image = false
    private var iv_lot_code_image = false
    private var iv_tray_banded_image = false
    private var iv_tray_cover_image = false
    private var str_tray_banded_radio_group: String? = null
    private var str_tray_cover_radio_group: String? = null
    private var str_tray_ori_radio_group: String? = null
    private var str_tray_missing_chips_radio_group: String? = null
    private var str_et_lot_code: String? = null
    private var str_et_lot_chips_no: String? = null
    private var str_tray_ori_image: String? = null
    private var str_tray_missing_chips_image: String? = null
    private var str_lot_chips_no_image: String? = null
    private var str_lot_code_image: String? = null
    private var str_tray_banded_image: String? = null
    private var str_tray_cover_image: String? = null
    private var file_tray_ori_image: File? = null
    private var file_tray_missing_chips_image: File? = null
    private var file_lot_chips_no_image: File? = null
    private var file_lot_code_image: File? = null
    private var file_tray_banded_image: File? = null
    private var file_tray_cover_image: File? = null
    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tray_inspection)
        binding = ActivityTrayInspectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.trayBandedRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_tray_banded_radio_group = rb.text.toString()
            }

        }
        binding.trayCoverRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_tray_cover_radio_group = rb.text.toString()
            }

        }
        binding.trayOriRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_tray_ori_radio_group = rb.text.toString()
            }

        }
        binding.trayMissingChipsRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_tray_missing_chips_radio_group = rb.text.toString()
            }

        }
        progressAlert = ProgressAlert(this)
        binding.trayBandedImage.setOnClickListener(View.OnClickListener {
             iv_tray_ori_image = false
             iv_tray_missing_chips_image = false
             iv_lot_chips_no_image = false
             iv_lot_code_image = false
             iv_tray_banded_image = true
             iv_tray_cover_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.trayCoverImage.setOnClickListener(View.OnClickListener {
            iv_tray_ori_image = false
            iv_tray_missing_chips_image = false
            iv_lot_chips_no_image = false
            iv_lot_code_image = false
            iv_tray_banded_image = false
            iv_tray_cover_image = true
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.trayOriImage.setOnClickListener(View.OnClickListener {
            iv_tray_ori_image = true
            iv_tray_missing_chips_image = false
            iv_lot_chips_no_image = false
            iv_lot_code_image = false
            iv_tray_banded_image = false
            iv_tray_cover_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.lotChipsNoImage.setOnClickListener(View.OnClickListener {
            iv_tray_ori_image = false
            iv_tray_missing_chips_image = false
            iv_lot_chips_no_image = true
            iv_lot_code_image = false
            iv_tray_banded_image = false
            iv_tray_cover_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.trayMissingChipsImage.setOnClickListener(View.OnClickListener {
            iv_tray_ori_image = false
            iv_tray_missing_chips_image = true
            iv_lot_chips_no_image = false
            iv_lot_code_image = false
            iv_tray_banded_image = false
            iv_tray_cover_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.lotCodeImage.setOnClickListener(View.OnClickListener {
            iv_tray_ori_image = false
            iv_tray_missing_chips_image = false
            iv_lot_chips_no_image = false
            iv_lot_code_image = true
            iv_tray_banded_image = false
            iv_tray_cover_image = false
            showImageChooseAlert()
        })

        str_et_lot_code = binding.etLotCode.text.toString()
        str_et_lot_chips_no = binding.etLotChipsNo.text.toString()
    }
    private fun showImageChooseAlert() {

        val dialog = Dialog(this@TrayInspectionActivity)
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
            val imagefilepath: String? =
                BitmapUtils.saveImage(this@TrayInspectionActivity, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> " + imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            if (iv_tray_banded_image) {
                file_tray_banded_image = destination
                binding.trayBandedImage.setImageBitmap(bitmap)
                uploadImage(file_tray_banded_image!!)
            } else if (iv_tray_cover_image) {
                file_tray_cover_image = destination
                binding.trayCoverImage.setImageBitmap(bitmap)
                uploadImage(file_tray_cover_image!!)
            } else if (iv_tray_missing_chips_image) {
                file_tray_missing_chips_image = destination
                binding.trayMissingChipsImage.setImageBitmap(bitmap)
                uploadImage(file_tray_missing_chips_image!!)
            } else if (iv_lot_chips_no_image){
                file_lot_chips_no_image = destination
                binding.lotChipsNoImage.setImageBitmap(bitmap)
                uploadImage(file_lot_chips_no_image!!)
            } else if (iv_lot_code_image) {
                file_lot_code_image = destination
                binding.lotChipsNoImage.setImageBitmap(bitmap)
                uploadImage(file_lot_code_image!!)
            }else if (iv_tray_ori_image) {
                file_tray_ori_image = destination
                binding.trayOriImage.setImageBitmap(bitmap)
                uploadImage(file_tray_ori_image!!)
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
        if (ContextCompat.checkSelfPermission(this@TrayInspectionActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                val filePath = uriPathHelper.getPath(this@TrayInspectionActivity, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                if (iv_tray_banded_image) {
                    file_tray_banded_image = destination
                    binding.trayBandedImage.setImageBitmap(bitmap)
                    uploadImage(file_tray_banded_image!!)
                } else if (iv_tray_cover_image) {
                    file_tray_cover_image = destination
                    binding.trayCoverImage.setImageBitmap(bitmap)
                    uploadImage(file_tray_cover_image!!)
                } else if (iv_tray_missing_chips_image) {
                    file_tray_missing_chips_image = destination
                    binding.trayMissingChipsImage.setImageBitmap(bitmap)
                    uploadImage(file_tray_missing_chips_image!!)
                } else if (iv_lot_chips_no_image){
                    file_lot_chips_no_image = destination
                    binding.lotChipsNoImage.setImageBitmap(bitmap)
                    uploadImage(file_lot_chips_no_image!!)
                } else if (iv_lot_code_image) {
                    file_lot_code_image = destination
                    binding.lotChipsNoImage.setImageBitmap(bitmap)
                    uploadImage(file_lot_code_image!!)
                }
                else if (iv_tray_ori_image) {
                    file_tray_ori_image = destination
                    binding.trayOriImage.setImageBitmap(bitmap)
                    uploadImage(file_tray_ori_image!!)
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
                    if(iv_tray_banded_image){
                        str_tray_banded_image = s3FileUrl
                    }
                    else if (iv_tray_cover_image){
                        str_tray_cover_image = s3FileUrl
                    }
                    else if (iv_tray_missing_chips_image){
                        str_tray_missing_chips_image = s3FileUrl
                    }
                    else if (iv_lot_chips_no_image){
                        str_lot_chips_no_image = s3FileUrl
                    }
                    else if (iv_lot_code_image){
                        str_lot_code_image = s3FileUrl
                    }
                    else if(iv_tray_ori_image){
                        str_tray_ori_image = s3FileUrl
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
            insertDataIntoDatabase(str_tray_banded_radio_group!!)

        })

    }
    fun insertDataIntoDatabase(str_tray_banded_radio_group:String)
    {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    var statement = connection.createStatement()
                    val inspection_id = SharedHelper.getKey(this@TrayInspectionActivity, "inspection_id")
                    val insertQuery = "INSERT INTO tray_inspection (tray_banded_radio_group, tray_cover_radio_group, tray_ori_radio_group, tray_missing_chips_radio_group, et_lot_code, et_lot_chips_no, tray_ori_image, tray_missing_chips_image, lot_chips_no_image, lot_code_image, tray_banded_image, tray_cover_image, inspection_id) VALUES ('"+str_tray_banded_radio_group+"', '"+str_tray_cover_radio_group+"', '"+str_tray_ori_radio_group+"', '"+str_tray_missing_chips_radio_group+"', '"+str_et_lot_code+"', '"+str_et_lot_chips_no+"', '"+str_tray_ori_image+"', '"+str_tray_missing_chips_image+"', '"+str_lot_chips_no_image+"', '"+str_lot_code_image+"', '"+str_tray_banded_image+"', '"+str_tray_cover_image+"', '"+inspection_id+"');"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery)
                    Log.e("insert query status","Data inserted successfully!")
                    runOnUiThread {
                        progressAlert.dismissAlert()
                        val intent = Intent(this@TrayInspectionActivity, ReportTray::class.java)
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
