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
import com.summonelec.inspectiongenie.databinding.ActivityDeterminepPartBinding
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


class DeterminepPartActivity : AppCompatActivity() {
    lateinit var binding: ActivityDeterminepPartBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri: Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var iv_type_radio =false
    private var iv_puncher_bag_radio=false
    private var iv_bag_pun2= false
    private var iv_bag_seal= false
    private var iv_bag_overall_image= false
    private var msl_sticker= false
    private var iv_et_sticker= false
    private  var msl_verification = false
    private var hic_card = false
    private var open_box = false
    private var file_bag_seal: File? =null
    private var file_bag_overall_image: File? =null
    private var file_open_box: File? =null
    private var str_iv_type_radio: String? = null
    private var str_iv_puncher_bag_radio: String? = null
    private var str_iv_bag_pun2: String? = null
    private var str_iv_bag_seal: String? = null
    private var str_iv_bag_overall_image: String? = null
    private var str_iv_msl_sticker: String? = null
    private var str_iv_et_sticker: String? = null
    private  var str_iv_msl_verification: String? = null
    private var str_iv_hic_card: String? = null
    private var str_iv_open_box: String? = null
    private var str_msl_for_parts: String? = null

    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeterminepPartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnNxt.setOnClickListener(View.OnClickListener {
            if (iv_bag_seal && str_iv_bag_seal.isNullOrEmpty()) {
                // Bag Seal Image is required but not selected
                // Show an error message or handle it accordingly
                showError("Bag Seal Image is required.")
            } else {
                // Bag Seal Image is selected or not required, proceed with data insertion
                Log.d("Activity", "btnNxt Clicked - Data Inserted into Database")
            }
        })
        binding.punchersBagRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_iv_puncher_bag_radio = rb.text.toString()
            }
        }
            binding.typeRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
                val rb = radioGroup.findViewById<View>(i) as RadioButton
                if (null != rb && i > -1) {
                    // Get the selected option
                    str_iv_type_radio = rb.text.toString()
                }
            }
            binding.bagPunctuers.setOnCheckedChangeListener { radioGroup, i ->
                val rb = radioGroup.findViewById<View>(i) as RadioButton
                if (null != rb && i > -1) {
                    // Get the selected option
                    str_iv_bag_pun2 = rb.text.toString()
                }
            }
        binding.mslSticker.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_iv_msl_sticker = rb.text.toString()
            }
        }
        binding.mslVerification.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_iv_msl_verification = rb.text.toString()
            }
        }
        binding.hicCard.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_iv_hic_card = rb.text.toString()
            }
        }

            progressAlert = ProgressAlert(this)
            binding.bagSealImage.setOnClickListener(View.OnClickListener {
                 iv_type_radio = false
                iv_puncher_bag_radio = false
                iv_bag_pun2 = false
                iv_bag_seal = true
                iv_bag_overall_image = false
                msl_sticker = false
                iv_et_sticker = false
                msl_verification = false
                hic_card = false
                open_box = false

                showImageChooseAlert()
            })
        progressAlert = ProgressAlert(this)
        binding.bagOverallImage.setOnClickListener(View.OnClickListener {
            iv_type_radio = false
            iv_puncher_bag_radio = false
            iv_bag_pun2 = false
            iv_bag_seal = false
            iv_bag_overall_image = true
            msl_sticker = false
            iv_et_sticker = false
            msl_verification = false
            hic_card = false
            open_box = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.openBag.setOnClickListener(View.OnClickListener {
            iv_type_radio = false
            iv_puncher_bag_radio = false
            iv_bag_pun2 = false
            iv_bag_seal = false
            iv_bag_overall_image = false
            msl_sticker = false
            iv_et_sticker = false
            msl_verification = false
            hic_card = false
            open_box = true
            showImageChooseAlert()
        })

        // Retrieve text from EditText
        val etSticker = binding.etSticker
        str_iv_et_sticker = etSticker.text.toString()

        val mslForParts = binding.mslForParts
        str_msl_for_parts =  mslForParts.text.toString()


    }


    private fun showImageChooseAlert() {

        val dialog = Dialog(this@DeterminepPartActivity)
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
            val imagefilepath: String? = BitmapUtils.saveImage(this@DeterminepPartActivity, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> "+imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            if (iv_bag_seal) {
                file_bag_seal = destination
                binding.bagSealImage.setImageBitmap(bitmap)
                uploadImage(file_bag_seal!!)
            }
            else if(iv_bag_overall_image) {
                file_bag_overall_image = destination
                binding.bagOverallImage.setImageBitmap(bitmap)
                uploadImage(file_bag_overall_image!!)
            }
            else if(open_box){
                file_open_box = destination
                binding.openBag.setImageBitmap(bitmap)
                uploadImage(file_open_box!!)
            }
     /*       binding.bagSealImage.setImageBitmap(bitmap)
            binding.bagOverallImage.setImageBitmap(bitmap)
            binding.openBag.setImageBitmap(bitmap)
            uploadImage(destination!!)*/


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
        if (ContextCompat.checkSelfPermission(this@DeterminepPartActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                val filePath = uriPathHelper.getPath(this@DeterminepPartActivity, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                if (iv_bag_seal) {
                    file_bag_seal = destination
                    binding.bagSealImage.setImageBitmap(bitmap)
                    uploadImage(file_bag_seal!!)
                }
                else if(iv_bag_overall_image) {
                    file_bag_overall_image = destination
                    binding.bagOverallImage.setImageBitmap(bitmap)
                    uploadImage(file_bag_overall_image!!)
                }
                else if(open_box){
                    file_open_box = destination
                    binding.openBag.setImageBitmap(bitmap)
                    uploadImage(file_open_box!!)
                }

             /*   binding.bagSealImage.setImageBitmap(bitmap)
                binding.bagOverallImage.setImageBitmap(bitmap)
                binding.openBag.setImageBitmap(bitmap)
                uploadImage(destination!!)*/

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
                    if(iv_bag_seal){
                        str_iv_bag_seal = s3FileUrl
                    }
                    else if (iv_bag_overall_image){
                        str_iv_bag_overall_image = s3FileUrl
                    }
                    else if (open_box){
                        str_iv_open_box = s3FileUrl
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
          //  insertDataIntoDatabase(str_iv_type_radio!!)
         //   Log.d("Activity", "btnNxt Clicked - Data Inserted into Database")
            if (iv_bag_seal && str_iv_bag_seal.isNullOrEmpty()) {
                // Bag Seal Image is required but not selected
                // Show an error message or handle it accordingly
                showError("Bag Seal Image is required.")
            } else {
                // Bag Seal Image is selected or not required, proceed with data insertion
                insertDataIntoDatabase(str_iv_type_radio!!)
                Log.d("Activity", "btnNxt Clicked - Data Inserted into Database")
            }

        })
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this@DeterminepPartActivity, errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun insertDataIntoDatabase(str_iv_type_radio:String)
    {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    var statement = connection.createStatement()
                    val inspection_id = SharedHelper.getKey(this@DeterminepPartActivity, "inspection_id")
                    val insertQuery = "INSERT INTO determine_parts(type_radio, puncher_bag_radio, bag_pun2, bag_seal, bag_overall_image, msl_sticker, et_sticker, msl_verification, hic_card, open_box,et_msl_for_parts, inspection_id) VALUES ('"+str_iv_type_radio+"', '"+str_iv_puncher_bag_radio+"', '"+str_iv_bag_pun2+"', '"+str_iv_bag_seal+"', '"+str_iv_bag_overall_image+"', '"+str_iv_msl_sticker+"', '"+str_iv_et_sticker+"', '"+str_iv_msl_verification+"', '"+str_iv_hic_card+"', '"+str_iv_open_box+"', '"+str_msl_for_parts+"', '"+inspection_id+"');"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery)
                    Log.e("insert query status","Data inserted successfully!")
                    runOnUiThread {
                        progressAlert.dismissAlert()
                     /*  val intent = Intent(this@DeterminepPartActivity, ReportActivity::class.java)
                        startActivity(intent) */
                        when (str_iv_type_radio) {
                            "REEL" -> {
                                val intent = Intent(this@DeterminepPartActivity, CutTapeAndReelActivity::class.java)
                                startActivity(intent)
                            }
                            "TUBE" -> {
                                val intent = Intent(this@DeterminepPartActivity, TubeInspectionActivity::class.java)
                                startActivity(intent)
                            }
                            "TRAY" -> {
                                val intent = Intent(this@DeterminepPartActivity, TrayInspectionActivity::class.java)
                                startActivity(intent)
                            }
                            "BULK" -> {
                                val intent = Intent(this@DeterminepPartActivity, BulkBagBoxActivity::class.java)
                                startActivity(intent)
                            }
                            // Add more cases as needed
                        }
                        Log.d("Activity", "Time up")
                        // Finish the current activity
                        finish()
                    }

                } catch (e: SQLException) {
                    e.printStackTrace()
                }

                it.close() // Don't forget to close the connection when done
            }

        }

    }
}