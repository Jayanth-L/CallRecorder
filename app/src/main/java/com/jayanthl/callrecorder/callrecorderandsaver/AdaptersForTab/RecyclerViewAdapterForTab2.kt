package com.jayanthl.callrecorder.callrecorderandsaver.AdaptersForTab


import android.content.Context
import android.content.Intent
import android.media.ImageReader
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.jayanthl.callrecorder.callrecorderandsaver.IncomingcallsActivity
import com.jayanthl.callrecorder.callrecorderandsaver.R

class RecyclerViewAdapterForTab2(mContext: Context, outcomingPhoneNumbersList: MutableList<String>) : RecyclerView.Adapter<RecyclerViewAdapterForTab2.Tab2FragmentsViewHolder>() {

    var mContext: Context
    var outcomingPhoneNumbersList: MutableList<String>
    var copiedOutGoingNumberForNumberListeFilter: MutableList<String>
    var phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    var allContacts: List<Contact> = Contacts.getQuery().find()
    init {
        this.mContext = mContext
        this.outcomingPhoneNumbersList = outcomingPhoneNumbersList
        this.copiedOutGoingNumberForNumberListeFilter = outcomingPhoneNumbersList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Tab2FragmentsViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.items_tablayout1, parent, false)
        val holder = Tab2FragmentsViewHolder(view)
        return holder
    }

    override fun getItemCount(): Int {
        return outcomingPhoneNumbersList.size
    }

    override fun onBindViewHolder(holder: Tab2FragmentsViewHolder, position: Int) {

        var numbername = ""
        var photouri = ""
        for(contacts in allContacts) {
            if(contacts.phoneNumbers.isNotEmpty()) {
                if(contacts.phoneNumbers[0].number != null) {
                    var inContactsPhoneNumner = contacts.phoneNumbers[0].number
                    if(!inContactsPhoneNumner.contains("*") && !inContactsPhoneNumner.contains("#")) {
                        try {
                            if (phoneUtil.format(phoneUtil.parse(contacts.phoneNumbers[0].number, "IN"), PhoneNumberUtil.PhoneNumberFormat.E164).contains(outcomingPhoneNumbersList[position])) {
                                if (contacts.photoUri == null) {
                                    photouri = ""
                                } else {
                                    photouri = contacts.photoUri
                                }
                                numbername = contacts.displayName
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
        }


        Log.i("RecyclerViewAdapterTab2", "Loading view $position")
        if(numbername.equals("")) {
            holder.textVew.text = outcomingPhoneNumbersList[position] + "'s Call Recordings"
        } else {
            holder.textVew.text = numbername + "'s Call Recordings"
        }

        if(!photouri.equals("")) {
            holder.imageView.setImageURI(Uri.parse(photouri))
        } else {
            holder.imageView.setImageResource(R.drawable.ic_account_circle_black_24dp)
        }

        holder.linearLayout.setOnClickListener(View.OnClickListener {
            val recordViewIntent = Intent(mContext, IncomingcallsActivity::class.java)
            if(numbername.equals("")) {
                recordViewIntent.putExtra("record_name", outcomingPhoneNumbersList[position])
            } else {
                recordViewIntent.putExtra("record_name", numbername)
            }

            recordViewIntent.putExtra("record_phone_number", outcomingPhoneNumbersList[position])
            recordViewIntent.putExtra("record_type", "outgoing")
            recordViewIntent.putExtra("record_image_uri", photouri)
            recordViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(recordViewIntent)
        })
    }



    class Tab2FragmentsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var textVew: TextView
        var linearLayout: LinearLayout
        var imageView: de.hdodenhof.circleimageview.CircleImageView
        init {
            this.textVew = itemView.findViewById(R.id.textView1)
            this.linearLayout = itemView.findViewById(R.id.linearLayout)
            this.imageView = itemView.findViewById(R.id.imageView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateSearchFilterListForTab2(outgoingPhoneNumberListFilter: MutableList<String>) {
        this.outcomingPhoneNumbersList = outgoingPhoneNumberListFilter
        notifyDataSetChanged()
    }

    private fun getNameFromPhoneNumber(contactNumber: String): String {
        for (contacts in allContacts) {
            if (contacts.phoneNumbers.isNotEmpty()) {
                if (contacts.phoneNumbers[0].number != null) {
                    var inContactsPhoneNumner = contacts.phoneNumbers[0].number
                    if(!inContactsPhoneNumner.contains("*") && !inContactsPhoneNumner.contains("#")) {
                        try {
                            if (phoneUtil.format(phoneUtil.parse(contacts.phoneNumbers[0].number, "IN"), PhoneNumberUtil.PhoneNumberFormat.E164).contains(contactNumber)) {
                                return contacts.displayName
                            }
                        } catch(e: RuntimeException)  { }
                    }
                }
            }
        }
        return ""
    }

    fun textEntryFindFilter(enteredData: String?, newFilterPhoneNumberList: MutableList<String>) {
        for(contacts in copiedOutGoingNumberForNumberListeFilter) {
            if(contacts.contains(enteredData!!)) {
                newFilterPhoneNumberList.add(contacts)
            } else if(getNameFromPhoneNumber(contacts).toLowerCase().contains(enteredData.toLowerCase())) {
                newFilterPhoneNumberList.add(contacts)
            }
        }

        this.outcomingPhoneNumbersList = newFilterPhoneNumberList
        notifyDataSetChanged()
    }
}