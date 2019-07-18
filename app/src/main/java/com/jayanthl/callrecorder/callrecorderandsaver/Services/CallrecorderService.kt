package com.jayanthl.callrecorder.callrecorderandsaver.Services

import android.app.*
import android.app.Notification.GROUP_ALERT_SUMMARY
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.jayanthl.callrecorder.callrecorderandsaver.MainActivity
import com.jayanthl.callrecorder.callrecorderandsaver.R
import com.jayanthl.callrecorder.callrecorderandsaver.RecordplayActivity
import java.io.File
import java.io.IOException
import java.util.*

class CallrecorderService: Service() {

    var mediaRecorder: MediaRecorder? = null
    val CHANNEL_ID: String = "recordServiceChannel"
    val CHANNEL_ID_2: String = "recordPersistentChannel"
    lateinit var audioFile: String
    lateinit var phoneNumber: String
    lateinit var audioFormat: String
    lateinit var recordingFileNameForNotification: String
    var isRecording: Boolean = false
    lateinit var mContext: Context

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //get the sent data
        phoneNumber = intent!!.getStringExtra("phoneNumber")
        var callType = intent!!.getStringExtra("call_type")
        mContext = this

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        //Creating a notification channel for foreground service
        createNotificationChannel()
        var notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("recording your call")
                .setContentTitle("Recording Call")
                .setSmallIcon(R.drawable.navigation_icon)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(1, notification)



        val intentFilter = IntentFilter("com.example.jayanthl.callrecorddemo1.EXAMPLE_STOP")
        registerReceiver(broadcastReceiver, intentFilter)
        Log.i("CALL", "service started : $callType")
        try {
            mediaRecorder!!.release()
        } catch (e: Exception) {
            //e.printStackTrace()
        }
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        val recordingFileName: String = getRandomRecordFileName(callType)
        recordingFileNameForNotification = recordingFileName
        mediaRecorder!!.setOutputFile(recordingFileName)
        try {
            mediaRecorder!!.prepare()
            Log.i("CAL", "media prepared")
            Log.i("CAL", "media record")
        } catch (e: IllegalStateException) {
            Log.i("CALL", "could'nt prepare")
            //e.printStackTrace()
        } catch (e: IOException) {
            Log.i("CALL", "IO Exception")
        }

        try {
            mediaRecorder!!.start()
            Log.i("CALL", "Recording Started")
            isRecording = true
        } catch (e: Exception) {
            Log.i("CALL", "couldn't start recording")
        }





        /*Handler().postDelayed({
            mediaRecorder!!.stop()
            Log.i("CALL", "record stopped")
            if(mediaRecorder != null) {
                mediaRecorder!!.reset()
                mediaRecorder!!.release()
                mediaRecorder = null
                Log.i("CALL", "released media recorder")
            }
            onDestroy()
        }, 30000)*/
        return  START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.i("CALL", "service ended")
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: RuntimeException) {

        }
        super.onDestroy()
    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("CALL", "received a broadcast to stop recording")
            if(mediaRecorder != null) {
                if(isRecording) {
                    mediaRecorder!!.stop()
                }
                mediaRecorder!!.reset()
                mediaRecorder!!.release()
                mediaRecorder = null
                Log.i("CALL", " media recorder released")
                onDestroy()

                //generate a notification of recorded call if the user settings says
                val preferencesSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("notificationswitch", false)
                if(preferencesSharedPreferences) {
                    val notificationIntent = Intent(mContext, RecordplayActivity::class.java)
                    notificationIntent.putExtra("audioFileName", recordingFileNameForNotification)
                    Log.i("Service", "recording File Name : $recordingFileNameForNotification")
                    val pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

                    //Creating a notification channel for Call record Completion.
                    createPersistenceNotificationChannel()
                    var notification2 = NotificationCompat.Builder(mContext, CHANNEL_ID)
                            .setContentText("Call Recorded")
                            .setContentTitle("Tap to listen")
                            .setSmallIcon(R.drawable.navigation_icon)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setOnlyAlertOnce(true)
                            .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                            .setGroup("Notification Group")
                            .setGroupSummary(false)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)

                    val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(mContext)
                    notificationManager.notify(2, notification2.build())
                }
                stopSelf()
            }
        }
    }

    fun ClosedRange<Int>.random() = Random().nextInt((endInclusive -1) - start) + start

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var serviceChannel = NotificationChannel(CHANNEL_ID, "Recording Started", NotificationManager.IMPORTANCE_DEFAULT)
            var notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }


    private fun createPersistenceNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var serviceChannel = NotificationChannel(CHANNEL_ID_2, "Call Recorded", NotificationManager.IMPORTANCE_MIN)
            var notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getRandomRecordFileName(callType: String): String {
        val tempCallFileName = Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/callrecordings1_${callType}_${phoneNumber}_${(0..1000000000).random()}.amr"
        if(!File(tempCallFileName).exists()) {
            return tempCallFileName
        } else {
            return getRandomRecordFileName(callType)
        }
    }
}
