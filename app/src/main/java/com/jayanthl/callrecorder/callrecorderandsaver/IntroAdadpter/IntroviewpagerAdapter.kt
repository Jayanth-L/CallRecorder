package com.jayanthl.callrecorder.callrecorderandsaver.IntroAdadpter

import android.content.Context
import android.database.DataSetObserver
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class IntroviewpagerAdapter(mContext: Context, integerArray: IntArray): PagerAdapter() {

    var integerArray: IntArray
    lateinit var layoutInflater: LayoutInflater
    var mContext: Context
    init {
        this.integerArray = integerArray
        this.mContext = mContext
        layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    }

    override fun getCount(): Int {
        return integerArray.size
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0 == p1
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view: View = layoutInflater.inflate(integerArray[position], container, false)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        var view: View = `object` as View
        container.removeView(view)
    }
}