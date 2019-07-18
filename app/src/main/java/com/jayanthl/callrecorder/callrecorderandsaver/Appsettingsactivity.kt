package com.jayanthl.callrecorder.callrecorderandsaver

import android.app.ActionBar
import android.app.AppComponentFactory
import android.os.Bundle
import android.preference.*
import AppCompatPreferenceActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.preference_withactionbar.*

class Appsettingsactivity : AppCompatPreferenceActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preference_withactionbar)
        val toolbar = findViewById<Toolbar>(R.id.toolbarPreference)
        setSupportActionBar(toolbar)
        setUpActionBar()
        addPreferencesFromResource(R.xml.preference)

        changeSummaryAndStitleForNotification(findPreference("notificationswitch"))
        changeSummaryAndStitleForRecording(findPreference("recordingswitch"))
    }

    private val PrefercnceChangeListener = Preference.OnPreferenceChangeListener{preference, any ->
        if(preference is SwitchPreference) {
            if(preference.key == "notificationswitch") {
                if(any.toString().equals("true")) {
                    preference.summary = "Persistent Notification will be shown after call record"
                } else if(any.toString().equals("false")) {
                    preference.summary = "Persistent Notification is Disabled"
                }
            } else if(preference.key == "recordingswitch") {
                if(any.toString().equals("true")) {
                    preference.summary = "Call Recording is ACTIVE"
                } else if(any.toString().equals("false")) {
                    preference.summary = "Call Recording is DISABLED"
                }
            }
        }
        return@OnPreferenceChangeListener true
    }

    private fun changeSummaryAndStitleForNotification(preference: Preference) {
        preference.onPreferenceChangeListener = PrefercnceChangeListener
        PrefercnceChangeListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.context)
                        .getBoolean("notificationswitch", false))
    }

    private fun changeSummaryAndStitleForRecording(preference: Preference) {
        preference.onPreferenceChangeListener = PrefercnceChangeListener
        PrefercnceChangeListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.context)
                        .getBoolean("recordingswitch", false))
    }

    class SettingsFragment: PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preference)
        }


    }

    private fun setUpActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }
}