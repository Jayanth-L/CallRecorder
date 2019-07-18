package com.jayanthl.callrecorder.callrecorderandsaver

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.ActionBar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.Toast
import com.jayanthl.callrecorder.callrecorderandsaver.AdapterForListingCalls.RecyclerViewIncomingAdapter
import kotlinx.android.synthetic.main.activity_outgoing_calls.*
import java.io.File
import java.util.*

//this program is actually associated with incoming

class OutgoingCallsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_calls)


        val recordName = intent.getStringExtra("record_name")
        val recordType = intent.getStringExtra("record_type")
        val recordPhoneNumber = intent.getStringExtra("record_phone_number")
        val recordImageUri = intent.getStringExtra("record_image_uri")

        //for back button
        var toolbarIncoming: android.support.v7.widget.Toolbar = findViewById(R.id.toolBaroutgoing)
        setSupportActionBar(toolbarIncoming)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }


        //collect incomingcallsFileList
        var incomingFilesList = mutableListOf<String>()
        var incomingFileLastAccessedTime = mutableListOf<String>()
        File(Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/").walk().forEach {
            if ("$it".contains(recordPhoneNumber) && "$it".contains("incoming")) {
                incomingFilesList.add("$it")

                incomingFileLastAccessedTime.add(android.text.format.DateFormat.getDateFormat(this).format(Date(File("$it").lastModified())).toString() + " at ${android.text.format.DateFormat.getTimeFormat(this).format(Date(File("$it").lastModified())).toString()}")
            }
        }

        val recyclerViewIncomingAdapter = RecyclerViewIncomingAdapter(this, recordImageUri, recordName, recordPhoneNumber, incomingFilesList, incomingFileLastAccessedTime)
        recyclerViewIncoming.layoutManager = LinearLayoutManager(this)
        recyclerViewIncoming.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))
        recyclerViewIncoming.adapter = recyclerViewIncomingAdapter



        //Toast.makeText(this, "$recordName, $recordType, $recordPhoneNumber", Toast.LENGTH_LONG).show()

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            android.R.id.home -> {
                finish()
                return true
            } else -> Toast.makeText(this, "Clicked on some", Toast.LENGTH_SHORT).show()
        }
        return false
    }
}
