package com.kuzheevadel.photogallery

import android.content.Context
import android.preference.PreferenceManager

class QueryPreferences {

    companion object {

        @JvmStatic
        val PREF_LAST_RESULT_ID = "lastResultId"

        @JvmStatic
        val PREF_IS_ALARM_ON = "isAlarmOn"

        @JvmStatic
        fun isAlarmOn(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(PREF_IS_ALARM_ON, false)
        }

        @JvmStatic
        fun setAlarmOn(context: Context, isOn: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(PREF_IS_ALARM_ON, isOn)
                    .apply()
        }

        @JvmStatic
        fun getStoredQuery(context: Context): String? {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("searchQuery", null)
        }

        @JvmStatic
        fun setStoredQuery(context: Context, query: String?) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString("searchQuery", query)
                    .apply()
        }

        @JvmStatic
        fun getLastResultId(context: Context): String? {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(PREF_LAST_RESULT_ID, null)
        }

        @JvmStatic
        fun setLastResultId(context: Context, lastResId: String) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(PREF_LAST_RESULT_ID, lastResId)
                    .apply()
        }
    }
}
