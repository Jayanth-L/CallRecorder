package com.jayanthl.callrecorder.callrecorderandsaver.AdapterForListingCalls


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.jayanthl.callrecorder.callrecorderandsaver.R
import com.jayanthl.callrecorder.callrecorderandsaver.RecordplayActivity
import java.io.File

class RecyclerViewOutGoingAdapter(mContext: Context, imageUri: String, name: String, phoneNumber: String, outGoingNumberFilePaths: MutableList<String>, outGoingFileLastAccessedTime: MutableList<String>) : RecyclerView.Adapter<RecyclerViewOutGoingAdapter.OutgoingViewHolder> (){

    val mContext: Context
    val imageUri: String
    val name: String
    val phoneNumber: String
    val outGoingNumberFilePaths: MutableList<String>
    val outGoingFileLastAccessedTime: MutableList<String>

    init {
        this.mContext = mContext
        this.imageUri = imageUri
        this.name = name
        this.phoneNumber = phoneNumber
        this.outGoingNumberFilePaths = outGoingNumberFilePaths.asReversed()
        this.outGoingFileLastAccessedTime = outGoingFileLastAccessedTime.asReversed()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutgoingViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_outgoingincominglayout, parent, false)
        val holder = OutgoingViewHolder(view)
        return holder
    }

    override fun getItemCount(): Int {
        return outGoingNumberFilePaths.size
    }

    override fun onBindViewHolder(holder: OutgoingViewHolder, position: Int) {
        holder.nameOrnumber.text = "You ------> $name"
        if(!imageUri.equals("")) {
            holder.imageView.setImageURI(Uri.parse(imageUri))
        }

        holder.createdTime.text = outGoingFileLastAccessedTime[position]
        holder.linearLayoutincomingoutgoing.setOnClickListener(View.OnClickListener {
            val audioFilePlayIntent = Intent(mContext, RecordplayActivity::class.java)
            audioFilePlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            audioFilePlayIntent.putExtra("audioFileName", outGoingNumberFilePaths[position])
            mContext.startActivity(audioFilePlayIntent)
        })

        holder.linearLayoutincomingoutgoing.setOnLongClickListener(View.OnLongClickListener {
            var popupMenu = PopupMenu(mContext, it)
            var menuInflater: MenuInflater = popupMenu.menuInflater
            menuInflater.inflate(R.menu.firstactivity_popupmenu, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.action_delete -> {
                        if(File(outGoingNumberFilePaths[position]).exists()) {
                            File(outGoingNumberFilePaths[position]).delete()
                            outGoingNumberFilePaths.removeAt(position)
                            outGoingFileLastAccessedTime.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, outGoingFileLastAccessedTime.size)
                            notifyItemRangeChanged(position, outGoingNumberFilePaths.size)
                            Toast.makeText(mContext, "recording deleted", Toast.LENGTH_SHORT).show()
                        }
                        return@setOnMenuItemClickListener true
                    }

                    R.id.action_share -> {
                        Toast.makeText(mContext, "Clickod on Share", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }

            return@OnLongClickListener true
        })
    }

    class OutgoingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var imageView: de.hdodenhof.circleimageview.CircleImageView
        var nameOrnumber: TextView
        var createdTime: TextView
        var linearLayoutincomingoutgoing: LinearLayout
        init {
            this.imageView = itemView.findViewById(R.id.imageViewOutgoingIncoming)
            this.nameOrnumber = itemView.findViewById(R.id.textViewOutgoingIncomingSource)
            this.createdTime = itemView.findViewById(R.id.textViewOutgoingTimeCreated)
            this.linearLayoutincomingoutgoing = itemView.findViewById(R.id.linearLayoutOutgoingIncoming)
        }
    }
}