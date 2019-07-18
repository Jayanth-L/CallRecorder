package com.jayanthl.callrecorder.callrecorderandsaver.Fragments

import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.jayanthl.callrecorder.callrecorderandsaver.AdaptersForTab.RecyclerViewAdapterForTab2
import com.jayanthl.callrecorder.callrecorderandsaver.R
import java.io.File

class Tab2Fragment: Fragment(), SearchView.OnQueryTextListener {
    val TAG: String = "Tab2Fragment"

    lateinit var recyclerViewAdapterForTab2: RecyclerViewAdapterForTab2

    var allDistinctNumbers: MutableList<String> = mutableListOf()
    var allInternationalPhoneNumberList = mutableListOf<String>()
    var allLinearInternationalPhoneNumberList = mutableListOf<String>()
    var phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        allDistinctNumbers.clear()
        allInternationalPhoneNumberList.clear()
        allLinearInternationalPhoneNumberList.clear()
        setHasOptionsMenu(true)
        var view: View = inflater.inflate(R.layout.tab2_layout, container, false)

        var allAudioFilesPath = getAllAudioFilesDirectory()
        var extractedNumberOutcomingList: MutableList<String> = mutableListOf()
        extractedNumberOutcomingList.clear()
        extractedNumberOutcomingList = extractNumberFromRecordedFileNameForOutgoingCalls(allAudioFilesPath) // all outgoing numbers list
        //Log.i("dela1", " The value got fromthe function : ${extractedNumberOutcomingList}, ${extractedNumberOutcomingList.size}")

        //Now standardising the incoming calls using libphoneutil
        for(numbers in  extractedNumberOutcomingList.distinct()) {
            try {
                var numberInit = phoneUtil.parse(numbers, "IN")
                allInternationalPhoneNumberList.add(phoneUtil.format(numberInit, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL))
                allLinearInternationalPhoneNumberList.add(phoneUtil.format(numberInit, PhoneNumberUtil.PhoneNumberFormat.E164))
            } catch (e: NumberParseException) {}
        }

        //getting all names and imageUri's
        if(extractedNumberOutcomingList.size != 0) {
            for(phoneNumber in extractedNumberOutcomingList.distinct()) {
                allDistinctNumbers.add(phoneNumber)
            }
        }
        //Log.i(TAG, "All outgoing Numbers length:${allDistinctNumbers.size}, ${allDistinctNumbers}")
        //Log.i(TAG, "All Linearized outgoing numbers: ${allLinearInternationalPhoneNumberList.size}, ${allLinearInternationalPhoneNumberList}")
        //Log.i(TAG, "All outgoing Names len : ${allNamesForAllNumbers}")
        //Log.i(TAG, "All outgoing Image Uri's : ${allImageUrisForAllNumbers}")


        var recyclerView2: RecyclerView = view.findViewById(R.id.recyclerView2)
        recyclerViewAdapterForTab2 = RecyclerViewAdapterForTab2(activity!!.applicationContext, allDistinctNumbers)
        recyclerView2.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView2.setHasFixedSize(true)
        recyclerView2.setItemViewCacheSize(20)
        recyclerView2.isDrawingCacheEnabled = true
        recyclerView2.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        recyclerView2.adapter = recyclerViewAdapterForTab2
        registerForContextMenu(recyclerView2)

        return view
    }

    private fun extractNumberFromRecordedFileNameForOutgoingCalls(allAudioFilesPath: MutableList<String>) : MutableList<String> {
        var extractedNumberFromRecordedFilename: MutableList<String> = mutableListOf()
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

            if(itExtracted.contains("outgoing") && itExtracted.contains("+91")) {
                var extractedNumber: String = ""
                for(position in 28..itExtracted.length - 1) {
                    if((itExtracted)[position] == '_') {
                        break
                    }
                    extractedNumber = extractedNumber + itExtracted[position]
                }
                //Log.i(TAG,"outgoing extracted Number from +91 : $extractedNumber")
                extractedNumberFromRecordedFilename.add(extractedNumber)
                extractedNumber = ""

            } else if(itExtracted.contains("outgoing") && !itExtracted.contains("+91")){
                var extractedNumber: String = ""
                for(position in 25..itExtracted.length - 1) {
                    if((itExtracted)[position] == '_') {
                        break
                    }
                    extractedNumber = extractedNumber + itExtracted[position]
                }
                //Log.i(TAG,"outgoing extracted Number from : $extractedNumber")
                extractedNumberFromRecordedFilename.add(extractedNumber)
            }
        }
        //Log.i(TAG, "number of outgoing calls : ${extractedNumberFromRecordedFilename.size}")
        //Log.i(TAG, "outgoing calls list : $extractedNumberFromRecordedFilename")
        return extractedNumberFromRecordedFilename
    }


    private fun getAllAudioFilesDirectory(): MutableList<String> {
        var allAudioFilesDirectory: MutableList<String> = mutableListOf()
        var recordPath = Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/"
        File(recordPath).walk().forEach {
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
        recyclerViewAdapterForTab2.textEntryFindFilter(enteredData, newUpdatedContactsList)
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
        recyclerViewAdapterForTab2.updateSearchFilterListForTab2(allDistinctNumbersFilterList)
        */
        return true
    }
}
