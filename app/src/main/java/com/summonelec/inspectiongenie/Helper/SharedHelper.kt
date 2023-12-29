package com.summonelec.inspectiongenie.Helper

import android.content.Context
import android.content.SharedPreferences

object SharedHelper {
    var sharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    @JvmStatic
    fun putKey(context: Context, Key: String?, Value: String?) {
        sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit()
        editor?.putString(Key, Value)
        editor?.commit()
    }

    @JvmStatic
    fun getKey(contextGetKey: Context, Key: String?): String? {
        sharedPreferences = contextGetKey.getSharedPreferences("Cache", Context.MODE_PRIVATE)
        return sharedPreferences?.getString(Key, "")
    }

    fun clearSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.clear()?.commit()
    }
}