package com.jayanthl.callrecorder.callrecorderandsaver

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.preference.PreferenceGroup
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.Toast
import com.github.tamir7.contacts.Contacts
import com.jayanthl.callrecorder.callrecorderandsaver.FragmentAdapter.SectionsFragmentPagerAdapter
import com.jayanthl.callrecorder.callrecorderandsaver.Fragments.Tab1Fragment
import com.jayanthl.callrecorder.callrecorderandsaver.Fragments.Tab2Fragment
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.io.File
import android.support.v7.app.AlertDialog.Builder
import android.view.*
import android.widget.ProgressBar
import com.github.tamir7.contacts.Contact
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.LicenserDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    lateinit var mDrawerLayout: DrawerLayout
    lateinit var dialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //checking if the directories are initiated or not


        //sharedpreferences for detecting incoming or outgoing calls
        var sharedPreferences = getSharedPreferences("callDataType", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("callDataType", "")
        editor.putString("outgoingcall_started", "")
        editor.putString("outgoing_number", "")
        editor.putString("incoimng_number", "")
        editor.commit()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var permissionArrayList: ArrayList<String> = ArrayList()
            permissionArrayList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissionArrayList.add(android.Manifest.permission.RECORD_AUDIO)
            permissionArrayList.add(android.Manifest.permission.PROCESS_OUTGOING_CALLS)
            permissionArrayList.add(android.Manifest.permission.READ_PHONE_STATE)
            permissionArrayList.add(Manifest.permission.READ_CONTACTS)

            //ToDo: builder message explaining permissions usages

            if (!hasPermissions(this, permissionArrayList)) {
                val givePermissionInSliderIntent = Intent(this, IntroActivity::class.java)
                givePermissionInSliderIntent.putExtra("asking_permission", true)
                startActivity(givePermissionInSliderIntent)
                finish()
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.PROCESS_OUTGOING_CALLS, android.Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS), 1001)
            } else {
                //Initilize Contacts Library

                //checking / init directories
                val recordsDirectory = Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/"
                if(!File(recordsDirectory).exists()) {
                    File(recordsDirectory).mkdir()
                }
                Log.i(TAG, " Initializing Contacts Library")
                Contacts.initialize(this)
            }
        } else {
            val recordsDirectory = Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/"
            if(!File(recordsDirectory).exists()) {
                File(recordsDirectory).mkdir()
            }
            Log.i(TAG, "Initializing contacts for older api's")
            Contacts.initialize(this)
        }

        //progress Dialog for Executing init
        val isFirstTime = intent.getBooleanExtra("first_time", false)
        if(!isFirstTime) {
            dialog = ProgressDialog(this)
            dialog.setMessage("Loading Recordings List...")
            dialog.setCancelable(false)
            dialog.setInverseBackgroundForced(false)
            dialog.show()
            Handler().postDelayed({
                dialog.hide()
            }, 4000)
        }

        if(isFirstTime) {
            //Toast.makeText(this, " Call recordings is disabled by default enable in the settings", Toast.LENGTH_SHORT).show()
            val preferencesSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            if(!preferencesSharedPreferences.getBoolean("recordingswitch", false)) {
                val editor = preferencesSharedPreferences.edit()
                editor.remove("recordingswitch")
                if(!preferencesSharedPreferences.getBoolean("notificationswitch", false)) {
                    editor.remove("notificationswitch")
                    editor.putBoolean("notificationswitch", true)
                }
                editor.putBoolean("recordingswitch", true)
                editor.commit()
            }
            Log.i(TAG, "executing mainactivity is FirstTime")
            Toast.makeText(this, "All your calls will be listed here once recorded :)", Toast.LENGTH_LONG).show()

            Handler().postDelayed({

                val view: View = LayoutInflater.from(this).inflate(R.layout.termsagree_layout, null)
                val builder = Builder(this)
                builder.setView(view).setTitle("Terms :)").setPositiveButton("I'm good to go!", DialogInterface.OnClickListener { dialog, _->

                    dialog.dismiss()
                })
                builder.create().show()

            }, 4500)
        }

        //setting up tool bar
        val toolbar: android.support.v7.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        //Navigation Drawer
        mDrawerLayout = findViewById(R.id.drawer_layout)


        //listener for navigationView
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_rateus -> {
                    var appUri = Uri.parse("market://details?id=${this.packageName}")
                    var appstoreIntent = Intent(Intent.ACTION_VIEW, appUri)
                    drawer_layout.closeDrawers()
                    try {
                        startActivity(appstoreIntent)
                        Toast.makeText(this, "Thankyou for rating us :)", Toast.LENGTH_SHORT).show()
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this, " Play store is't installed", Toast.LENGTH_SHORT).show()
                    }
                    return@setNavigationItemSelectedListener true
                }

                R.id.action_delete -> {
                    drawer_layout.closeDrawers()
                    Handler().postDelayed({
                        val view: View = LayoutInflater.from(this).inflate(R.layout.deleteconfirmation_layout, null)
                        val builder = Builder(this)
                        builder.setView(view).setTitle("Delete Recordings").setPositiveButton("yes", DialogInterface.OnClickListener { dialog, _->
                            File(Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/").walk().forEach {
                                if(it.toString().contains("incoming") || it.toString().contains("outgoing")) {
                                    it.delete()
                                }
                            }
                            dialog.dismiss()
                            Toast.makeText(this, "All recordings deleted successfully!", Toast.LENGTH_LONG).show()
                        })
                                .setNegativeButton("No", DialogInterface.OnClickListener{ dialog, _->
                                    dialog.dismiss()
                                })
                        builder.create().show()
                    }, 700)
                    return@setNavigationItemSelectedListener true
                }

                R.id.action_sharetheapp -> {

                    try {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.setType("text/plain")
                        intent.putExtra(Intent.EXTRA_SUBJECT, " Call Recorder and saver")
                        var expln = "\nHey Guys try this new Awesome Call recording App, Record All Important calls :)\n\n"
                        expln += "https://play.google.com/store/apps/details?id=${this.packageName}"
                        intent.putExtra(Intent.EXTRA_TEXT, expln)
                        startActivity(Intent.createChooser(intent, "choose an app to share :)"))
                    } catch (e: Exception) {}

                    drawer_layout.closeDrawers()
                    return@setNavigationItemSelectedListener true
                }


                R.id.action_navigationsettings -> {
                    drawer_layout.closeDrawers()
                    Handler().postDelayed({
                        val appSettingsIntent = Intent(this, Appsettingsactivity::class.java)
                        startActivity(appSettingsIntent)
                    }, 350)
                    return@setNavigationItemSelectedListener true
                }
                else -> return@setNavigationItemSelectedListener false
            }
        }

        //from here the UI part starts
        setSupportActionBar(toolbar)
        initiateViewPager(container)
        tabs.setupWithViewPager(container)

        //Now Gathering all the saved Files

        var recorderFilesList = getAllSavedRecordList()
        Log.i(TAG, "recordedCallList :${recorderFilesList}")

        var filteredOutgoingCallsList = filteredOutgoingcalls(recorderFilesList) //Got All Outgoing Values for fragment 2
        Log.i(TAG, "Outgoing Calls: ${filteredOutgoingCallsList}")

        var filteredIncomingCallsList = filteredInComingcalls(recorderFilesList) //Got All Incoming Values for fragment 1
        Log.i(TAG, "Incoming Calls: ${filteredIncomingCallsList}")
    }

    //From Here Function Definition Starts

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

    fun getAllSavedRecordList(): MutableList<String> {
        var recorderFilesList: MutableList<String> = mutableListOf()
        File(Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/").walk().forEach {
            if (it.toString().endsWith(".amr")) {
                recorderFilesList.add("$it")
            }
        }
        return recorderFilesList
    }

    fun filteredOutgoingcalls(recordedFileList: MutableList<String>): MutableList<String> {
        var filteredOutgoingCallList = mutableListOf<String>()
        for (i in recordedFileList) {
            if (i.contains("outgoing")) {
                filteredOutgoingCallList.add(i)
            }
        }
        return filteredOutgoingCallList
    }

    fun filteredInComingcalls(recordedFileList: MutableList<String>): MutableList<String> {
        var filteredIncomngCalls = mutableListOf<String>()
        for (i in recordedFileList) {
            if (i.contains("incoming")) {
                filteredIncomngCalls.add(i)
            }
        }
        return filteredIncomngCalls
    }

    //perfect for getting names from the phonenumbers

    fun getNameFromPhoneNumber(contactNumber: String): String {
        var allContacts: List<Contact> = Contacts.getQuery().find()
        for (contacts in allContacts) {
            if (contacts.phoneNumbers.isNotEmpty()) {
                if (contacts.phoneNumbers[0].normalizedNumber != null) {
                    if (contacts.phoneNumbers[0].normalizedNumber.contains("8050363665")) {
                        return contacts.displayName
                    }
                }
            }
        }
        return ""
    }

    //supportActionBar menu handler
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater: MenuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.actionbar_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {

            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
            }

            R.id.action_about -> {
                val view: View = LayoutInflater.from(this).inflate(R.layout.about_layout, null)
                val builder = Builder(this)
                builder.setView(view).setTitle("About").setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                })
                builder.create().show()
            }

            R.id.action_deleteallrecordings -> {
                val view: View = LayoutInflater.from(this).inflate(R.layout.deleteconfirmation_layout, null)
                val builder = Builder(this)
                builder.setView(view).setTitle("Delete ?").setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
                    File(Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/").walk().forEach {
                        if (it.toString().contains("incoming") || it.toString().contains("outgoing")) {
                            it.delete()
                        }
                    }
                    Toast.makeText(this, "All call Recodings Deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    //Refreshing Fragments
                    val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    try {
                        fragmentTransaction.detach(supportFragmentManager.fragments[0])
                        fragmentTransaction.attach(supportFragmentManager.fragments[0])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        fragmentTransaction.detach(supportFragmentManager.fragments[1])
                        fragmentTransaction.attach(supportFragmentManager.fragments[1])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    fragmentTransaction.commit()
                })
                        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                            Toast.makeText(this, "Didn't delete anything", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        })
                builder.create().show()
            }

            R.id.action_privacypolicy -> {
                val privacyPolicyUrl = "https://sites.google.com/view/callrecorderandsaverjayanthl/home"
                val privacyPolicyLinkIntent = Intent(Intent.ACTION_VIEW)
                privacyPolicyLinkIntent.data = Uri.parse(privacyPolicyUrl)
                startActivity(privacyPolicyLinkIntent)

            }

            R.id.action_deleteincoming -> {
                val view: View = LayoutInflater.from(this).inflate(R.layout.deleteincomingbuilder_layout, null)
                val builder = Builder(this)
                builder.setView(view).setTitle("Delete Incoming records ?").setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, _ ->
                    File(Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/").walk().forEach {
                        if (it.toString().contains("incoming")) {
                            it.delete()
                        }
                    }
                    Toast.makeText(this, "All incoming calls deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    //Refreshing Fragments
                    var fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    try {
                        fragmentTransaction.detach(supportFragmentManager.fragments[0])
                        fragmentTransaction.attach(supportFragmentManager.fragments[0])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        fragmentTransaction.detach(supportFragmentManager.fragments[1])
                        fragmentTransaction.attach(supportFragmentManager.fragments[1])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    fragmentTransaction.commit()
                })
                        .setNegativeButton("NO", DialogInterface.OnClickListener { dialog, _ ->
                            dialog.dismiss()
                        })
                builder.create().show()
                return true
            }

            R.id.action_deleteoutgoing -> {
                val view: View = LayoutInflater.from(this).inflate(R.layout.deleteoutgoingbuilder_layout, null)
                val builder = Builder(this)
                builder.setView(view).setTitle("Delete Outgoing records ?").setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, _ ->
                    File(Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/").walk().forEach {
                        if (it.toString().contains("outgoing")) {
                            it.delete()
                        }
                    }
                    dialog.dismiss()
                    Toast.makeText(this, "All outgoing records deleted successfully", Toast.LENGTH_SHORT).show()

                    // Refreshing Fragments
                    var fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    try {
                        fragmentTransaction.detach(supportFragmentManager.fragments[0])
                        fragmentTransaction.attach(supportFragmentManager.fragments[0])
                    } catch (e: Exception) {
                        Log.i("Fragmentman", "Attaching detaching error 0")
                    }

                    try {
                        fragmentTransaction.detach((supportFragmentManager.fragments[1]))
                        fragmentTransaction.attach(supportFragmentManager.fragments[1])
                    } catch (e: Exception) {
                        Log.i("Fragmentman", "Attaching detaching error 1")
                    }

                    fragmentTransaction.commit()
                })
                        .setNegativeButton("NO", DialogInterface.OnClickListener { dialog, _ ->
                            dialog.dismiss()
                        })

                builder.create().show()
                return true
            }

            R.id.action_actionbarsettings -> {
                val appSettingsIntent = Intent(this, Appsettingsactivity::class.java)
                startActivity(appSettingsIntent)
            }

            R.id.action_refresh -> {
                // Refreshing Fragments
                var fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                try {
                    fragmentTransaction.detach(supportFragmentManager.fragments[0])
                    fragmentTransaction.attach(supportFragmentManager.fragments[0])
                } catch (e: Exception) {
                    Log.i("Fragmentman", "Attaching detaching error 0")
                }

                try {
                    fragmentTransaction.detach((supportFragmentManager.fragments[1]))
                    fragmentTransaction.attach(supportFragmentManager.fragments[1])
                } catch (e: Exception) {
                    Log.i("Fragmentman", "Attaching detaching error 1")
                }

                fragmentTransaction.commit()
                Toast.makeText(this, "Refreshed Recording list", Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.action_opensourcelicenses -> {
                val licenseDialog = LicenserDialog(this)
                licenseDialog.setTitle("Open Source Licenses")
                        .setCustomNoticeTitle("Notice for Files")
                        .setLibrary(Library("Android Support Libraries", "https://developer.android.com/topic/libraries/support-library/index.html", License.APACHE))
                        .setLibrary(Library("CircleImageView, Copyright 2014 - 2018 Henning Dodenhof ", "https://github.com/hdodenhof/CircleImageView", License.APACHE))
                        .setLibrary(Library("Contacts, Copyright Tamir Shomer 2016", "https://github.com/tamir7/Contacts", License.APACHE))
                        .setLibrary(Library("Licenser", "https://github.com/marcoscgdev/Licenser", License.MIT))
                        .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener({dialog, _->
                            dialog.dismiss()
                        }))
                        .show()
            }


            else -> {
                //Do nothing;
            }
        }
        return false
    }

    fun initiateViewPager(viewPager: ViewPager) {
        var sectionsFragmentPagerAdapter = SectionsFragmentPagerAdapter(supportFragmentManager)
        sectionsFragmentPagerAdapter.addFragment(Tab1Fragment(), "Incoming")
        sectionsFragmentPagerAdapter.addFragment(Tab2Fragment(), "Outgoing")
        viewPager.adapter = sectionsFragmentPagerAdapter
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        var permissionNotProvided = false
        if(grantResults.size > 0) {
            for (resultsPosition in 0..grantResults.size -1) {
                if(grantResults[resultsPosition] != PackageManager.PERMISSION_GRANTED) {
                    permissionNotProvided = true
                    break
                }
            }
            if(permissionNotProvided) {
                Log.i(TAG, "All permissions are not given")
            } else {
                Log.i(TAG, "Permission Granted")
            }
        }
    }

    override fun onBackPressed() {
        try {

        } catch (e: Exception) {}
        super.onBackPressed()
    }

    override fun onPause() {
        try {
            dialog.dismiss()
        } catch (e: Exception) {}
        super.onPause()
    }

    override fun onDestroy() {
        try {
            dialog.dismiss()
        } catch (e: Exception) {}
        super.onDestroy()
    }
}