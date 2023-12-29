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
import com.bumptech.glide.Glide
import com.summonelec.inspectiongenie.DB.DatabaseConnection
import com.summonelec.inspectiongenie.Helper.ProgressAlert
import com.summonelec.inspectiongenie.Helper.SendEmailUtil
import com.summonelec.inspectiongenie.Helper.SharedHelper
import com.summonelec.inspectiongenie.R
import com.summonelec.inspectiongenie.ScannerActivity
import com.summonelec.inspectiongenie.databinding.ActivityReportReelBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.concurrent.thread

class ReportReel : AppCompatActivity() {
    lateinit var binding: ActivityReportReelBinding
    private lateinit var progressAlert: ProgressAlert
    var pdf_file_path: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_reel)
        binding = ActivityReportReelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressAlert = ProgressAlert(this)
        load_data()
        binding.Download.setOnClickListener(View.OnClickListener {
            showAllDOneAlert()
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
        val determine_parts0_layout   : LinearLayout = binding.determineParts0Pdf
        val determinep_part1_layout : LinearLayout = binding.determineParts1Pdf
        val cut_tape_reel2_pdf : LinearLayout = binding.cutTapeReel2Pdf
        val cut_tape_reel_pdf : LinearLayout = binding.cutTapeReelPdf
        val layoutViews: List<View> = listOf(package_receive_layout, weight_dimension_layout, condition_items_layout,manufature_label_layout,determine_parts0_layout,determinep_part1_layout,cut_tape_reel_pdf,cut_tape_reel2_pdf )

        val item_name = SharedHelper.getKey(this@ReportReel, "item_name")
        val po_number = SharedHelper.getKey(this@ReportReel, "po_number")
        val inspection_id = SharedHelper.getKey(this@ReportReel, "inspection_id")

        val pdfname = "$inspection_id-generated"
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
            val dialog = Dialog(this@ReportReel)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.alert_all_done)
            dialog.setCanceledOnTouchOutside(false)
            val dialogButton = dialog.findViewById<View>(R.id.btn_goHome) as Button
            dialogButton.setOnClickListener {
                val intent = Intent(this@ReportReel, ScannerActivity::class.java)
                startActivity(intent)
            }
            dialog.show()
        }
    }
    fun generatePdfFromView(context: Context, views: List<View>, pdfFileName: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(610, 1191, 1).create()  // A4 size: 595x842 points

        for (viewIndex in views.indices) {
            val page = pdfDocument.startPage(pageInfo)
            val view = views[viewIndex]

            val widthSpec = View.MeasureSpec.makeMeasureSpec(pageInfo.pageWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(pageInfo.pageHeight, View.MeasureSpec.EXACTLY)

            // Measure the view
            view.measure(widthSpec, heightSpec)

            // Layout the view
            view.layout(0, 0, pageInfo.pageWidth, pageInfo.pageHeight)

            // Create a bitmap and a canvas to draw the view
            val bitmap = Bitmap.createBitmap(pageInfo.pageWidth, pageInfo.pageHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw the background (if any)
            val backgroundDrawable = view.background
            if (backgroundDrawable != null) {
                backgroundDrawable.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }

            // Draw the view on the canvas
            view.draw(canvas)

            // Draw the bitmap on the page canvas
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)

            // Finish the page
            pdfDocument.finishPage(page)
        }

        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/$pdfFileName.pdf"
        val file = File(filePath)

        try {
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()
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
                    val inspection_id = SharedHelper.getKey(this@ReportReel, "inspection_id")
                    val query = "SELECT a.top_image, a.bottom_image, a.left_image, a.right_image, a.front_image, a.back_image, b.weigth, b.length, b.height, b.width, b.scale_image, b.mesaure_image, d.package_slip, dp.bag_seal, dp.type_radio, dp.puncher_bag_radio, dp.bag_pun2, dp.bag_overall_image, dp.msl_sticker, dp.open_box, dp.msl_verification, dp.hic_card, e.top_image AS c_top, e.bottom_image AS c_bottom, e.left_image AS c_left, e.right_image AS c_right, e.front_image AS c_front, e.back_image AS c_back, f.manufacture_label, ct.tape_reel_radio_group, ct.order_match_radio_group, ct.tape_leader_radio_group, ct.lead_tape_condition_radio_group, ct.part_orie_radio_group, ct.tape_damage_radio_group, ct.label_place_image, ct.label_place2_image, ct.lead_tape_condition_image, ct.tape_damage_image FROM package_receive a JOIN weight_dimension b ON a.inspection_id = b.inspection_id JOIN package_slip d ON a.inspection_id = d.inspection_id JOIN conditin_line_item e ON a.inspection_id = e.inspection_id JOIN manufacture_label f ON a.inspection_id = f.inspection_id JOIN determine_parts dp ON a.inspection_id = dp.inspection_id JOIN cut_tape_reel ct ON a.inspection_id = ct.inspection_id WHERE a.inspection_id = '" + inspection_id + "';"
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
                        val tapeReelRadioGroup: String = resultSet.getString("tape_reel_radio_group")
                        val orderMatchRadioGroup: String = resultSet.getString("order_match_radio_group")
                        val tapeLeaderRadioGroup: String = resultSet.getString("tape_leader_radio_group")
                        val leadTapeConditionRadioGroup: String = resultSet.getString("lead_tape_condition_radio_group")
                        val partOrieRadioGroup: String = resultSet.getString("part_orie_radio_group")
                        val tapeDamageRadioGroup: String = resultSet.getString("tape_damage_radio_group")
                        val labelPlaceImage: String = resultSet.getString("label_place_image")
                        val labelPlace2Image: String = resultSet.getString("label_place2_image")
                        val leadTapeConditionImage: String = resultSet.getString("lead_tape_condition_image")
                        val tapeDamageImage: String = resultSet.getString("tape_damage_image")
                        Log.e("data", "top_image:$top_image,bottom_image:$bottom_image")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            //pdf report

                            Glide.with(this@ReportReel).load(top_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivTopViewPdf)
                            Glide.with(this@ReportReel).load(bottom_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBottomViewPdf)
                            Glide.with(this@ReportReel).load(left_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivLeftViewPdf)
                            Glide.with(this@ReportReel).load(right_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivRightViewPdf)
                            Glide.with(this@ReportReel).load(front_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivFrontViewPdf)
                            Glide.with(this@ReportReel).load(back_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBackViewPdf)
                            binding.txtWeightPdf.setText(weigth)
                            binding.txtLengthPdf.setText(length)
                            binding.txtHeightPdf.setText(height)
                            binding.txtWidthPdf.setText(width)
                            Glide.with(this@ReportReel).load(scale_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivScaleviewPdf)
                            Glide.with(this@ReportReel).load(mesaure_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivMeasurePdf)
                            /*    Glide.with(this@ReportActivity).load(package_image)
                                    .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                    .into(binding.ivPackageInteroierPdf)*/
                            //  binding.txtPackageReportPdf.setText(package_has_report)
                            Glide.with(this@ReportReel).load(package_slip)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivPackageSlipPdf)
                            Glide.with(this@ReportReel).load(c_top)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvTopViewPdf)
                            Glide.with(this@ReportReel).load(c_bottom)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBottomViewPdf)
                            Glide.with(this@ReportReel).load(c_left)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvLeftViewPdf)
                            Glide.with(this@ReportReel).load(c_right)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvRightViewPdf)
                            Glide.with(this@ReportReel).load(c_front)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvFrontViewPdf)
                            Glide.with(this@ReportReel).load(c_back)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBackviewPdf)
                            Glide.with(this@ReportReel).load(manufacture_label)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivManufautreLabelPdf)
                            binding.puncturesBagRadioPdf.setText(puncher_bag_radio)
                            binding.punctureBagRadioPdf.setText(bag_pun2)
                            binding.typeRadioPdf.setText(type_radio)
                            binding.mslStickerPdf.setText(msl_sticker)
                            binding.digiKeyPdf.setText(msl_verification)
                            binding.hicCardPdf.setText(hic_card)
                            binding.tapeReelRadioGroupPdf.setText(tapeReelRadioGroup)
                            binding.orderMatchRadioGroupPdf.setText(orderMatchRadioGroup)
                            binding.tapeLeaderRadioGroupPdf.setText(tapeLeaderRadioGroup)
                            binding.leadTapeConditionRadioGroupPdf.setText(leadTapeConditionRadioGroup)
                            binding.partOrieRadioGroupPdf.setText(partOrieRadioGroup)
                            binding.tapeDamageRadioGroupPdf.setText(tapeDamageRadioGroup)
                            Glide.with(this@ReportReel).load(bag_seal)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBagSealImagePdf)
                            Glide.with(this@ReportReel).load(bag_overall_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.bagOverallImagePdf)
                            Glide.with(this@ReportReel).load(open_box)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.iiOpenBagPdf)
                            Glide.with(this@ReportReel).load(labelPlace2Image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.labelPlace2ImagePdf)
                            Glide.with(this@ReportReel).load(labelPlaceImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.labelPlaceImagePdf)
                        }

                    } else {
                        Log.e("Data message", "failed...")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            Toast.makeText(
                                this@ReportReel,
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