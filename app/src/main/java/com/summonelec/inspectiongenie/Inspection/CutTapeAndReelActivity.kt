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
import com.summonelec.inspectiongenie.databinding.ActivityCutTapeAndReelBinding
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

class CutTapeAndReelActivity : AppCompatActivity() {
    lateinit var binding: ActivityCutTapeAndReelBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri: Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var iv_tape_reel_radio_group = false
    private var iv_label_place_image = false
    private var iv_label_place2_image = false
    private var iv_lead_tape_condition_image = false
    private var iv_order_match_radio_group = false
    private var iv_tape_leader_radio_group = false
    private var iv_lead_tape_condition_radio_group = false
    private var iv_part_orie_radio_group = false
    private var iv_tape_damage_radio_group = false
    private var iv_tape_damage_image = false
    private var str_tape_reel_radio_group: String? = null
    private var str_label_place_image: String? = null
    private var str_label_place2_image: String? = null
    private var str_lead_tape_condition_image: String? = null
    private var file_label_place_image: File? = null
    private var file_label_place2_image: File? = null
    private var file_lead_tape_condition_image: File? = null
    private var str_order_match_radio_group: String? = null
    private var str_tape_leader_radio_group: String? = null
    private var str_lead_tape_condition_radio_group: String? = null
    private var str_part_orie_radio_group: String? = null
    private var str_tape_damage_radio_group: String? = null
    private var str_tape_damage_image: String? = null
    private var file_tape_damage_image: File? = null
    private var creds: BasicAWSCredentials =
        BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cut_tape_and_reel)
        binding = ActivityCutTapeAndReelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tapeReelRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_tape_reel_radio_group = rb.text.toString()
            }

        }
        binding.orderMatchRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_order_match_radio_group = rb.text.toString()
            }

        }
        binding.tapeLeaderRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_tape_leader_radio_group = rb.text.toString()
            }

        }
        binding.leadTapeConditionRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_lead_tape_condition_radio_group = rb.text.toString()
            }

        }
        binding.partOrieRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_part_orie_radio_group = rb.text.toString()
            }

        }
        binding.tapeDamageRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_tape_damage_radio_group = rb.text.toString()
            }

        }
        progressAlert = ProgressAlert(this)
        binding.labelPlaceImage.setOnClickListener(View.OnClickListener {
            iv_tape_reel_radio_group = false
            iv_label_place_image = true
            iv_label_place2_image = false
            iv_lead_tape_condition_image = false
            iv_order_match_radio_group = false
            iv_tape_leader_radio_group = false
            iv_lead_tape_condition_radio_group = false
            iv_part_orie_radio_group = false
            iv_tape_damage_radio_group = false
            iv_tape_damage_image = false


            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.labelPlace2Image.setOnClickListener(View.OnClickListener {
            iv_tape_reel_radio_group = false
            iv_label_place_image = false
            iv_label_place2_image = true
            iv_lead_tape_condition_image = false
            iv_order_match_radio_group = false
            iv_tape_leader_radio_group = false
            iv_lead_tape_condition_radio_group = false
            iv_part_orie_radio_group = false
            iv_tape_damage_radio_group = false
            iv_tape_damage_image = false


            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.leadTapeConditionImage.setOnClickListener(View.OnClickListener {
            iv_tape_reel_radio_group = false
            iv_label_place_image = false
            iv_label_place2_image = false
            iv_lead_tape_condition_image = true
            iv_order_match_radio_group = false
            iv_tape_leader_radio_group = false
            iv_lead_tape_condition_radio_group = false
            iv_part_orie_radio_group = false
            iv_tape_damage_radio_group = false
            iv_tape_damage_image = false


            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.tapeDamageImage.setOnClickListener(View.OnClickListener {
            iv_tape_reel_radio_group = false
            iv_label_place_image = false
            iv_label_place2_image = false
            iv_lead_tape_condition_image = false
            iv_order_match_radio_group = false
            iv_tape_leader_radio_group = false
            iv_lead_tape_condition_radio_group = false
            iv_part_orie_radio_group = false
            iv_tape_damage_radio_group = false
            iv_tape_damage_image = true


            showImageChooseAlert()
        })

    }
    private fun showImageChooseAlert() {

        val dialog = Dialog(this@CutTapeAndReelActivity)
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
            val imagefilepath: String? = BitmapUtils.saveImage(this@CutTapeAndReelActivity, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> "+imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            if (iv_label_place_image) {
                file_label_place_image = destination
                binding.labelPlaceImage.setImageBitmap(bitmap)
                uploadImage(file_label_place_image!!)
            }
            else if(iv_label_place2_image) {
                file_label_place2_image = destination
                binding.labelPlace2Image.setImageBitmap(bitmap)
                uploadImage(file_label_place2_image!!)
            }
            else if(iv_lead_tape_condition_image){
                file_lead_tape_condition_image = destination
                binding.leadTapeConditionImage.setImageBitmap(bitmap)
                uploadImage(file_lead_tape_condition_image!!)
            }
            else if (iv_tape_damage_image)
                file_tape_damage_image = destination
            binding.tapeDamageImage.setImageBitmap(bitmap)
            uploadImage(file_tape_damage_image!!)

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
        if (ContextCompat.checkSelfPermission(this@CutTapeAndReelActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                val filePath = uriPathHelper.getPath(this@CutTapeAndReelActivity, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                if (iv_label_place_image) {
                    file_label_place_image = destination
                    binding.labelPlaceImage.setImageBitmap(bitmap)
                    uploadImage(file_label_place_image!!)
                }
                else if(iv_label_place2_image) {
                    file_label_place2_image = destination
                    binding.labelPlace2Image.setImageBitmap(bitmap)
                    uploadImage(file_label_place2_image!!)
                }
                else if(iv_lead_tape_condition_image){
                    file_lead_tape_condition_image = destination
                    binding.leadTapeConditionImage.setImageBitmap(bitmap)
                    uploadImage(file_lead_tape_condition_image!!)
                }
                else if (iv_tape_damage_image){
                    file_tape_damage_image = destination
                    binding.tapeDamageImage.setImageBitmap(bitmap)
                    uploadImage(file_tape_damage_image!!)
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
                    if(iv_label_place_image){
                        str_label_place_image = s3FileUrl
                    }
                    else if (iv_label_place2_image){
                        str_label_place2_image = s3FileUrl
                    }
                    else if (iv_lead_tape_condition_image){
                        str_lead_tape_condition_image = s3FileUrl
                    }
                    else if (iv_tape_damage_image){
                        str_tape_damage_image = s3FileUrl
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
            insertDataIntoDatabase(str_tape_reel_radio_group!!)

        })

}

private fun showError(errorMessage: String) {
    Toast.makeText(this@CutTapeAndReelActivity, errorMessage, Toast.LENGTH_SHORT).show()
}

fun insertDataIntoDatabase(str_tape_reel_radio_group:String)
{
    progressAlert.showAlert()
    GlobalScope.launch {
        val connection = DatabaseConnection.connectToDatabase()
        connection?.let {
            try {
                var statement = connection.createStatement()
                val inspection_id = SharedHelper.getKey(this@CutTapeAndReelActivity, "inspection_id")
                val insertQuery = "INSERT INTO cut_tape_reel(tape_reel_radio_group, label_place_image, label_place2_image, lead_tape_condition_image, order_match_radio_group, tape_leader_radio_group, lead_tape_condition_radio_group, part_orie_radio_group, tape_damage_radio_group, tape_damage_image, inspection_id) VALUES ('"+str_tape_reel_radio_group+"','"+str_label_place_image+"', '"+str_label_place2_image+"', '"+str_lead_tape_condition_image+"', '"+str_order_match_radio_group+"', '"+str_tape_leader_radio_group+"', '"+str_lead_tape_condition_radio_group+"', '"+str_part_orie_radio_group+"', '"+str_tape_damage_radio_group+"', '"+str_tape_damage_image+"', '"+inspection_id+"');"
               //val insertQuery = "INSERT INTO determine_parts(type_radio, puncher_bag_radio, bag_pun2, bag_seal, bag_overall_image, msl_sticker, et_sticker, msl_verification, hic_card, open_box, inspection_id) VALUES ('"+str_iv_type_radio+"', '"+str_iv_puncher_bag_radio+"', '"+str_iv_bag_pun2+"', '"+str_iv_bag_seal+"', '"+str_iv_bag_overall_image+"', '"+str_iv_msl_sticker+"', '"+str_iv_et_sticker+"', '"+str_iv_msl_verification+"', '"+str_iv_hic_card+"', '"+str_iv_open_box+"', '"+inspection_id+"');"
                Log.e("insert query",insertQuery)
                statement.executeUpdate(insertQuery)
                Log.e("insert query status","Data inserted successfully!")
                runOnUiThread {
                    progressAlert.dismissAlert()
                    val intent = Intent(this@CutTapeAndReelActivity, ReportReel::class.java)
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
