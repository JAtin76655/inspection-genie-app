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
import com.summonelec.inspectiongenie.ScannerActivity
import com.summonelec.inspectiongenie.databinding.ActivityTubeReportBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.concurrent.thread

class Tube_report : AppCompatActivity() {
    lateinit var binding: ActivityTubeReportBinding
    private lateinit var progressAlert: ProgressAlert
    var pdf_file_path: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTubeReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressAlert = ProgressAlert(this)
        load_data()

        binding.Download.setOnClickListener(View.OnClickListener {
            sendmail()
        })

    }

    fun sendmail()
    {
        progressAlert.showAlert()
        val package_receive_layout : LinearLayout = binding.packageReceiveLayout
        val weight_dimension_layout : LinearLayout = binding.weightDimensionLayout
        val condition_items_layout : LinearLayout = binding.conditionItemsLayout
        val manufature_label_layout : LinearLayout = binding.manufatureLabelLayout
        val determinep_part_layout : LinearLayout = binding.determineParts1Pdf
        val determine_part2_pdf : LinearLayout = binding.determineParts2Pdf
        val report_tube : LinearLayout = binding.reportTube
        val report_tube2 : LinearLayout = binding.reportTube2
        val layoutViews: List<View> = listOf(package_receive_layout, weight_dimension_layout, condition_items_layout,manufature_label_layout,determinep_part_layout,determine_part2_pdf, report_tube, report_tube2)

        val item_name = SharedHelper.getKey(this@Tube_report, "item_name")
        val po_number = SharedHelper.getKey(this@Tube_report, "po_number")
        val inspection_id = SharedHelper.getKey(this@Tube_report, "inspection_id")

        val pdfname = "$inspection_id-generated"
        generatePdfFromView(this, layoutViews, pdfname)
        thread {
            val recipientEmail = arrayOf("support@summonelectronics.zohodesk.com","app@summonelec.com","jatin.s76655@gmail.com")
            val subject = "Inspection Report $po_number - $item_name with Attachment"
            val body = "Po Number : $po_number\n"
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
            val dialog = Dialog(this@Tube_report)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.alert_all_done)
            dialog.setCanceledOnTouchOutside(false)
            val dialogButton = dialog.findViewById<View>(R.id.btn_goHome) as Button
            dialogButton.setOnClickListener {
                finish()
                val intent = Intent(this@Tube_report, ScannerActivity::class.java)
                startActivity(intent)
            }
            dialog.show()
        }
    }

    fun generatePdfFromView(context: Context, views: List<View>, pdfFileName: String) {
        Log.e("Pdf","start")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(610, 1191, 1).create()  // A4 size: 595x842 points


        for (viewIndex in 0 until views.size) {
            val page = pdfDocument.startPage(pageInfo)
            val view = views[viewIndex]
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                610,
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                1191,
                View.MeasureSpec.EXACTLY
            )
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, 610, 1191)
            val bitmap = Bitmap.createBitmap(
                610, 1191,
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
                    var inspection_id = SharedHelper.getKey(this@Tube_report, "inspection_id")
                    val query = "SELECT a.top_image, a.bottom_image, a.left_image, a.right_image, a.front_image, a.back_image, b.weigth, b.length, b.height, b.width, b.scale_image, b.mesaure_image, d.package_slip, dp.bag_seal, dp.type_radio, dp.puncher_bag_radio, dp.bag_pun2, dp.bag_overall_image, dp.msl_sticker, dp.open_box, dp.msl_verification, dp.hic_card, e.top_image AS c_top, e.bottom_image AS c_bottom, e.left_image AS c_left, e.right_image AS c_right, e.front_image AS c_front, e.back_image AS c_back, f.manufacture_label, t.logo_tube_radio_group, t.tube_length_radio_group, t.issue_tube_radio_group,t.direction_tube_radio_group, t.quantity_tubes, t.logo_tube_image, t.tube_length_image, t.issue_tube_image, t.direction_tube_image FROM package_receive a JOIN weight_dimension b ON a.inspection_id = b.inspection_id JOIN package_slip d ON a.inspection_id = d.inspection_id JOIN conditin_line_item e ON a.inspection_id = e.inspection_id JOIN manufacture_label f ON a.inspection_id = f.inspection_id JOIN determine_parts dp ON a.inspection_id = dp.inspection_id JOIN tube_inspection t ON a.inspection_id = t.inspection_id WHERE a.inspection_id ='" + inspection_id + "';"
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
                        val logoTubeRadioGroup = resultSet.getString("logo_tube_radio_group");
                        val tubeLengthRadioGroup = resultSet.getString("tube_length_radio_group");
                        val issueTubeRadioGroup = resultSet.getString("issue_tube_radio_group");
                        val directionTubeRadioGroup = resultSet.getString("direction_tube_radio_group");
                        val quantityTubes = resultSet.getString("quantity_tubes");
                        val logoTubeImage = resultSet.getString("logo_tube_image");
                        val tubeLengthImage = resultSet.getString("tube_length_image");
                        val issueTubeImage = resultSet.getString("issue_tube_image");
                        val directionTubeImage = resultSet.getString("direction_tube_image");

                        Log.e("data", "top_image:$top_image,bottom_image:$bottom_image")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            //pdf report


                            Glide.with(this@Tube_report).load(top_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivTopViewPdf)
                            Glide.with(this@Tube_report).load(bottom_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBottomViewPdf)
                            Glide.with(this@Tube_report).load(left_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivLeftViewPdf)
                            Glide.with(this@Tube_report).load(right_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivRightViewPdf)
                            Glide.with(this@Tube_report).load(front_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivFrontViewPdf)
                            Glide.with(this@Tube_report).load(back_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBackViewPdf)
                            binding.txtWeightPdf.setText(weigth)
                            binding.txtLengthPdf.setText(length)
                            binding.txtHeightPdf.setText(height)
                            binding.txtWidthPdf.setText(width)
                            Glide.with(this@Tube_report).load(scale_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivScaleviewPdf)
                            Glide.with(this@Tube_report).load(mesaure_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivMeasurePdf)
                            /*    Glide.with(this@ReportActivity).load(package_image)
                                    .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                    .into(binding.ivPackageInteroierPdf)*/
                            //  binding.txtPackageReportPdf.setText(package_has_report)
                            Glide.with(this@Tube_report).load(package_slip)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivPackageSlipPdf)
                            Glide.with(this@Tube_report).load(c_top)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvTopViewPdf)
                            Glide.with(this@Tube_report).load(c_bottom)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBottomViewPdf)
                            Glide.with(this@Tube_report).load(c_left)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvLeftViewPdf)
                            Glide.with(this@Tube_report).load(c_right)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvRightViewPdf)
                            Glide.with(this@Tube_report).load(c_front)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvFrontViewPdf)
                            Glide.with(this@Tube_report).load(c_back)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBackviewPdf)
                            Glide.with(this@Tube_report).load(manufacture_label)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivManufautreLabelPdf)
                            binding.puncturesBagRadioPdf.setText(puncher_bag_radio)
                            binding.punctureBagRadioPdf.setText(bag_pun2)
                            binding.typeRadioPdf.setText(type_radio)
                            binding.mslStickerPdf.setText(msl_sticker)
                            binding.digiKeyPdf.setText(msl_verification)
                            binding.hicCardPdf.setText(hic_card)
                            binding.tubeLengthRadioGroupPdf.setText(tubeLengthRadioGroup)
                            binding.issueTubeRadioGroupPdf.setText(issueTubeRadioGroup)
                            binding.logoTubeRadioGroupPdf.setText(logoTubeRadioGroup)
                            binding.driectionTubeRadioPdf.setText(directionTubeRadioGroup)
                            binding.quantityTubesRadioGroupPdf.setText(quantityTubes)
                            //    binding.tapeDamageRadioGroupPdf.setText(tapeDamageRadioGroup)
                            Glide.with(this@Tube_report).load(bag_seal)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBagSealImagePdf)
                            Glide.with(this@Tube_report).load(bag_overall_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.bagOverallImagePdf)
                            Glide.with(this@Tube_report).load(open_box)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.iiOpenBagPdf)
                            Glide.with(this@Tube_report).load(logoTubeImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.logoTubeImagPdf)
                            Glide.with(this@Tube_report).load(tubeLengthImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.tubeLengthImagePdf)
                            Glide.with(this@Tube_report).load(issueTubeImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.issueTubeImagePdf)
                            Glide.with(this@Tube_report).load(directionTubeImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.directionTubeImagePdf)
                        }

                    } else {
                        Log.e("Data message", "failed...")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            Toast.makeText(
                                this@Tube_report,
                                "Data load Failed...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e("SQL Exception", e.message ?: "Unknown SQL Exception")
                }

                it.close() // Don't forget to close the connection when done
            }
        }

    }
}