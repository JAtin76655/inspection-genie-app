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
import com.summonelec.inspectiongenie.databinding.ActivityReportBoxBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.concurrent.thread

class ReportBox : AppCompatActivity() {
    lateinit var binding: ActivityReportBoxBinding
    private lateinit var progressAlert: ProgressAlert
    var pdf_file_path: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_box)
        binding = ActivityReportBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressAlert = ProgressAlert(this)
        load_data()
        binding.Download.setOnClickListener(View.OnClickListener {
            sendmail()
           showAllDOneAlert()
        })
    }
    fun sendmail()
    {
        progressAlert.showAlert()
        val package_receive_layout : LinearLayout = binding.packageReceiveLayout
        val weight_dimension_layout : LinearLayout = binding.weightDimensionLayout
        val condition_items_layout : LinearLayout = binding.conditionItemsLayout
        val manufature_label_layout : LinearLayout = binding.manufatureLabelLayout
        val determinep_part_layout : LinearLayout = binding.determinePartsPdf
        val determine_part2_layout : LinearLayout = binding.determinePart2Layout
        val report_box : LinearLayout = binding.reportBox
        val report_boxo : LinearLayout = binding.reportBoxo
        val layoutViews: List<View> = listOf(package_receive_layout, weight_dimension_layout, condition_items_layout,manufature_label_layout, determinep_part_layout,determine_part2_layout, report_box,report_boxo)

        val item_name = SharedHelper.getKey(this@ReportBox, "item_name")
        val po_number = SharedHelper.getKey(this@ReportBox, "po_number")
        val inspection_id = SharedHelper.getKey(this@ReportBox, "inspection_id")

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
            val dialog = Dialog(this@ReportBox)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.alert_all_done)
            dialog.setCanceledOnTouchOutside(false)
            val dialogButton = dialog.findViewById<View>(R.id.btn_goHome) as Button
            dialogButton.setOnClickListener {
                val intent = Intent(this@ReportBox, ScannerActivity::class.java)
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
                    val inspection_id = SharedHelper.getKey(this@ReportBox, "inspection_id")
                    val query = "SELECT a.top_image, a.bottom_image, a.left_image, a.right_image, a.front_image, a.back_image, b.weigth, b.length, b.height, b.width, b.scale_image, b.mesaure_image, d.package_slip, dp.bag_seal, dp.type_radio, dp.puncher_bag_radio, dp.bag_pun2, dp.bag_overall_image, dp.msl_sticker, dp.open_box, dp.msl_verification, dp.hic_card, e.top_image AS c_top, e.bottom_image AS c_bottom, e.left_image AS c_left, e.right_image AS c_right, e.front_image AS c_front, e.back_image AS c_back, f.manufacture_label, bi.bag_parts_radio_group, bi.factory_label_radio_group, bi.manu_packing_slip_radio_group, bi.multiple_bag_radio_group, bi.et_bag_parts_inside, bi.et_mfg_bags, bi.et_partial_bag, bi.bag_parts_image, bi.factory_label_image, bi.manu_packing_slip_image, bi.mfg_bags_image, bi.partial_bags_image, bi.multiple_label_image FROM package_receive a JOIN weight_dimension b ON a.inspection_id = b.inspection_id JOIN package_slip d ON a.inspection_id = d.inspection_id JOIN conditin_line_item e ON a.inspection_id = e.inspection_id JOIN manufacture_label f ON a.inspection_id = f.inspection_id JOIN determine_parts dp ON a.inspection_id = dp.inspection_id JOIN bag_inspection bi ON a.inspection_id = bi.inspection_id WHERE a.inspection_id ='" + inspection_id + "';"
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
                        val bagPartsRadioGroup = resultSet.getString("bag_parts_radio_group");
                        val factoryLabelRadioGroup = resultSet.getString("factory_label_radio_group");
                        val manuPackingSlipRadioGroup = resultSet.getString("manu_packing_slip_radio_group");
                        val multipleBagRadioGroup = resultSet.getString("multiple_bag_radio_group");
                        val etBagPartsInside = resultSet.getString("et_bag_parts_inside");
                        val etMfgBags = resultSet.getString("et_mfg_bags");
                        val etPartialBag = resultSet.getString("et_partial_bag");
                        val bagPartsImage = resultSet.getString("bag_parts_image");
                        val factoryLabelImage = resultSet.getString("factory_label_image");
                        val manuPackingSlipImage = resultSet.getString("manu_packing_slip_image");
                        val mfgBagsImage = resultSet.getString("mfg_bags_image");
                        val partialBagsImage = resultSet.getString("partial_bags_image");
                        val multipleLabelImage = resultSet.getString("multiple_label_image");



                        Log.e("data", "top_image:$top_image,bottom_image:$bottom_image")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            //pdf report

                            Glide.with(this@ReportBox).load(top_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivTopViewPdf)
                            Glide.with(this@ReportBox).load(bottom_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBottomViewPdf)
                            Glide.with(this@ReportBox).load(left_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivLeftViewPdf)
                            Glide.with(this@ReportBox).load(right_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivRightViewPdf)
                            Glide.with(this@ReportBox).load(front_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivFrontViewPdf)
                            Glide.with(this@ReportBox).load(back_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBackViewPdf)
                            binding.txtWeightPdf.setText(weigth)
                            binding.txtLengthPdf.setText(length)
                            binding.txtHeightPdf.setText(height)
                            binding.txtWidthPdf.setText(width)
                            Glide.with(this@ReportBox).load(scale_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivScaleviewPdf)
                            Glide.with(this@ReportBox).load(mesaure_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivMeasurePdf)
                            /*    Glide.with(this@ReportActivity).load(package_image)
                                    .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                    .into(binding.ivPackageInteroierPdf)*/
                            //  binding.txtPackageReportPdf.setText(package_has_report)
                            Glide.with(this@ReportBox).load(package_slip)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivPackageSlipPdf)
                            Glide.with(this@ReportBox).load(c_top)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvTopViewPdf)
                            Glide.with(this@ReportBox).load(c_bottom)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBottomViewPdf)
                            Glide.with(this@ReportBox).load(c_left)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvLeftViewPdf)
                            Glide.with(this@ReportBox).load(c_right)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvRightViewPdf)
                            Glide.with(this@ReportBox).load(c_front)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvFrontViewPdf)
                            Glide.with(this@ReportBox).load(c_back)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.cdIvBackviewPdf)
                            Glide.with(this@ReportBox).load(manufacture_label)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivManufautreLabelPdf)
                            binding.puncturesBagRadioPdf.setText(puncher_bag_radio)
                            binding.punctureBagRadioPdf.setText(bag_pun2)
                            binding.typeRadioPdf.setText(type_radio)
                            binding.mslStickerPdf.setText(msl_sticker)
                            binding.digiKeyPdf.setText(msl_verification)
                            binding.hicCardPdf.setText(hic_card)
                            binding.bagPartsRadioGroupPdf.setText(bagPartsRadioGroup)
                            binding.factoryLabelRadioGroupPdf.setText(factoryLabelRadioGroup)
                            binding.manuPackingSlipRadioGroupPdf.setText(manuPackingSlipRadioGroup)
                            binding.etMfgBagsPdf.setText(etMfgBags)
                            Glide.with(this@ReportBox).load(bag_seal)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.ivBagSealImagePdf)
                            Glide.with(this@ReportBox).load(bag_overall_image)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.bagOverallImagePdf)
                            Glide.with(this@ReportBox).load(open_box)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.iiOpenBagPdf)
                            Glide.with(this@ReportBox).load(bagPartsImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.bagPartsImagePdf)
                            Glide.with(this@ReportBox).load(factoryLabelImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.factoryLabelImagePdf)
                            Glide.with(this@ReportBox).load(manuPackingSlipImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.manuPackingSlipImagePdf)
                            Glide.with(this@ReportBox).load(mfgBagsImage)
                                .placeholder(R.drawable.ic_add).error(R.drawable.ic_add)
                                .into(binding.mfgBagsImagePdf)
                        }

                    } else {
                        Log.e("Data message", "failed...")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            Toast.makeText(
                                this@ReportBox,
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