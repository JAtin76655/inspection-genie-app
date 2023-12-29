package com.summonelec.inspectiongenie.Helper

import android.content.Context
import android.graphics.Color
import cn.pedant.SweetAlert.SweetAlertDialog

class ProgressAlert(private val context: Context) {
    var pDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)

    fun showAlert()
    {

        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading ...");
        pDialog.setCancelable(false);
        pDialog.show();

    }
    fun dismissAlert()
    {
        pDialog.dismiss()
    }
}