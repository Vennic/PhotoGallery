package com.kuzheevadel.photogallery

import android.content.Context
import android.preference.PreferenceManager

fun getStoredQuery(context: Context): String? {
    return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("searchQuery", null)
}

fun setStoredQuery(context: Context, query: String?) {
    PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString("searchQuery", query)
            .apply()
}