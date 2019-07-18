package com.jayanthl.callrecorder.callrecorderandsaver.AdapterForListingCalls

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.jayanthl.callrecorder.callrecorderandsaver.R
import com.jayanthl.callrecorder.callrecorderandsaver.RecordplayActivity
import java.io.File

class RecyclerViewIncomingAdapter(mContext: Context, imageUri:String, name: String, phoneNumber:String, incomingNumberFilePaths: MutableList<String>, incomingFileLastAccessedTime: MutableList<String>): RecyclerView.Adapter<RecyclerViewIncomingAdapter.IncomingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_incomingoutgoinglayout, parent, false)
        val holder = IncomingViewHolder(view)
        return holder
    }

    override fun getItemCount(): Int {
        return incomingNumberFilePaths.size
    }

    override fun onBindViewHolder(holder: IncomingViewHolder, position: Int) {
        holder.nameOrnumber.text = "$name -------> You"
        if(!imageUri.equals("")) {
            holder.imageView.setImageURI(Uri.parse(imageUri))
        }

        holder.createdTime.text = incomingFileLastAccessedTime[position]
        holder.linearLayoutincomingoutgoing.setOnClickListener(View.OnClickListener {
            val audioFilePlayIntent = Intent(mContext, RecordplayActivity::class.java)
            audioFilePlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            audioFilePlayIntent.putExtra("audioFileName", incomingNumberFilePaths[position])
            mContext.startActivity(audioFilePlayIntent)
        })

        holder.linearLayoutincomingoutgoing.setOnLongClickListener(View.OnLongClickListener {
            var popupMenu = PopupMenu(mContext, it)
            val menuInflater: MenuInflater = popupMenu.menuInflater
            menuInflater.inflate(R.menu.firstactivity_popupmenu, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.action_delete -> {
                        if(File(incomingNumberFilePaths[position]).exists()) {
                            File(incomingNumberFilePaths[position]).delete()
                            incomingNumberFilePaths.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, incomingFileLastAccessedTime.size)
                            notifyItemRangeChanged(position, incomingNumberFilePaths.size)
                            Toast.makeText(mContext, "recording deleted", Toast.LENGTH_SHORT).show()
                        }
                        return@setOnMenuItemClickListener true
                    }

                    R.id.action_share -> {
                        Toast.makeText(mContext, "Clicked on Share", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    } else -> return@setOnMenuItemClickListener false
                }
            }
            return@OnLongClickListener true
        })

    }

    val mContext: Context
    val imageUri: String
    val name: String
    val phoneNumber: String
    val incomingNumberFilePaths: MutableList<String>
    val incomingFileLastAccessedTime: MutableList<String>

    init {
        this.mContext = mContext
        this.imageUri = imageUri
        this.name = name
        this.phoneNumber = phoneNumber
        this.incomingFileLastAccessedTime = incomingFileLastAccessedTime.asReversed()
        this.incomingNumberFilePaths = incomingNumberFilePaths.asReversed()
    }






    class IncomingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var imageView: de.hdodenhof.circleimageview.CircleImageView
        var nameOrnumber: TextView
        var createdTime: TextView
        var linearLayoutincomingoutgoing: LinearLayout
        init {
            this.imageView = itemView.findViewById(R.id.imageViewIncomingOutgoing)
            this.nameOrnumber = itemView.findViewById(R.id.textViewIncomingOutgoingSource)
            this.createdTime = itemView.findViewById(R.id.textViewIncomingTimeCreated)
            this.linearLayoutincomingoutgoing = itemView.findViewById(R.id.linearLayoutIncomingOutgoing)
        }
    }
}