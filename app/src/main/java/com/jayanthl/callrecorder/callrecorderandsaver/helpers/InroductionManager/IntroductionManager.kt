package com.jayanthl.callrecorder.callrecorderandsaver.helpers.InroductionManager

import android.content.Context
import android.content.SharedPreferences
import com.jayanthl.callrecorder.callrecorderandsaver.R

class IntroductionManager(mContext: Context) {

    var mContext: Context
    var sharedPreferences: SharedPreferences

    init {
        this.mContext = mContext
        sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.my_preferences), Context.MODE_PRIVATE)
    }

    fun writePreference() {
        var editor = sharedPreferences.edit()
        editor.putString(mContext.getString(R.string.my_preferences_key), "executed_greetings")
        editor.commit()
    }

    fun checkWhetherFirstTime(): Boolean {
        val writtenData = sharedPreferences.getString(mContext.getString(R.string.my_preferences_key), "not")
        if(writtenData.equals("not")) {
            return true
        } else {
            return false
        }
    }

    fun clearSharedPreferences() {
        sharedPreferences.edit().clear().commit()
    }
}