package com.jayanthl.callrecorder.callrecorderandsaver

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.github.tamir7.contacts.Contacts
import com.jayanthl.callrecorder.callrecorderandsaver.IntroAdadpter.IntroviewpagerAdapter
import com.jayanthl.callrecorder.callrecorderandsaver.helpers.InroductionManager.IntroductionManager
import java.io.File

class IntroActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    lateinit var mViewPager: ViewPager
    lateinit var introviewpagerAdapter: IntroviewpagerAdapter
    private lateinit var Dots_Layout: LinearLayout
    private lateinit var dots: Array<ImageView?>
    val TAG = "IntroActivity"
    lateinit var nextButton: Button
    lateinit var skipButon: Button
    var isAskingPermission: Boolean = false

    private val layouts = intArrayOf(R.layout.firstpresentation_layout, R.layout.secondpresentation_layout, R.layout.thirdpresentation_layout)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        isAskingPermission = intent.getBooleanExtra("asking_permission", false)
        Log.i(TAG, "Is asking permission" + isAskingPermission)
        if(!IntroductionManager(this).checkWhetherFirstTime() && !isAskingPermission) {
            val appIntent = Intent(this, MainActivity::class.java)
            appIntent.putExtra("first_time", false)
            startActivity(appIntent)
            finish()
        }
        setContentView(R.layout.activity_intro)

        mViewPager = findViewById(R.id.welcome_viewpager)
        introviewpagerAdapter = IntroviewpagerAdapter(this, layouts)
        mViewPager.adapter = introviewpagerAdapter

        Dots_Layout = findViewById(R.id.dotsLinearLayout)

        nextButton = findViewById(R.id.button_next)
        skipButon = findViewById(R.id.button_skip)

        nextButton.setOnClickListener(View.OnClickListener {
            nextSlideOpen()
        })



        skipButon.setOnClickListener(View.OnClickListener {
            try {
                val backPage = mViewPager.currentItem -1
                mViewPager.currentItem = backPage
            } catch (e: Exception) {}
        })

        if(isAskingPermission) {
            mViewPager.currentItem = 1
            nextButton.text = "AGREE"
        }

        createDots(0)
        mViewPager.addOnPageChangeListener(this)
        mViewPager.setOnTouchListener(View.OnTouchListener { view, motionEvent -> true })
    }

    private fun createDots(currentPoition: Int) {

        if(Dots_Layout != null) {
            Dots_Layout.removeAllViews()
        }
        dots = arrayOfNulls(layouts.size)

        for (i in layouts.indices) {
            dots[i] = ImageView(this)
            if (i == currentPoition) {
                dots[i]!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.open_dots))
            } else {
                dots[i]!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.close_dots))
            }
            var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(4,0,4,0)
            Dots_Layout.addView(dots[i], params)
        }
    }


    override fun onPageScrollStateChanged(p0: Int) {}

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

    override fun onPageSelected(position: Int) {
        createDots(position)
        if(mViewPager.currentItem == layouts.size -1) {
            nextButton.text = "START"
            skipButon.text = "BACK"
        } else {
            nextButton.text = "Next"
        }
        if(mViewPager.currentItem == 1) {
            nextButton.text = "AGREE"
            skipButon.text = "BACK"
        }
        if(mViewPager.currentItem == 0) {
            skipButon.text = "  Take a Tour"
        }
    }

    fun nextSlideOpen() {
        var nextSlide = mViewPager.currentItem + 1
        if(nextSlide < layouts.size) {
            if(!nextButton.text.equals("AGREE")) {
                mViewPager.currentItem = nextSlide
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var permissionArrayList: ArrayList<String> = ArrayList()
                    permissionArrayList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    permissionArrayList.add(android.Manifest.permission.RECORD_AUDIO)
                    permissionArrayList.add(android.Manifest.permission.PROCESS_OUTGOING_CALLS)
                    permissionArrayList.add(android.Manifest.permission.READ_PHONE_STATE)
                    permissionArrayList.add(Manifest.permission.READ_CONTACTS)

                    //ToDo: builder message explaining permissions usages

                    if (!hasPermissions(this, permissionArrayList)) {
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.PROCESS_OUTGOING_CALLS, android.Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS), 1001)
                    } else {
                        mViewPager.currentItem = nextSlide
                    }
                } else {
                    mViewPager.currentItem = nextSlide
                }
            }
        } else if(nextSlide == layouts.size -1) {
            nextButton.text = "Start"
        } else if(mViewPager.currentItem == layouts.size -1) {
            nextButton.isEnabled = false
            nextButton.text = "Start"
            val startApp = Intent(this, MainActivity::class.java)
            if(isAskingPermission) {
                startApp.putExtra("first_time",false)
            } else {
                startApp.putExtra("first_time", true)
            }
            startActivity(startApp)
            IntroductionManager(this).writePreference()
            finish()
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
                Toast.makeText(this, "Please give the necessary permission", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
                mViewPager.currentItem = 2
                val recordsDirectory = Environment.getExternalStorageDirectory().toString() + "/CallRecordingsAndSaver/"
                if(!File(recordsDirectory).exists()) {
                    File(recordsDirectory).mkdir()
                }
            }
        }
    }
}
