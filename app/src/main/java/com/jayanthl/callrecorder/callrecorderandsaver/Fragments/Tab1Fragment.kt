package com.jayanthl.callrecorder.callrecorderandsaver.Fragments

import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.view.MenuCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.Toast
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.jayanthl.callrecorder.callrecorderandsaver.AdaptersForTab.RecyclerViewAdadpterForTab1
import com.jayanthl.callrecorder.callrecorderandsaver.R
import java.io.File

class Tab1Fragment: Fragment(), SearchView.OnQueryTextListener {

    var allDistinctNumbers: MutableList<String> = mutableListOf()

    var phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    var allInternationalPhoneNumberList = mutableListOf<String>()
    var allLinearInternationalPhoneNumberList = mutableListOf<String>()

    lateinit var recyclerViewAdadpterForTab1: RecyclerViewAdadpterForTab1

    val TAG: String  = "Tab1Fragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        allDistinctNumbers.clear()
        allInternationalPhoneNumberList.clear()
        allLinearInternationalPhoneNumberList.clear()
        setHasOptionsMenu(true)
        var view: View = inflater.inflate(R.layout.tab1_layout, container, false)

        var allAudioFilesPath = getAllAudioFilesDirectory()
        var extractedNumberIncomingList = extractNumberFromRecordedFileNameForIncomingCalls(allAudioFilesPath) // all incoming numbers list
        //Now standardising the incoming calls using libphoneutil
        for(numbers in  extractedNumberIncomingList) {
            try {
                var numberInit = phoneUtil.parse(numbers, "IN")
                allInternationalPhoneNumberList.add(phoneUtil.format(numberInit, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL))
                allLinearInternationalPhoneNumberList.add(phoneUtil.format(numberInit, PhoneNumberUtil.PhoneNumberFormat.E164))
            } catch (e: NumberParseException) {}
        }


        //getting all names and imageUri's
        for(phoneNumber in extractedNumberIncomingList.distinct()) {
            allDistinctNumbers.add(phoneNumber)
        }

        //Log.i(TAG, "All incoming Numbers : ${allDistinctNumbers}")
        //Log.i(TAG, "All incoming Numbers size : ${allDistinctNumbers.size}")
        //Log.i(TAG, "All internationalised number length:${allLinearInternationalPhoneNumberList.size}")
        //Log.i(TAG, "All incoming Names len : ${allNamesForAllNumbers}")
        //Log.i(TAG, "All incoming Image Uri's : ${allImageUrisForAllNumbers}")

        var recyclerView1: RecyclerView = view.findViewById(R.id.recyclerView1)
        recyclerViewAdadpterForTab1 = RecyclerViewAdadpterForTab1(activity!!.applicationContext, allDistinctNumbers)
        recyclerView1.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView1.setHasFixedSize(true)
        recyclerView1.setItemViewCacheSize(20)
        recyclerView1.isDrawingCacheEnabled = true
        recyclerView1.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        recyclerView1.adapter = recyclerViewAdadpterForTab1
        registerForContextMenu(recyclerView1)


        return view
    }

    private fun extractNumberFromRecordedFileNameForIncomingCalls(allAudioFilesPath: MutableList<String>) : MutableList<String> {
        var extractedNumberFromRecordedFilename: MutableList<String> = mutableListOf()
        var filesDirectory: String = Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/"
        for(it in allAudioFilesPath) {

            var reversedFileName = "$it".reversed()
            var extractedReversedFileName: String = ""
            for(reversedPosition in 0..reversedFileName.length -1) {
                if(reversedFileName[reversedPosition] == '/') {
                    break
                }
                extractedReversedFileName = extractedReversedFileName + reversedFileName[reversedPosition]
            }
            var itExtracted = extractedReversedFileName.reversed()
            //Log.i(TAG, "extracted : $itExtracted")

            if(itExtracted.contains("incoming") && itExtracted.contains("+91")) {
                var extractedNumber: String = ""
                for(position in 28..itExtracted.length - 1) {
                    if((itExtracted)[position] == '_') {
                        break
                    }
                    extractedNumber = extractedNumber + itExtracted[position]
                }
                //Log.i(TAG,"incoming extracted Number from +91 : $extractedNumber")
                extractedNumberFromRecordedFilename.add(extractedNumber)
                extractedNumber = ""

            } else if(itExtracted.contains("incoming") && !itExtracted.contains("+91")){
                var extractedNumber: String = ""
                for(position in 25..itExtracted.length - 1) {
                    if((itExtracted)[position] == '_') {
                        break
                    }
                    extractedNumber = extractedNumber + itExtracted[position]
                }
                //Log.i(TAG,"incoming extracted Number from : $extractedNumber")
                extractedNumberFromRecordedFilename.add(extractedNumber)
            }
        }
        //Log.i(TAG, "number of incoming calls : ${extractedNumberFromRecordedFilename.size}")
        //Log.i(TAG, "incomings calls list : $extractedNumberFromRecordedFilename")
        return extractedNumberFromRecordedFilename
    }

    private fun getAllAudioFilesDirectory(): MutableList<String> {
        var allAudioFilesDirectory: MutableList<String> = mutableListOf()
        File(Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/").walk().forEach {
            allAudioFilesDirectory.add("$it")
            //Log.i(TAG, "Files Path : $it")
        }
        allAudioFilesDirectory.removeAt(0)
        return allAudioFilesDirectory
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.search_menu, menu)
        var menuItem: MenuItem = menu!!.findItem(R.id.action_search)
        var searchView = MenuItemCompat.getActionView(menuItem) as SearchView
        searchView.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onQueryTextSubmit(enteredData: String?): Boolean {
        var newUpdatedContactsList: MutableList<String> = mutableListOf()
        recyclerViewAdadpterForTab1.textEntryFindFilter(enteredData, newUpdatedContactsList)
        return true
    }

    override fun onQueryTextChange(enteredData: String?): Boolean {
        /*
        var allDistinctNumbersFilterList: MutableList<String> = mutableListOf()

        for(i in 0..allDistinctNumbers.size -1) {
            if(allDistinctNumbers[i].contains(enteredData.toString())) {
                allDistinctNumbersFilterList.add(allDistinctNumbers[i])
            }
        }
        recyclerViewAdadpterForTab1.updateSearchFilterListForTab1(allDistinctNumbersFilterList)
        */
        return true
    }


}