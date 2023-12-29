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
import com.summonelec.inspectiongenie.databinding.ActivityBulkBagBoxBinding
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

class BulkBagBoxActivity : AppCompatActivity() {
    lateinit var binding: ActivityBulkBagBoxBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri: Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var iv_bag_parts_image = false
    private var iv_factory_label_image = false
    private var iv_manu_packing_slip_image = false
    private var iv_mfg_bags_image = false
    private var iv_partial_bags_image = false
    private var iv_multiple_label_image = false
    private var str_bag_parts_radio_group: String? = null
    private var str_factory_label_radio_group: String? = null
    private var str_manu_packing_slip_radio_group: String? = null
    private var str_multiple_bag_radio_group: String? = null
    private var str_et_bag_parts_inside: String? = null
    private var str_et_mfg_bags: String? = null
    private var str_et_partial_bag: String? = null
    private var str_bag_parts_image: String? = null
    private var str_factory_label_image: String? = null
    private var str_manu_packing_slip_image: String? = null
    private var str_mfg_bags_image: String? = null
    private var str_partial_bags_image: String? = null
    private var str_multiple_label_image: String? = null
    private var file_bag_parts_image: File? = null
    private var file_factory_label_image: File? = null
    private var file_manu_packing_slip_image: File? = null
    private var file_mfg_bags_image: File? = null
    private var file_partial_bags_image: File? = null
    private var file_multiple_label_image: File? = null
    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulk_bag_box)
        binding = ActivityBulkBagBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bagPartsRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_bag_parts_radio_group = rb.text.toString()
            }

        }
        binding.factoryLabelRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_factory_label_radio_group = rb.text.toString()
            }

        }
        binding.manuPackingSlipRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_manu_packing_slip_radio_group = rb.text.toString()
            }

        }
        binding.multipleBagRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_multiple_bag_radio_group= rb.text.toString()
            }

        }
        progressAlert = ProgressAlert(this)
        binding.bagPartsImage.setOnClickListener(View.OnClickListener {
            iv_bag_parts_image = true
            iv_factory_label_image = false
            iv_manu_packing_slip_image = false
            iv_mfg_bags_image = false
            iv_partial_bags_image = false
            iv_multiple_label_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.factoryLabelImage.setOnClickListener(View.OnClickListener {
            iv_bag_parts_image = false
            iv_factory_label_image = true
            iv_manu_packing_slip_image = false
            iv_mfg_bags_image = false
            iv_partial_bags_image = false
            iv_multiple_label_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.manuPackingSlipImage.setOnClickListener(View.OnClickListener {
            iv_bag_parts_image = false
            iv_factory_label_image = false
            iv_manu_packing_slip_image = true
            iv_mfg_bags_image = false
            iv_partial_bags_image = false
            iv_multiple_label_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.mfgBagsImage.setOnClickListener(View.OnClickListener {
            iv_bag_parts_image = false
            iv_factory_label_image = false
            iv_manu_packing_slip_image = false
            iv_mfg_bags_image = true
            iv_partial_bags_image = false
            iv_multiple_label_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.partialBagsImage.setOnClickListener(View.OnClickListener {
            iv_bag_parts_image = false
            iv_factory_label_image = false
            iv_manu_packing_slip_image = false
            iv_mfg_bags_image = false
            iv_partial_bags_image = true
            iv_multiple_label_image = false
            showImageChooseAlert()
        })
        progressAlert = ProgressAlert(this)
        binding.multipleLabelImage.setOnClickListener(View.OnClickListener {
            iv_bag_parts_image = false
            iv_factory_label_image = false
            iv_manu_packing_slip_image = false
            iv_mfg_bags_image = false
            iv_partial_bags_image = false
            iv_multiple_label_image = true
            showImageChooseAlert()
        })

        binding.btnNxt.setOnClickListener(View.OnClickListener {
            str_et_mfg_bags = binding.etMfgBags.text.toString()
            str_et_partial_bag = binding.etPartialBag.text.toString()
            str_et_bag_parts_inside = binding.etBagPartsInside.text.toString()
            // Bag Seal Image is selected or not required, proceed with data insertion
            insertDataIntoDatabase(str_factory_label_radio_group!!)

        })

    }
    private fun showImageChooseAlert() {

        val dialog = Dialog(this@BulkBagBoxActivity)
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
                BitmapUtils.saveImage(this@BulkBagBoxActivity, bitmap!!)
            Log.e("Activity", "Pick from Camera::>>> " + imagefilepath)
            destination = File(imagefilepath)
            imgPath = destination!!.absolutePath
            if (iv_factory_label_image) {
                file_factory_label_image = destination
                binding.factoryLabelImage.setImageBitmap(bitmap)
                uploadImage(file_factory_label_image!!)
            } else if (iv_multiple_label_image) {
                file_multiple_label_image = destination
                binding.multipleLabelImage.setImageBitmap(bitmap)
                uploadImage(file_multiple_label_image!!)
            } else if (iv_manu_packing_slip_image) {
                file_manu_packing_slip_image = destination
                binding.manuPackingSlipImage.setImageBitmap(bitmap)
                uploadImage(file_manu_packing_slip_image!!)
            } else if (iv_mfg_bags_image){
                file_mfg_bags_image = destination
                binding.mfgBagsImage.setImageBitmap(bitmap)
                uploadImage(file_mfg_bags_image!!)
            } else if (iv_bag_parts_image) {
                file_bag_parts_image = destination
                binding.bagPartsImage.setImageBitmap(bitmap)
                uploadImage(file_bag_parts_image!!)
            }else if (iv_partial_bags_image) {
                file_partial_bags_image = destination
                binding.partialBagsImage.setImageBitmap(bitmap)
                uploadImage(file_partial_bags_image!!)
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
        if (ContextCompat.checkSelfPermission(this@BulkBagBoxActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                val filePath = uriPathHelper.getPath(this@BulkBagBoxActivity, selectedImage!!)
                destination = File(filePath.toString())
                imgPath = destination!!.absolutePath
                if (iv_factory_label_image) {
                    file_factory_label_image = destination
                    binding.factoryLabelImage.setImageBitmap(bitmap)
                    uploadImage(file_factory_label_image!!)
                } else if (iv_multiple_label_image) {
                    file_multiple_label_image = destination
                    binding.multipleLabelImage.setImageBitmap(bitmap)
                    uploadImage(file_multiple_label_image!!)
                } else if (iv_manu_packing_slip_image) {
                    file_manu_packing_slip_image = destination
                    binding.manuPackingSlipImage.setImageBitmap(bitmap)
                    uploadImage(file_manu_packing_slip_image!!)
                } else if (iv_mfg_bags_image){
                    file_mfg_bags_image = destination
                    binding.mfgBagsImage.setImageBitmap(bitmap)
                    uploadImage(file_mfg_bags_image!!)
                } else if (iv_bag_parts_image) {
                    file_bag_parts_image = destination
                    binding.bagPartsImage.setImageBitmap(bitmap)
                    uploadImage(file_bag_parts_image!!)
                }else if (iv_partial_bags_image) {
                    file_partial_bags_image = destination
                    binding.partialBagsImage.setImageBitmap(bitmap)
                    uploadImage(file_partial_bags_image!!)
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
                    if(iv_partial_bags_image){
                        str_partial_bags_image = s3FileUrl
                    }
                    else if (iv_bag_parts_image){
                        str_bag_parts_image = s3FileUrl
                    }
                    else if (iv_multiple_label_image){
                        str_multiple_label_image = s3FileUrl
                    }
                    else if (iv_factory_label_image){
                        str_factory_label_image = s3FileUrl
                    }
                    else if (iv_manu_packing_slip_image){
                        str_manu_packing_slip_image = s3FileUrl
                    }
                    else if(iv_mfg_bags_image){
                        str_mfg_bags_image = s3FileUrl
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
    }
    fun insertDataIntoDatabase(str_tray_banded_radio_group:String)
    {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    var statement = connection.createStatement()
                    val inspection_id = SharedHelper.getKey(this@BulkBagBoxActivity, "inspection_id")
                    val insertQuery = "INSERT INTO bag_inspection (bag_parts_radio_group, factory_label_radio_group, manu_packing_slip_radio_group, multiple_bag_radio_group, et_bag_parts_inside, et_mfg_bags, et_partial_bag, bag_parts_image, factory_label_image, manu_packing_slip_image, mfg_bags_image, partial_bags_image, multiple_label_image, inspection_id) VALUES ('"+str_bag_parts_radio_group+"', '"+str_factory_label_radio_group+"', '"+str_manu_packing_slip_radio_group+"', '"+str_multiple_bag_radio_group+"', '"+str_et_bag_parts_inside+"', '"+str_et_mfg_bags+"', '"+str_et_partial_bag+"', '"+str_bag_parts_image+"', '"+str_factory_label_image+"', '"+str_manu_packing_slip_image+"', '"+str_mfg_bags_image+"', '"+str_partial_bags_image+"', '"+str_multiple_label_image+"', '"+inspection_id+"');"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery)
                    Log.e("insert query status","Data inserted successfully!")
                    runOnUiThread {
                        progressAlert.dismissAlert()
                        val intent = Intent(this@BulkBagBoxActivity, DataMatch::class.java)
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