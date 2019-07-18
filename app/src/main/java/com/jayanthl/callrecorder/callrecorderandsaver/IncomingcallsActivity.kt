package com.jayanthl.callrecorder.callrecorderandsaver

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.Toast
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.jayanthl.callrecorder.callrecorderandsaver.AdapterForListingCalls.RecyclerViewOutGoingAdapter
import kotlinx.android.synthetic.main.activity_incomingcalls.*
import java.io.File
import java.util.*

// this program is actually associated with outgoing

class IncomingcallsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incomingcalls)

        var toolbarIncoming: android.support.v7.widget.Toolbar = findViewById(R.id.toolBarincoming)
        setSupportActionBar(toolbarIncoming)
        val actionBar: ActionBar? = this.supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }

        val recordName = intent.getStringExtra("record_name")
        val recordType = intent.getStringExtra("record_type")
        val recordPhoneNumber = intent.getStringExtra("record_phone_number")
        val recordImageUri = intent.getStringExtra("record_image_uri")

        //collect outgoingcallsFileList
        var outgoingFilesList = mutableListOf<String>()
        var outGoingFileLastAccessedTime = mutableListOf<String>()
        File(Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/").walk().forEach {
            if ("$it".contains(recordPhoneNumber) && "$it".contains("outgoing")) {
                outgoingFilesList.add("$it")

                outGoingFileLastAccessedTime.add(android.text.format.DateFormat.getDateFormat(this).format(Date(File("$it").lastModified())).toString() + " at ${android.text.format.DateFormat.getTimeFormat(this).format(Date(File("$it").lastModified())).toString()}")
            }
        }

        val recyclerViewOutGoingAdapter = RecyclerViewOutGoingAdapter(this, recordImageUri, recordName, recordPhoneNumber, outgoingFilesList, outGoingFileLastAccessedTime)
        recyclerViewIncomingOutgoing.adapter = recyclerViewOutGoingAdapter
        recyclerViewIncomingOutgoing.layoutManager = LinearLayoutManager(this)



        //Toast.makeText(this, "$recordName, $recordType, $recordPhoneNumber", Toast.LENGTH_LONG).show()

    }

    private fun getImageFromPhoneNumber(contactNumber: String): String {
        var allContacts: List<Contact> = Contacts.getQuery().find()
        for (contacts in allContacts) {
            if (contacts.phoneNumbers.isNotEmpty()) {
                if (contacts.phoneNumbers[0].normalizedNumber != null) {
                    if (contacts.phoneNumbers[0].normalizedNumber.contains(contactNumber)) {
                        return if (contacts.photoUri == null) {
                            ""
                        } else {
                            contacts.photoUri
                        }
                    }
                }
            }
        }
        return ""
    }

    private fun getNameFromPhoneNumber(contactNumber: String): String {
        var allContacts: List<Contact> = Contacts.getQuery().find()
        for (contacts in allContacts) {
            if (contacts.phoneNumbers.isNotEmpty()) {
                if (contacts.phoneNumbers[0].normalizedNumber != null) {
                    if (contacts.phoneNumbers[0].normalizedNumber.contains(contactNumber)) {
                        return contacts.displayName
                    }
                }
            }
        }
        return ""
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            android.R.id.home -> {
                finish()
                return true
            } else -> super.onOptionsItemSelected(item)
        }

        return false
    }
}
