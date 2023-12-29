package com.summonelec.inspectiongenie.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog

import java.util.*

object Utils {

    fun showAlertDialog(context: Context?, title: String?, message: String?) {
        val builder = AlertDialog.Builder(
            context!!
        )
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(
            "Ok",
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int -> dialog.dismiss() })
        val alertDialog = builder.create()
        alertDialog.show()
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

//    fun getDataList(count: Int): List<ActiveItemsListModel> {
//        val list: MutableList<ActiveItemsListModel> = ArrayList<ActiveItemsListModel>()
//        list.add(ActiveItemsListModel("TMK316BBJ226ML-T", "SKU: 24-CD3U022.00X025-U", "1000.000"))
//        list.add(ActiveItemsListModel("PE014005", "SKU: 06-00077-U", "1000.000"))
//        list.add(ActiveItemsListModel("TMK212B7473KD-T", "SKU:06-00077-U", "1000.000"))
//        for (i in 0 until count) {
//            list.add(ActiveItemsListModel("TMK212B7473KD-T", "SKU:2528$i", "1330.000"))
//        }
//        return list
//    }
//
//    fun getLinkDataList(count: Int): List<DataLinkageActiveItemDetailModel> {
//        val list: MutableList<DataLinkageActiveItemDetailModel> =
//            ArrayList<DataLinkageActiveItemDetailModel>()
//        for (i in 0 until count) {
//            list.add(DataLinkageActiveItemDetailModel("PURCHASE ORDER", "PO-00069"))
//        }
//        return list
//    }

    fun singleSelectItem(textView: AutoCompleteTextView, context: Context?) {
        // AlertDialog builder instance to build the alert dialog
        val alertDialog = AlertDialog.Builder(
            context!!
        )

        // set the custom icon to the alert dialog

        // title of the alert dialog
        alertDialog.setTitle("Choose an Item")

        // list of the items to be displayed to
        // the user in the form of list
        // so that user can select the item from
        val listItems = arrayOf("Android Development", "Web Development", "Machine Learning")

        // the function setSingleChoiceItems is the function which builds
        // the alert dialog with the single item selection
        alertDialog.setSingleChoiceItems(listItems, 0, object : DialogInterface.OnClickListener {
            @SuppressLint("SetTextI18n")
            override fun onClick(dialog: DialogInterface, which: Int) {

                // now also update the TextView which previews the selected item
                textView.setText(listItems[which])

                // when selected an item the dialog should be closed with the dismiss method
                dialog.dismiss()
            }
        })

        // set the negative button if the user
        // is not interested to select or change
        // already selected item
        alertDialog.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {}
        })

        // create and build the AlertDialog instance
        // with the AlertDialog builder instance
        val customAlertDialog = alertDialog.create()

        // show the alert dialog when the button is clicked
        customAlertDialog.show()
    }
}