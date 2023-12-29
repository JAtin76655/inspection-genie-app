package com.summonelec.inspectiongenie

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.navigation.NavigationView
import com.mysql.jdbc.Statement
import com.summonelec.inspectiongenie.API.APIInterface.POJO.ListItem
import com.summonelec.inspectiongenie.Adapter.ItemAdapter
import com.summonelec.inspectiongenie.DB.DatabaseConnection
import com.summonelec.inspectiongenie.Helper.CellClickListener
import com.summonelec.inspectiongenie.Helper.ProgressAlert
import com.summonelec.inspectiongenie.Helper.SharedHelper
import com.summonelec.inspectiongenie.Inspection.Previous_inspections
import com.summonelec.inspectiongenie.Inspection.WeightAndDiemensActivity
import com.summonelec.inspectiongenie.UserLogin.LoginActivity
import com.summonelec.inspectiongenie.databinding.ActivityScannerBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import kotlin.random.Random

class ScannerActivity : AppCompatActivity(), CellClickListener {
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private lateinit var binding: ActivityScannerBinding
    private lateinit var progressAlert: ProgressAlert
    var cameraSource_status = false
    var po_number = "0"
    var po_item_id = "0"
    var dynamicItemList = mutableListOf<ListItem>()
    lateinit var drawerLayout2: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var toolbar: Toolbar

    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        drawerLayout2 = findViewById(R.id.drawer_layoutt)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout2, R.string.nav_open, R.string.nav_close)

        drawerLayout2.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // to make the Navigation drawer icon always appear on the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.inflateMenu(R.menu.drawer_menu)

        progressAlert = ProgressAlert(this)
        binding.scannerLayout.visibility = View.GONE
        binding.manualSearchLayout.visibility = View.GONE
        binding.poDetailsLayout.visibility = View.GONE
        val method:String = intent.getStringExtra("method").toString()
        if (method.equals("barcode"))
        {
            cameraSource_status = true
            binding.scannerLayout.visibility = View.VISIBLE
            binding.manualSearchLayout.visibility = View.GONE
            if (ContextCompat.checkSelfPermission(
                    this@ScannerActivity, android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askForCameraPermission()
            } else {
                setupControls()
            }

            val aniSlide: Animation =
                AnimationUtils.loadAnimation(this@ScannerActivity, R.anim.scanner_animation)
            binding.barcodeLine.startAnimation(aniSlide)
        }
        else
        {
            binding.scannerLayout.visibility = View.GONE
            binding.manualSearchLayout.visibility = View.VISIBLE
        }
        /**  binding.backBtn.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
        binding.manualSearch.setOnClickListener(View.OnClickListener {
            val data = binding.manualSearchData.text.toString()
           var data1 = binding.manualSearchData2.text.toString()
            if (data.equals(""))
            {
                binding.manualSearchData.error = "Please enter vaild data"
            }
            else
            {
                getPo(data,data)

            }
        }) **/
        binding.manualSearch.setOnClickListener(View.OnClickListener {
            val data = binding.manualSearchData.text.toString()
            var data1 = binding.manualSearchData2.text.toString()

            if (data.isEmpty() && data1.isEmpty()) {
                binding.manualSearchData.error = "Please enter valid data"
                binding.manualSearchData2.error = "Please enter valid data"
            } else {
                binding.manualSearchData.error = null
                binding.manualSearchData2.error = null

                // At least one of data or data1 is not empty, proceed with your logic
                getPo(data, data1)
            }
        })
        // Item select based on PO
       /* binding.startIns.setOnClickListener(View.OnClickListener
        {

            if (po_item_id.equals("0"))
            {
                Toast.makeText(this@ScannerActivity, "Please select anyone item", Toast.LENGTH_SHORT).show()
            }
            else
            {
              //  insertDataIntoDatabase(po_number,po_item_id)
            }

        })*/

        binding.startIns.setOnClickListener(View.OnClickListener {
            if (po_number.isEmpty()) {
                Toast.makeText(this@ScannerActivity, "Please select a valid PO number", Toast.LENGTH_SHORT).show()
            } else {
                // Directly start the inspection without requiring item selection
                // Set po_item_id same as po_number
                po_item_id = po_number + "98"
                insertDataIntoDatabase(po_number, po_item_id)
            }
        })


    }
  /*  fun setUpToolBar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Inspection genie"
    }*/


    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        binding.cameraSurfaceView.getHolder().addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "Scanner has been closed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue


                    //Don't forget to add this line printing value or finishing activity must run on main thread
                    runOnUiThread {
                        cameraSource.stop()
                        getPo(scannedValue,scannedValue)
                        Toast.makeText(this@ScannerActivity, "value- $scannedValue", Toast.LENGTH_SHORT).show()
                        //finish()
                    }
                }else
                {
                   // Toast.makeText(this@ScannerActivity, "value- else", Toast.LENGTH_SHORT).show()

                }
            }
        })
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@ScannerActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraSource_status)
        {
            cameraSource.stop()
        }

    }
   /* fun getPo(po: String?, track: String?) {
        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    val query = "SELECT * FROM purchase_master WHERE po_number='" + po + "' OR tracking_number='" + track + "' "
                    Log.e("purchase_master query", query)
                    val statement = connection.createStatement()
                    val resultSet: ResultSet = statement.executeQuery(query)
                    val rowCount = if (resultSet.next()) resultSet.getInt(1) else 0
                    Log.e("Data count:",""+rowCount)
                    if (rowCount != 0) {
                        var po_date = "0"
                        var reference_number = "0"
                        var status = "0"
                        var tracking_number = "0"
                        resultSet.first()
                        po_number = resultSet.getString("po_number")
                        tracking_number = resultSet.getString("tracking_number")
                        po_date = resultSet.getString("po_date")
                        reference_number = resultSet.getString("reference_number")
                        status = resultSet.getString("status")
                        Log.e("Data","po_number: $po_number, po_date: $po_date, reference_number: $reference_number, status: $status")

                        runOnUiThread {
                            progressAlert.dismissAlert()
                            binding.poDetailsLayout.visibility = View.VISIBLE
                            binding.poNumber.setText(po_number+" | "+reference_number+" | "+tracking_number)
                            binding.poDate.setText(po_date)
                            binding.poStatus.setText(status)
                            getPoItemList(po_number)

                        }

                    } else {
                        Log.e("purchase message", "failed...")
                        runOnUiThread {
                            progressAlert.dismissAlert()
                            Toast.makeText(this@ScannerActivity, "No data found at "+po, Toast.LENGTH_SHORT).show()

                        }


                    }

                } catch (e: SQLException) {
                    e.printStackTrace()
                }

                it.close() // Don't forget to close the connection when done
            }

        }


    } */

    fun getPo(po: String?, track: String?) {
        // Assuming progressAlert is declared and initialized somewhere in your class
        progressAlert.showAlert()

        // Directly use the provided PO number for saving
        runOnUiThread {
            binding.poDetailsLayout.visibility = View.VISIBLE
            binding.poNumber.text = po  // Display the provided PO number
          //  getPoItemList(po)
            if (po != null) {
                po_number = po
            }
            progressAlert.dismissAlert()
        }
    }


    fun getPoItemList(po: String?) {
        progressAlert.showAlert()

        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    val query = "SELECT * FROM purchase_master_item WHERE po_number ='" + po + "'"
                    Log.e("purchase_master item query", query)
                    val statement = connection.createStatement()
                    val resultSet: ResultSet = statement.executeQuery(query)
                        var item_name = "0"
                        var item_desc = "0"
                        var item_qty = "0"
                        var item_purchase_id = "0"

                        while (resultSet.next())
                        {
                            item_purchase_id = resultSet.getString("item_purchase_id")
                            item_name = resultSet.getString("item_name")
                            item_desc = resultSet.getString("item_desc")
                            item_qty = resultSet.getString("item_qty")
                            Log.e("Data","item_name: $item_name, item_desc: $item_desc, item_qty: $item_qty")
                            dynamicItemList.add(ListItem(item_purchase_id,item_name, item_desc,item_qty))
                        }



                        runOnUiThread {
                            progressAlert.dismissAlert()
                            binding.itemListView.adapter = ItemAdapter(this@ScannerActivity,dynamicItemList,this@ScannerActivity)

                        }


                } catch (e: SQLException) {
                    e.printStackTrace()
                }

                it.close() // Don't forget to close the connection when done
            }

        }


    }

    override fun onCellClickListener(position: Int) {
        if(!po_item_id.equals("0"))
        {
            Toast.makeText(this@ScannerActivity, "you can select one item", Toast.LENGTH_SHORT).show()
        }
        else
        {
            po_item_id = dynamicItemList.get(position).item_purchase_id
            SharedHelper.putKey(this@ScannerActivity, "item_name", "" + dynamicItemList.get(position).item_name)
            SharedHelper.putKey(this@ScannerActivity, "po_number", "" + po_number)
        }

    }

    override fun onUpdatedCellListener() {
            po_item_id = "0"
    }
    /* fun insertDataIntoDatabase(po_number:String,po_item_id:String)
    {

        progressAlert.showAlert()
        GlobalScope.launch {
            val connection = DatabaseConnection.connectToDatabase()
            connection?.let {
                try {
                    var generatedId = 0
                    var statement = connection.createStatement()
                    val insertQuery = "INSERT INTO inspection (po_number,po_item_id) VALUES ('"+po_number+"','"+po_item_id+"')"
                    Log.e("insert query",insertQuery)
                    statement.executeUpdate(insertQuery, Statement.RETURN_GENERATED_KEYS)
                    val resultSet: ResultSet = statement.generatedKeys
                    if (resultSet.next()) {
                        generatedId = resultSet.getLong(1).toInt()
                    }
                    Log.e("insert query status","Data inserted successfully!")
                    runOnUiThread {
                        progressAlert.dismissAlert()
                        SharedHelper.putKey(this@ScannerActivity, "inspection_id", "" + generatedId)
                        val intent = Intent(this@ScannerActivity, ConditionLineItemActivity::class.java)
                        startActivity(intent)
                        finish()

                    }

                } catch (e: SQLException) {
                    progressAlert.dismissAlert()
                    e.printStackTrace()
                }

                it.close() // Don't forget to close the connection when done
            }

        }

    } */
    fun generateRandomAlphanumeric(length: Int): String {
        val alphanumericChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random.Default

        return (1..length)
            .map { alphanumericChars[random.nextInt(alphanumericChars.length)] }
            .joinToString("")
    }
    fun inspectionrandom() {
        val randomAlphanumeric = generateRandomAlphanumeric(8)
        println("Generated Random Alphanumeric: $randomAlphanumeric")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertDataIntoDatabase(poNumber: String, poItemId: String) {
        progressAlert.showAlert()

        GlobalScope.launch {
            DatabaseConnection.connectToDatabase()?.use { connection ->
                try {
                   var generatedId = ""


                    // Insert data into purchase_master
                    val zohoId = 22904140000L + poNumber.toLong() // Calculate zoho_id
                    // Get today's date
                    val currentDate = java.time.LocalDate.now()
                    val formattedDate = java.sql.Date.valueOf(currentDate.toString())

                    val insertPurchaseMasterQuery ="INSERT INTO purchase_master (po_number, zoho_id, po_date, reference_number, status, delivery_date, tracking_number, sub_total, tax_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"

                    val preparedStatementPurchaseMaster = connection.prepareStatement(
                        insertPurchaseMasterQuery,
                        Statement.RETURN_GENERATED_KEYS
                    )
                    preparedStatementPurchaseMaster.setString(1, poNumber)
                    preparedStatementPurchaseMaster.setLong(2, zohoId)
                    preparedStatementPurchaseMaster.setDate(3, formattedDate) // Set po_date
                    preparedStatementPurchaseMaster.setNull(4, Types.VARCHAR) // Set reference_number as null
                    preparedStatementPurchaseMaster.setNull(5, Types.VARCHAR) // Set status as null
                    preparedStatementPurchaseMaster.setNull(6, Types.DATE) // Set delivery_date as null
                    preparedStatementPurchaseMaster.setNull(7, Types.VARCHAR) // Set tracking_number as null
                    preparedStatementPurchaseMaster.setNull(8, Types.DECIMAL) // Set sub_total as null
                    preparedStatementPurchaseMaster.setNull(9, Types.DECIMAL) // Set tax_total as null
                    preparedStatementPurchaseMaster.executeUpdate()

                    // Retrieve the generated key (if needed)
               /*     val resultSetPurchaseMaster: ResultSet =
                        preparedStatementPurchaseMaster.generatedKeys
                    if (resultSetPurchaseMaster.next()) {
                        generatedId = resultSetPurchaseMaster.getLong(1).toInt()
                    }*/
                    // insert data into inspection table
                    val generatedAlphanumeric = generateRandomAlphanumeric(8)
                    generatedId = "$po_number-$generatedAlphanumeric"
                    val insertQuery = "INSERT INTO inspection (po_number, po_item_id,inspection_id) VALUES (?, ?, ?)"
                    val preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)

                    preparedStatement.setString(1, poNumber)
                    preparedStatement.setString(2, poItemId)
                    preparedStatement.setString(3, generatedId)

                    preparedStatement.executeUpdate()

                    val resultSet: ResultSet = preparedStatement.generatedKeys
                    if (resultSet.next()) {

                     //   generatedId = Random.nextInt()
                    }


                    runOnUiThread {
                        progressAlert.dismissAlert()
                        SharedHelper.putKey(this@ScannerActivity, "inspection_id", "" + generatedId)
                        val intent = Intent(this@ScannerActivity, WeightAndDiemensActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                } catch (e: SQLException) {
                    progressAlert.dismissAlert()
                    Log.e("Insert Error", e.message ?: "Unknown error")
                    e.printStackTrace()
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.prev_insp -> {
                openPreviousInspectionActivity()
                true
            }
            R.id.logout -> {
                // Handle logout item click
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun openPreviousInspectionActivity() {
        val intent = Intent(this, Previous_inspections::class.java)
        startActivity(intent)
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Perform logout actions
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun performLogout() {
        // Add your logout logic here
        // For example, clear user session, navigate to the login screen, etc.
        // Example:
        // Clear user session and navigate to the login screen
        // You should replace this with your actual logout logic
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}