package com.jayanthl.callrecorder.callrecorderandsaver

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class RecordplayActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener  {

    val TAG = "RecordplayActivity"

    lateinit var playPauseButton: ImageView
    lateinit var positionBar: SeekBar
    lateinit var currentTime: TextView
    lateinit var totalDuration: TextView
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordplay)

        playPauseButton = findViewById(R.id.playPauseButton)
        positionBar = findViewById(R.id.positionSeekBar)
        currentTime = findViewById(R.id.currentTime)
        totalDuration = findViewById(R.id.totalDuration)

        //toolbar
        var toolbarPlay: Toolbar = findViewById(R.id.toolbar_playact)
        setSupportActionBar(toolbarPlay)
        val actionBar: ActionBar? = this.supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }

        // MediaPlayer
        var audioFileName: String = intent.getStringExtra("audioFileName")
        Log.i(TAG, "got file name as $audioFileName")

        try {
            mediaPlayer = MediaPlayer.create(this, Uri.parse(audioFileName))
            mediaPlayer.isLooping = false
            mediaPlayer.seekTo(0)
            val totalTime = mediaPlayer.duration
            totalDuration.text = createTimeLabel(totalTime)

            //positionBar
            positionBar = findViewById(R.id.positionSeekBar)
            positionBar.max = totalTime
            positionBar.setOnSeekBarChangeListener(this)
            mediaPlayer.setOnCompletionListener {
                playPauseButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
                finish()
            }

            // Play and pause thing...

            playPauseButton.setOnClickListener(View.OnClickListener {
                if(!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    playPauseButton.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
                } else {
                    mediaPlayer.pause()
                    playPauseButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
                }
            })
        } catch (e: Exception) {
            mediaPlayer = MediaPlayer()
            Toast.makeText(this, "This recording is empty, you can delete it", Toast.LENGTH_LONG).show()
            finish()
            e.printStackTrace()

        }


        // Thread Update position bar and time label

        Thread(Runnable {
            while (mediaPlayer != null) {
                try {
                    val message: Message = Message()
                    message.what = mediaPlayer.currentPosition
                    handler.sendMessage(message)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if(fromUser) {
            mediaPlayer.seekTo(progress)
            positionBar.setProgress(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    private val handler = @SuppressLint("HandlerLeak")
    object: Handler() {

        override fun handleMessage(msg: Message?) {
            var currentPosition = msg!!.what
            //update positionbar
            positionBar.setProgress(currentPosition)
            var elapsedTime = createTimeLabel(currentPosition)
            currentTime.text = elapsedTime

        }
    }

    fun createTimeLabel(time: Int): String {
        var timeLabel = ""
        var min: Int = time / 1000 / 60
        var sec: Int = time / 1000 % 60
        timeLabel = min.toString() + ":"
        if(sec < 10) timeLabel += "0"
        timeLabel += sec
        return timeLabel
    }

    override fun onBackPressed() {
        mediaPlayer.stop()
        finish()
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

    override fun onPause() {
        mediaPlayer.pause()
        super.onPause()
    }

}
