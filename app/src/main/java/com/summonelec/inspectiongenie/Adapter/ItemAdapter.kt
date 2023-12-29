package com.summonelec.inspectiongenie.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.summonelec.inspectiongenie.API.APIInterface.POJO.ListItem
import com.summonelec.inspectiongenie.Helper.CellClickListener
import com.summonelec.inspectiongenie.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ItemAdapter(private val context: Context, private val list: List<ListItem>,var cellClickListener: CellClickListener): BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val listorderView = LayoutInflater.from(context).inflate(R.layout.item_stock_single, parent, false)
        val item_qty: TextView = listorderView.findViewById(R.id.item_qty)
        val item_desc: TextView = listorderView.findViewById(R.id.item_desc)
        val item_name: TextView = listorderView.findViewById(R.id.item_name)
        val item_checkbox: CheckBox = listorderView.findViewById(R.id.item_checkbox)
        item_qty.setText(list.get(position).item_qty)
        item_desc.setText(list.get(position).item_desc)
        item_name.setText(list.get(position).item_name)
        item_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
            {
                cellClickListener.onCellClickListener(position)
            }
            else
            {
                cellClickListener.onUpdatedCellListener()
            }

        }
        return listorderView

    }

}