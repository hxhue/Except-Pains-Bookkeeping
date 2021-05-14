package com.example.epledger.inbox.event.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.R
import kotlinx.android.synthetic.main.view_icon.view.*

class IconItemAdapter(private val iconIDArray: Array<Int>, private val positionClickListener: OnPositionClickListener):
        RecyclerView.Adapter<IconItemAdapter.ViewHolder>() {
    
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image: ImageView = view.view_icon_image_view
    }

    interface OnPositionClickListener {
        fun onPositionClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.view_icon, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ctx = holder.itemView.context
        holder.image.setImageDrawable(ctx.getDrawable(iconIDArray[position]))
        holder.itemView.setOnClickListener {
            positionClickListener.onPositionClick(position)
        }
    }

    override fun getItemCount(): Int {
        return iconIDArray.size
    }
}