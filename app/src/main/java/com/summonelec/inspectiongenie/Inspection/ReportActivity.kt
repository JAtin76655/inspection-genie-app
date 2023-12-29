package com.summonelec.inspectiongenie.Inspection

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.summonelec.inspectiongenie.DB.DatabaseConnection
import com.summonelec.inspectiongenie.Helper.ProgressAlert
import com.summonelec.inspectiongenie.Helper.SendEmailUtil
import com.summonelec.inspectiongenie.Helper.SharedHelper
import com.summonelec.inspectiongenie.R
import com.summonelec.inspectiongenie.databinding.ActivityReportBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.concurrent.thread

class ReportActivity : AppCompatActivity() {
    lateinit var binding: ActivityReportBinding
    private lateinit var progressAlert: ProgressAlert
    var pdf_file_path: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backPageImageView: ImageView = findViewById(R.id.back_page)
        backPageImageView.setOnClickListener {
            // Perform the action you want when the ImageView is clicked
            onBackPressed()
        }


        progressAlert = ProgressAlert(this)
        load_data()
        binding.btnSendMail.setOnClickListener(View.OnClickListener {
            sendmail()
        })
    }
    fun sendmail()
    {
        progressAlert.showAlert()
        val package_receive_layout :LinearLayout = binding.packageReceiveLayout
        val weight_dimension_layout :LinearLayout = binding.weightDimensionLayout
        val condition_items_layout :LinearLayout = binding.conditionItemsLayout
        val manufature_label_layout :LinearLayout = binding.manufatureLabelLayout
        val layoutViews: List<View> = listOf(package_receive_layout, weight_dimension_layout, condition_items_layout,manufature_label_layout)

        val item_name = SharedHelper.getKey(this@ReportActivity, "item_name")
        val po_number = SharedHelper.getKey(this@ReportActivity, "po_number")
        val pdfname = "$po_number-$item_name generated"
        generatePdfFromView(this, layoutViews, pdfname)
        thread {
            val recipientEmail = arrayOf("support@summonelectronics.zohodesk.com","app@summonelec.com","jatin.s76655@gmail.com")
            val subject = "Inspection Report $po_number - $item_name with Attachment"
            val body = "Po Number : $po_number\n" +
                     "Item :$item_name"
            val attachmentPath = pdf_file_path
            val senderEmail = "iamshana2020@gmail.com"
            val senderPassword = "Shana@202020"

            SendEmailUtil.sendEmailWithAttachment(
                recipientEmail,
                subject,
                body,
                attachmentPath!!,
                senderEmail,
                senderPassword
            )
            progressAlert.dismissAlert()
            showAllDOneAlert()
        }
    }
    private fun showAllDOneAlert() {
        runOnUiThread {
            val dialog = Dialog(this@ReportActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.alert_all_done)
            dialog.setCanceledOnTouchOutside(false)
            val dialogButton = dialog.findViewById<View>(R.id.btn_goHome) as Button
            dialogButton.setOnClickListener {
                val intent = Intent(this@ReportActivity, HomeActivity::class.java)
                startActivity(intent)
            }
            dialog.show()
        }
    }
    fun generatePdfFromView(context: Context,views: List<View>, pdfFileName: String) {
        Log.e("Pdf","start")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()  // A4 size: 595x842 points


        for (viewIndex in 0 until views.size) {
            val page = pdfDocument.startPage(pageInfo)
            val view = views[viewIndex]
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                595,
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                842,
                View.MeasureSpec.EXACTLY
            )
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, 592, 842)
            val bitmap = Bitmap.createBitmap(
                592, 842,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            val backgroundDrawable = view.background
            if (backgroundDrawable != null) {
                backgroundDrawable.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
                view.draw(canvas)
            }

            // val bitmap = Bitmap.createBitmap(2500,4500, Bitmap.Config.ARGB_8888)
            // val canvas = Canvas(bitmap)
            canvas.drawColor(ContextCompat.getColor(context, android.R.color.white))
            view.draw(canvas)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)

            pdfDocument.finishPage(page)
        }


        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/$pdfFileName.pdf"
        val file = File(filePath)

        val pdfFile = file
        pdf_file_path = pdfFile.absolutePath
        Log.e("Pdf path",":"+pdfFile)
        try {
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        pdfDocument.close()
    }
    fun load_data() {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    val inspection_id = SharedHelper.getKey(this@ReportActivity, "inspection_id")
                    val query = "SELECT\n" +
                            "    a.top_image,\n" +
                            "    a.bottom_image,\n" +
                            "    a.left_image,\n" +
                            "    a.right_image,\n" +
                            "    a.front_image,\n" +
                            "    a.back_image,\n" +
                            "    b.weigth,\n" +
                            "    b.length,\n" +
                            "    b.height,\n" +
                            "    b.width,\n" +
                            "    b.scale_image,\n" +
                            "    b.mesaure_image,\n" +
                            "    d.package_slip,\n" +
                            "    dp.bag_seal,\n" +
                            "    dp.type_radio,\n" +
                            "    dp.puncher_bag_radio,\n" +
                            "    dp.bag_pun2,\n" +
                            "    dp.bag_overall_image,\n" +
                            "    dp.msl_sticker,\n" +
                            "    dp.open_box,\n" +
                            "    dp.msl_verification,\n" +
                            "    dp.hic_card,\n" +
                            "    e.top_image as c_top,\n" +
                            "    e.bottom_image as c_bottom,\n" +
                            "    e.left_image as c_left,\n" +
                            "    e.right_image as c_right,\n" +
                            "    e.front_image as c_front,\n" +
                            "    e.back_image as c_back,\n" +
                            "    f.manufacture_label\n" +
                            "FROM\n" +
                            "    package_receive a\n" +
                            "JOIN\n" +
                            "    weight_dimension b ON a.inspection_id = b.inspection_id\n" +
                            "JOIN\n" +
                            "    package_slip d ON a.inspection_id = d.inspection_id\n" +
                            "JOIN\n" +
                            "    conditin_line_item e ON a.inspection_id = e.inspection_id\n" +
                            "JOIN\n" +
                            "    manufacture_label f ON a.inspection_id = f.inspection_id\n" +
                            "JOIN\n" +
                            "    determine_parts dp ON a.inspection_id = dp.inspection_id  \n" +
                            "WHERE\n" +
                            "    a.inspection_id = $inspection_id;\n"
                    Log.e("report query", query)
                    val statement = connection.createStatement()
                    val resultSet: ResultSet = statement.executeQuery(query)
                    val rowCount = if (resultSet.next()) resultSet.getString(1) else 0
                    if (rowCount != 0) {
                        resultSet.first()
                        val top_image = resultSet.getString("top_image")
                        val bottom_image = resultSet.getString("bottom_image")
                        val left_image = resultSet.getString("left_image")
                        val right_image = resultSet.getString("right_image")
                        val front_image = resultSet.getString("front_image")
                        val back_image = resultSet.getString("back_image")
                        val weigth = resultSet.getString("weigth")
                        val length = resultSet.getString("length")
                        val height = resultSet.getString("height")
                        val width = resultSet.getString("width")
                        val scale_image = resultSet.getString("scale_image")
                        val mesaure_image = resultSet.getString("mesaure_image")
                        //val package_image = resultSet.getString("package_image")
                       // val package_has_report = resultSet.getString("package_has_report")
                        val package_slip = resultSet.getString("package_slip")
                        val c_top = resultSet.getString("c_top")
                        val c_bottom = resultSet.getString("c_bottom")
                        val c_left = resultSet.getString("c_left")
                        val c_right = resultSet.getString("c_right")
                        val c_front = resultSet.getString("c_front")
                        val c_back = resultSet.getString("c_back")
                        val manufacture_label = resultSet.getString("manufacture_label")
                        val bag_seal = resultSet.getString("bag_seal")
                        val bag_overall_image = resultSet.getString("bag_overall_image")
                        val open_box = resultSet.getString("open_box")
                        val type_radio = resultSet.getString("type_radio")
                        val puncher_bag_radio = resultSet.getString("puncher_bag_radio")
                        val bag_pun2 = resultSet.getString("bag_pun2")
                        val msl_sticker = resultSet.getString("msl_sticker")
                        val msl_verification = resultSet.getString("msl_verification")
                        val hic_card = resultSet.getString("hic_card")


                        Log.e("data","top_image:$top_image,bottom_image:$bottom_image")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            Glide.with(this@ReportActivity).load(top_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivTopView)
                            Glide.with(this@ReportActivity).load(bottom_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBottomView)
                            Glide.with(this@ReportActivity).load(left_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivLeftView)
                            Glide.with(this@ReportActivity).load(right_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivRightView)
                            Glide.with(this@ReportActivity).load(front_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivFrontView)
                            Glide.with(this@ReportActivity).load(back_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBackview)
                            binding.txtWeight.setText(weigth)
                            binding.txtLength.setText(length)
                            binding.txtHeight.setText(height)
                            binding.txtWidth.setText(width)
                            Glide.with(this@ReportActivity).load(scale_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivScaleview)
                            Glide.with(this@ReportActivity).load(mesaure_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivMeasure)
                    /*        Glide.with(this@ReportActivity).load(package_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivPackageInteroier) */
                        //    binding.txtPackageReport.setText(package_has_report)
                            Glide.with(this@ReportActivity).load(package_slip)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivPackageSlip)
                            Glide.with(this@ReportActivity).load(c_top)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvTopView)
                            Glide.with(this@ReportActivity).load(c_bottom)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBottomView)
                            Glide.with(this@ReportActivity).load(c_left)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvLeftView)
                            Glide.with(this@ReportActivity).load(c_right)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvRightView)
                            Glide.with(this@ReportActivity).load(c_front)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvFrontView)
                            Glide.with(this@ReportActivity).load(c_back)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBackview)
                            Glide.with(this@ReportActivity).load(manufacture_label)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivManufautreLabel)
                            binding.puncturesBagRadio.setText(puncher_bag_radio)
                            binding.punctureBagRadio.setText(bag_pun2)
                            binding.typeRadio.setText(type_radio)
                            binding.mslSticker.setText(msl_sticker)
                            binding.digiKey.setText(msl_verification)
                            binding.hicCard.setText(hic_card)
                            Glide.with(this@ReportActivity).load(bag_seal)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBagSealImage)
                            Glide.with(this@ReportActivity).load(bag_overall_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.bagOverallImage)
                            Glide.with(this@ReportActivity).load(open_box)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.iiOpenBag)


                            //pdf report

                            Glide.with(this@ReportActivity).load(top_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivTopViewPdf)
                            Glide.with(this@ReportActivity).load(bottom_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBottomViewPdf)
                            Glide.with(this@ReportActivity).load(left_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivLeftViewPdf)
                            Glide.with(this@ReportActivity).load(right_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivRightViewPdf)
                            Glide.with(this@ReportActivity).load(front_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivFrontViewPdf)
                            Glide.with(this@ReportActivity).load(back_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBackViewPdf)
                            binding.txtWeightPdf.setText(weigth)
                            binding.txtLengthPdf.setText(length)
                            binding.txtHeightPdf.setText(height)
                            binding.txtWidthPdf.setText(width)
                            Glide.with(this@ReportActivity).load(scale_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivScaleviewPdf)
                            Glide.with(this@ReportActivity).load(mesaure_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivMeasurePdf)
                        /*    Glide.with(this@ReportActivity).load(package_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivPackageInteroierPdf)*/
                          //  binding.txtPackageReportPdf.setText(package_has_report)
                            Glide.with(this@ReportActivity).load(package_slip)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivPackageSlipPdf)
                            Glide.with(this@ReportActivity).load(c_top)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvTopViewPdf)
                            Glide.with(this@ReportActivity).load(c_bottom)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBottomViewPdf)
                            Glide.with(this@ReportActivity).load(c_left)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvLeftViewPdf)
                            Glide.with(this@ReportActivity).load(c_right)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvRightViewPdf)
                            Glide.with(this@ReportActivity).load(c_front)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvFrontViewPdf)
                            Glide.with(this@ReportActivity).load(c_back)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBackviewPdf)
                            Glide.with(this@ReportActivity).load(manufacture_label)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivManufautreLabelPdf)
                            binding.puncturesBagRadio.setText(puncher_bag_radio)
                            binding.punctureBagRadio.setText(bag_pun2)
                            binding.typeRadio.setText(type_radio)
                            binding.mslSticker.setText(msl_sticker)
                            binding.digiKey.setText(msl_verification)
                            binding.hicCard.setText(hic_card)
                            Glide.with(this@ReportActivity).load(bag_seal)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBagSealImagePdf)
                            Glide.with(this@ReportActivity).load(bag_overall_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.bagOverallImagePdf)
                            Glide.with(this@ReportActivity).load(open_box)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.iiOpenBagPdf)
                        }

                    } else {
                        Log.e("Data message", "failed...")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            Toast.makeText(this@ReportActivity, "Data load Failed...", Toast.LENGTH_SHORT).show()
                        }


                    }

                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e("SQL Exception", e.message ?: "Unknown SQL Exception")
                }

                it.close() // Don't forget to close the connection when done
            }
        }
//        val apiInterface = ApiClientClass.getInstance().create(ApiInterface::class.java)
//        //val call = apiInterface.loginUser("user_login", email, password)
//        GlobalScope.launch {
//            val result = apiInterface.loginUser(email,password)
//            if (result.isSuccessful){
//                progressAlert.dismissAlert()
//                Log.d("server login data: ", result.body().toString())
//                putKey(this@LoginActivity, "USERID", result.body()?.userId.toString())
//                putKey(this@LoginActivity, "login", "yes")
//                putKey(this@LoginActivity, "user_name", result.body()?.name)
//                putKey(this@LoginActivity, "user_email", result.body()?.email)
//                putKey(this@LoginActivity, "user_image", result.body()?.user_image)
//                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
//                finish()
//            }
//            else
//            {
//                progressAlert.dismissAlert()
//                Log.e("login error",result.errorBody().toString())
//            }
        // Checking the results

    }
}