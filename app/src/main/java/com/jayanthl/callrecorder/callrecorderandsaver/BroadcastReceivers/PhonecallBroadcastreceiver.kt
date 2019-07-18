package com.jayanthl.callrecorder.callrecorderandsaver.BroadcastReceivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager
import android.util.Log
import com.jayanthl.callrecorder.callrecorderandsaver.Services.CallrecorderService
import java.util.*

class PhonecallBroadcastreceiver: BroadcastReceiver() {

    val TAG= "BroadcastReceiver"
    var latsState: Int = TelephonyManager.CALL_STATE_IDLE
    var call_type = ""
    lateinit var callStartTime: Date
    var isIncoming: Boolean = false
    lateinit var savedNumber: String
    lateinit var finalPhoneNumber: String

    override fun onReceive(mContext: Context?, intent: Intent?) {
        val prefereneSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("recordingswitch", false)
        if(prefereneSharedPreferences) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var permissionArrayList: ArrayList<String> = ArrayList()
                permissionArrayList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                permissionArrayList.add(android.Manifest.permission.RECORD_AUDIO)
                permissionArrayList.add(android.Manifest.permission.PROCESS_OUTGOING_CALLS)
                permissionArrayList.add(android.Manifest.permission.READ_PHONE_STATE)
                permissionArrayList.add(Manifest.permission.READ_CONTACTS)

                if(hasPermissions(mContext!!, permissionArrayList)) {
                    startBroadcastReceiver(mContext, intent)
                }
            } else {
                startBroadcastReceiver(mContext!!, intent)
            }
        }
    }

    fun startBroadcastReceiver(mContext: Context, intent: Intent?) {
        if(intent!!.action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.extras.getString("android.intent.extra.PHONE_NUMBER")
            Log.i("CALL", "logged first event: $savedNumber")
            var sharedPreferences = mContext!!.getSharedPreferences("callDataType", Context.MODE_PRIVATE)
            var editor = sharedPreferences.edit()
            val isOutgoing = sharedPreferences.getString("outgoingcall_started", "")
            if(!isOutgoing.equals("yes")) {
                editor.remove("outgoingcall_started")
                editor.putString("outgoingcall_started", "yes")
                editor.putString("outgoing_number", savedNumber)
                editor.apply()
            }

            //Toast.makeText(mContext, " this is an outgoing : $savedNumber", Toast.LENGTH_LONG).show()

        } else {

            var sharedPreferences = mContext!!.getSharedPreferences("callDataType", Context.MODE_PRIVATE)
            var editor = sharedPreferences.edit()
            val isOutgoing = sharedPreferences.getString("outgoingcall_started", "")
            if(isOutgoing.equals("yes")) {
                savedNumber = sharedPreferences.getString("outgoing_number", "") // yes yes
                editor.remove("outgoingcall_started")
                editor.remove("outgoing_number")
                editor.putString("outgoingcall_started", "")
                editor.putString("outgoing_number", "")
                editor.apply()
            } else {

                // else this will be an incoming
                var number = intent.extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                //Toast.makeText(mContext, " this is an incoming call : $number", Toast.LENGTH_LONG).show()
                if(number == null) {
                    savedNumber = "null"

                    Log.i("CALL", "null number")
                } else {
                    savedNumber = number
                    Log.i("CALL", "got a number: $number")
                }
            }
            var stateStr = intent.extras.getString(TelephonyManager.EXTRA_STATE)

            var state = 0
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.i("CALL", "setting state to idle")
                state = TelephonyManager.CALL_STATE_IDLE
            } else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.i("CALL", "setting state to offhook")
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.i("CALL", "setting state to ringing")
                state = TelephonyManager.CALL_STATE_RINGING
            }
            onCallStateChanged(mContext, state, savedNumber)
        }
    }

    fun onIncomingCallReceived(mContext: Context?, number: String, start: Date) {
        Log.i("CALL", "an incoming call detected")
        var sharedPreferences = mContext!!.getSharedPreferences("callDataType", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("incoming_number", number)
        editor.putString("callDataType", "yes")
        editor.apply()
        //Toast.makeText(mContext, "an incoming call detected", Toast.LENGTH_LONG).show()
    }
    fun onOutgoingCallstarted(mContext: Context?, number: String, start: Date){
        Log.i("CALL", "an outgoing call detected")

        var sharedPreferences = mContext!!.getSharedPreferences("callDataType", MODE_PRIVATE)
        var isIncomingCall: String = sharedPreferences.getString("callDataType", "")

        if(isIncomingCall.equals("yes")) { //no yes
            call_type = "incoming"
            var editor = sharedPreferences.edit()
            finalPhoneNumber = sharedPreferences.getString("incoming_number", "")
            editor.remove("incoming_number")
            editor.remove("callDataType")
            editor.putString("incoming_number", "")
            editor.putString("callDataType", "")
            editor.apply()
        } else {
            call_type = "outgoing"
            var editor = sharedPreferences.edit()
            finalPhoneNumber = number

        }
        var service = Intent()
        service.setClass(mContext, CallrecorderService::class.java)
        service.putExtra("phoneNumber", finalPhoneNumber)
        service.putExtra("call_type", call_type)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(mContext!!, service)
        } else {
            mContext!!.startService(service)
        }
        //Toast.makeText(mContext, "an outgoing call detected", Toast.LENGTH_LONG).show()
    }
    fun onIncomingcallEnded(mContext: Context?, number: String, start: Date, end: Date) {
       //Toast.makeText(mContext, "incoming call ended", Toast.LENGTH_LONG).show()
        Log.i("CALL", "incoming call ended")
    }
    fun onOutgoingCallEnded(mContext: Context?, number: String, start: Date, end: Date) {
        //Toast.makeText(mContext, "outgoing call ended", Toast.LENGTH_LONG).show()
        Log.i("CALL", "outgoing call ended")
        mContext!!.sendBroadcast(Intent().setAction("com.example.jayanthl.callrecorddemo1.EXAMPLE_STOP"))
    }
    fun onMissedcall(mContext: Context?, number: String, start: Date) {
        //Toast.makeText(mContext, " you got a missed call", Toast.LENGTH_LONG).show()
        Log.i("CALL", "you got a missed call")
    }
    fun onIncomingCallAnswered(mContext: Context?, number: String, start: Date) {
        //Toast.makeText(mContext, "you answered an incoming call", Toast.LENGTH_LONG).show()
        Log.i("CALL", "you answered an incoming call")
    }

    fun onCallStateChanged(mContext: Context?, state: Int, number: String) {

        when(state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                Log.i("CALL", "triggering 1st switch")
                isIncoming = true
                callStartTime = Date()
                savedNumber = number

                latsState = state
                onIncomingCallReceived(mContext, number, callStartTime)
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                Log.i("CALL", "triggering 2nd switch")
                if(latsState != TelephonyManager.CALL_STATE_RINGING && state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    callStartTime = Date()
                    latsState = state
                    onOutgoingCallstarted(mContext, savedNumber, callStartTime)

                } else if (latsState == TelephonyManager.CALL_STATE_RINGING && state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    isIncoming = true
                    callStartTime = Date()
                    latsState = state

                    onIncomingCallAnswered(mContext, savedNumber, callStartTime)
                }
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                Log.i("CALL", "triggering 3rd switch")
                if(latsState == TelephonyManager.CALL_STATE_RINGING) {
                    onMissedcall(mContext, number, callStartTime)
                    latsState = state
                } else if(isIncoming) {
                    onIncomingcallEnded(mContext, savedNumber, Date(), Date())
                    latsState = state
                } else {
                    onOutgoingCallEnded(mContext, savedNumber, Date(), Date())
                    val sharedPreferences = mContext!!.getSharedPreferences("callDataType", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.remove("callDataType")
                    editor.putString("callDataType", "")
                    editor.apply()
                    latsState = state
                }
            }
        }
    }

    fun hasPermissions(context: Context, permissionsList: ArrayList<String>): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permissionItem in permissionsList) {
                if (ContextCompat.checkSelfPermission(context, permissionItem) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        } else {
            return true
        }

    }
}