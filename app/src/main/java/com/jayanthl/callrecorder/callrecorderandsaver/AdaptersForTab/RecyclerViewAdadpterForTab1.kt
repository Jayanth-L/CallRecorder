package com.jayanthl.callrecorder.callrecorderandsaver.AdaptersForTab


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import android.widget.*
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.jayanthl.callrecorder.callrecorderandsaver.OutgoingCallsActivity
import com.jayanthl.callrecorder.callrecorderandsaver.R


class RecyclerViewAdadpterForTab1(mContext: Context, incomingPhoneNumbersList: MutableList<String>) : RecyclerView.Adapter<RecyclerViewAdadpterForTab1.Tab1FragmentsViewHolder>() {

    var mContext: Context
    var incomingPhoneNumbersList: MutableList<String>
    var phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    var allContacts: List<Contact> = Contacts.getQuery().find()
    var copiedIncomingNumberForNumberListeFilter: MutableList<String>

    init {
        this.mContext = mContext
        this.incomingPhoneNumbersList = incomingPhoneNumbersList
        this.copiedIncomingNumberForNumberListeFilter = incomingPhoneNumbersList
    }

    override fun getItemCount(): Int {
        return incomingPhoneNumbersList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Tab1FragmentsViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.items_tablayout1, parent, false)
        var holder = Tab1FragmentsViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: Tab1FragmentsViewHolder, position: Int) {
        Log.i("RecyclerViewAdapterTab1", "Loading view $position")


        var numbername = ""
        var photouri = ""
        for(contacts in allContacts) {
            if(contacts.phoneNumbers.isNotEmpty()) {
                if(contacts.phoneNumbers[0].number != null) {
                    var inContactsPhoneNumner = contacts.phoneNumbers[0].number
                    if(!inContactsPhoneNumner.contains("*") && !inContactsPhoneNumner.contains("#")) {
                        try {
                            if (phoneUtil.format(phoneUtil.parse(contacts.phoneNumbers[0].number, "IN"), PhoneNumberUtil.PhoneNumberFormat.E164).contains(incomingPhoneNumbersList[position])) {
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

        if (numbername.equals("")) {
            holder.textVew.text = incomingPhoneNumbersList[position] + "'s Call Recordings"
        } else {
            holder.textVew.text = numbername + "'s Call Recordings"
        }

        if (!photouri.equals("")) {
            holder.imageView.setImageURI(Uri.parse(photouri))
        } else {
            holder.imageView.setImageResource(R.drawable.ic_account_circle_black_24dp)
        }

        holder.linearLayout.setOnClickListener(View.OnClickListener {

            val recordViewIntent = Intent(mContext, OutgoingCallsActivity::class.java)
            if(numbername.equals("")) {
                recordViewIntent.putExtra("record_name", incomingPhoneNumbersList[position])
            } else {
                recordViewIntent.putExtra("record_name", numbername)
            }
            recordViewIntent.putExtra("record_phone_number", incomingPhoneNumbersList[position])
            recordViewIntent.putExtra("record_type", "incoming")
            recordViewIntent.putExtra("record_image_uri", photouri)
            recordViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(recordViewIntent)
        })

        holder.linearLayout.setOnLongClickListener(View.OnLongClickListener {
            Toast.makeText(mContext, "Long click", Toast.LENGTH_LONG).show()
            return@OnLongClickListener true
        })
    }


    class Tab1FragmentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var textVew: TextView
        var linearLayout: LinearLayout

        init {
            this.imageView = itemView.findViewById(R.id.imageView)
            this.textVew = itemView.findViewById(R.id.textView1)
            this.linearLayout = itemView.findViewById(R.id.linearLayout)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateSearchFilterListForTab1(incomingPhoneNumbersListFilter: MutableList<String>) {
        this.incomingPhoneNumbersList = incomingPhoneNumbersListFilter
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
        for(contacts in copiedIncomingNumberForNumberListeFilter) {
            if(contacts.contains(enteredData!!)) {
                newFilterPhoneNumberList.add(contacts)
            } else if(getNameFromPhoneNumber(contacts).toLowerCase().contains(enteredData.toLowerCase())) {
                newFilterPhoneNumberList.add(contacts)
            }
        }

        this.incomingPhoneNumbersList = newFilterPhoneNumberList
        notifyDataSetChanged()
    }
}