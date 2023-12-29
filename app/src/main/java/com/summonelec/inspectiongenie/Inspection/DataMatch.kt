package com.summonelec.inspectiongenie.Inspection

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.summonelec.inspectiongenie.DB.DatabaseConnection
import com.summonelec.inspectiongenie.Helper.ProgressAlert
import com.summonelec.inspectiongenie.Helper.SharedHelper
import com.summonelec.inspectiongenie.R
import com.summonelec.inspectiongenie.databinding.ActivityDataMatchBinding
import com.summonelec.inspectiongenie.utilities.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.sql.SQLException

class DataMatch : AppCompatActivity() {
    lateinit var binding: ActivityDataMatchBinding
    private lateinit var progressAlert: ProgressAlert
    lateinit var imageUri : Uri
    private var bitmap: Bitmap? = null
    private var destination: File? = null
    private var imgPath: String? = null
    private var creds: BasicAWSCredentials = BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY)
    private var s3Client: AmazonS3Client = AmazonS3Client(creds)
    private var str_match_part_number_radio_group: String? = null
    private var str_match_slip_po_radio_group: String? = null
    private var str_damage_packing_radio_group: String? = null
    private var et_match_part_number: String? = null
    private var et_match_slip_po: String? = null
    private var et_damage_packing: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_match)
        binding = ActivityDataMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressAlert = ProgressAlert(this)
        binding.matchPartNumberRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
              str_match_part_number_radio_group = rb.text.toString()
            }
        }
        binding.matchSlipPoRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_match_slip_po_radio_group = rb.text.toString()
            }
        }
        binding.damagePackingRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val rb = radioGroup.findViewById<View>(i) as RadioButton
            if (null != rb && i > -1) {
                // Get the selected option
                str_damage_packing_radio_group = rb.text.toString()
            }
        }

        binding.btnNxt.setOnClickListener(View.OnClickListener {
          et_match_part_number = binding.etMatchPartNumber.text.toString()
            et_match_slip_po = binding.etMatchSlipPo.text.toString()
            et_damage_packing = binding.etDamagePacking.text.toString()
          insertDataIntoDatabase()
        })

    }
    fun insertDataIntoDatabase()
    {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    var statement = connection.createStatement()
                    val inspection_id = SharedHelper.getKey(this@DataMatch, "inspection_id")
                    val insertQuery = "INSERT INTO data_match (match_part_number_radio_group, match_slip_po_radio_group, damage_packing_radio_group, et_match_part_number, et_match_slip_po, et_damage_packing, inspection_id) VALUES ('"+str_match_part_number_radio_group+"', '"+str_match_slip_po_radio_group+"', '"+str_damage_packing_radio_group+"', '"+et_match_part_number+"', '"+et_match_slip_po+"', '"+et_damage_packing+"', '"+inspection_id+"');"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery)
                    Log.e("insert query status","Data inserted successfully!")
                    runOnUiThread {
                        progressAlert.dismissAlert()
                        val intent = Intent(this@DataMatch, ReportBox::class.java)
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