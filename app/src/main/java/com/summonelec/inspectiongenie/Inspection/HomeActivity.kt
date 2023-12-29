package com.summonelec.inspectiongenie.Inspection

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.navigation.NavigationView
import com.summonelec.inspectiongenie.Helper.SendEmailUtil
import com.summonelec.inspectiongenie.Helper.SharedHelper
import com.summonelec.inspectiongenie.Inspection.PageFragments.HomeFragment
import com.summonelec.inspectiongenie.R
import com.summonelec.inspectiongenie.UserLogin.LoginActivity
import com.summonelec.inspectiongenie.databinding.ActivityHomeBinding
import com.summonelec.inspectiongenie.utilities.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity(), View.OnClickListener {
    private val mAppBarConfiguration: AppBarConfiguration? = null
    private var binding: ActivityHomeBinding? = null
    var drawerToggle: ActionBarDrawerToggle? = null
    var drawer: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var fragment: Fragment? = null
    var doubleBackToExitPressedOnce = false
    var homeFragment: HomeFragment? = null
    var pdf_file_path: String? = null
    private val LINE_SEPARATOR = "\n"
    var TAG = "message"
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(getLayoutInflater())
        setContentView(binding?.getRoot())
  /*      toolbar = binding?.appBarHome?.toolbar
        setSupportActionBar(binding?.appBarHome?.toolbar)
        spinnerActiveItems = binding?.appBarHome?.activeItemSpinner
        //this.actionBar = getSupportActionBar()
        Objects.requireNonNull<ActionBar>(getSupportActionBar()).setHomeButtonEnabled(true)
        mTxtToolbarTitle = binding?.appBarHome?.toolbarTitle
        drawer = binding?.drawerLayout
        navigationView = binding?.navView
        homeFragment = HomeFragment()
        mTxtToolbarTitle?.setText("Home")
        loadFragments(homeFragment)
        val username = SharedHelper.getKey(this@HomeActivity, "user_name")
        val user_email = SharedHelper.getKey(this@HomeActivity, "user_email")
        binding?.userName?.setText(username)
        binding?.userMail?.setText(user_email)
        sendmail()
        setupDrawerToggle()*/

//        binding?.txtMenuHome?.setOnClickListener(this)
//        binding?.txtMenuStockPartsActivity?.setOnClickListener(this)
//        binding?.txtMenuPartActivity?.setOnClickListener(this)
//        binding?.txtMenuActiveItems?.setOnClickListener(this)
//        binding?.txtMenuPackages?.setOnClickListener(this)
//        binding?.txtMenuSalesOrders?.setOnClickListener(this)
//        binding?.txtMenuPurchaseOrders?.setOnClickListener(this)
//        binding?.txtMenuSettings?.setOnClickListener(this)
        binding?.txtMenuStartNewInspection?.setOnClickListener(this)
         binding?.txtMenuLogout?.setOnClickListener(this)
//        val image_url = SharedHelper.getKey(this@Home, "user_image")
//        val user_name = SharedHelper.getKey(this@Home, "user_name")
//        val user_mail = SharedHelper.getKey(this@Home, "user_email")
//        binding?.userName?.setText("user_name")
//        binding?.userMail?.setText("user_mail")
//        Glide.with(this)
//            .load("image_url")
//            .into(binding?.profileImage!!)



        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*  mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_gallery, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);*/
    }

    /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }*/
    /* @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }*/
    fun setupDrawerToggle() {
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawer,
   //         binding?.appBarHome?.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawer?.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Hide soft key board programmatically
                Utils.hideKeyboard(this@HomeActivity)
            }

            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        drawerToggle!!.syncState()
    }

    @SuppressLint("NonConstantResourceId", "SetTextI18n")
    override fun onClick(v: View) {
        when (v.id) {
//            R.id.txtMenuHome -> {
//                homeFragment = HomeFragment()
//                mTxtToolbarTitle.setText("Home")
//                loadFragments(homeFragment)
//                drawer.close()
//            }
//            R.id.txtMenuStockPartsActivity -> {
//                fragment = StockPartsActivityFragment()
//                mTxtToolbarTitle.setText("Stock Parts Activity")
//                loadFragments(fragment)
//                drawer.close()
//            }
//            R.id.txtMenuPartActivity -> {
//                fragment = PartActivityFragment()
//                mTxtToolbarTitle.setText("Part Activity")
//                loadFragments(fragment)
//                drawer.close()
//            }
//            R.id.txtMenuActiveItems -> {
//                fragment = ActiveItemsFragment()
//                mTxtToolbarTitle.setText("Active Items")
//                loadFragments(fragment)
//                drawer.close()
//            }
//            R.id.txtMenuPackages -> {
//                fragment = PackagesFragment()
//                mTxtToolbarTitle.setText("Packages")
//                loadFragments(fragment)
//                drawer.close()
//            }
//            R.id.txtMenuSalesOrders -> {
//                fragment = SalesOrdersFragment()
//                mTxtToolbarTitle.setText("Sales Orders")
//                loadFragments(fragment)
//                drawer.close()
//            }
//            R.id.txtMenuPurchaseOrders -> {
//                fragment = PurchaseOrdersFragment()
//                mTxtToolbarTitle.setText("Purchase Orders")
//                loadFragments(fragment)
//                drawer.close()
//            }
            R.id.txtMenuStartNewInspection -> {
//                val intent = Intent(this@HomeActivity, InspectionActivity::class.java)
//                startActivity(intent)
                drawer?.close()
            }
//            R.id.txtMenuSettings -> {
//                fragment = SettingsFragment()
//                mTxtToolbarTitle.setText("Settings")
//                loadFragments(fragment)
//                drawer.close()
//            }
            R.id.txtMenuLogout -> {
                //SharedHelper.clearSharedPreferences(this)
                SharedHelper.putKey(this@HomeActivity, "login", "no")
                val login = Intent(this, LoginActivity::class.java)
                startActivity(login)
                finish()
                drawer?.close()
            }
            else -> {}
        }
    }

    fun loadFragments(fragment: Fragment?) {
        if (fragment != null) {
            val fragmentManager: FragmentManager = getSupportFragmentManager()
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null).commit()
        }
    }

    fun onTwiceClick() {
        if (doubleBackToExitPressedOnce) {
            finish()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click again to exit..", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (homeFragment?.isVisible() == true) {
            onTwiceClick()
        } else {
            super.onBackPressed()
        }
    }
   fun sendmail()
   {
       val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
       val view = inflater.inflate(R.layout.report_sample, null)
       val package_receive_layout :LinearLayout = view.findViewById(R.id.package_receive_layout)
       val weight_dimension_layout :LinearLayout = view.findViewById(R.id.weight_dimension_layout)
       val condition_items_layout :LinearLayout = view.findViewById(R.id.condition_items_layout)
       val manufature_label_layout :LinearLayout = view.findViewById(R.id.manufature_label_layout)
       val layoutViews: List<View> = listOf(package_receive_layout, weight_dimension_layout, condition_items_layout,manufature_label_layout)
      generatePdfFromView(this, layoutViews, "my_generated_pdf")
       val c = Calendar.getInstance().time
       println("Current time => $c")
       val df = SimpleDateFormat("yyyy-MM-dd")
       val formattedDate = df.format(c)
       val stackTrace = StringWriter()
       val errorReport = StringBuilder()
       errorReport.append("************ CAUSE OF ERROR ************\n\n")
       errorReport.append(stackTrace.toString())
       errorReport.append("\n************ DEVICE INFORMATION ***********\n")
       errorReport.append("Brand: ")
       errorReport.append(Build.BRAND)
       errorReport.append(LINE_SEPARATOR)
       errorReport.append("Device: ")
       errorReport.append(Build.DEVICE)
       errorReport.append(LINE_SEPARATOR)
       errorReport.append("Model: ")
       errorReport.append(Build.MODEL)
       errorReport.append(LINE_SEPARATOR)
       errorReport.append("Id: ")
       errorReport.append(Build.ID)
       errorReport.append(LINE_SEPARATOR)
       errorReport.append("Product: ")
       errorReport.append(Build.PRODUCT)
       errorReport.append(LINE_SEPARATOR)
       errorReport.append("\n************ FIRMWARE ************\n")
       errorReport.append("SDK: ")
       errorReport.append(Build.VERSION.SDK)
       errorReport.append(LINE_SEPARATOR)
       errorReport.append("Release: ")
       errorReport.append(Build.VERSION.RELEASE)
       errorReport.append(LINE_SEPARATOR)
       errorReport.append("Incremental: ")
       errorReport.append(Build.VERSION.INCREMENTAL)
       errorReport.append(LINE_SEPARATOR)
       errorReport.append("Date and Time: ")
       val currentTime = Calendar.getInstance().time
       errorReport.append(currentTime.toString())
       errorReport.append(LINE_SEPARATOR)

       errorReport.append(LINE_SEPARATOR)
       thread {
           val recipientEmail = arrayOf("jatin.s76655@gmail.com")
           val subject = "Test Email with Attachment"
           val body = errorReport.toString()
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



    companion object {
        var toolbar: Toolbar? = null
        var actionBar: ActionBar? = null
        var mTxtToolbarTitle: TextView? = null
        var spinnerActiveItems: Spinner? = null
    }


}