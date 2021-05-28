package com.example.epledger.inbox.event.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.R
import com.example.epledger.inbox.event.item.EventItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.view_event_item_row.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EventAdapter: RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    // Always a copy (Pointer-copy is cheap)
    var eventItems = ArrayList<EventItem>()
    var recyclerView: RecyclerView? = null

    interface OnPositionClickListener {
        fun onClick(position: Int)
    }

    lateinit var onPositionClickListener: OnPositionClickListener

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.event_item_row_title
        val info: TextView = view.event_item_row_info
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_event_item_row, parent, false)
        view.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (eventItems.size == 0) 1 else eventItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ctx = holder.itemView.context

        // 列表为空，显示提示语，取消所有回调
        if (eventItems.size == 0) {
            holder.itemView.event_item_row_title.setText("")
            holder.itemView.event_item_row_icon_image.setImageDrawable(null)
            holder.itemView.event_item_row_title.layoutParams.height = 1
            holder.itemView.event_item_row_info.setText(ctx.getString(R.string.no_events_prompt))
            holder.itemView.setOnLongClickListener(null)
            holder.itemView.setOnClickListener(null)
            return
        }

        val item = eventItems[position]

        holder.title.setText(item.name)

        val simpleFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        val dateStr = simpleFormat.format(item.startingDate)
        val cycleAmount = item.cycle
        val cycleUnit = when(item.unit) {
            EventItem.CycleUnit.DAY -> ctx.getString(R.string.unit_day)
            EventItem.CycleUnit.MONTH -> ctx.getString(R.string.unit_month)
            EventItem.CycleUnit.YEAR -> ctx.getString(R.string.unit_year)
        }
        holder.info.setText(String.format(ctx.getString(R.string.event_info_fmt), dateStr, cycleAmount, cycleUnit))

        val drawable = item.iconResID?.let { ctx.getDrawable(it) }
        holder.itemView.event_item_row_icon_image.setImageDrawable(drawable)

        // 设置监听
        holder.itemView.setOnLongClickListener {
            val dialog = MaterialAlertDialogBuilder(ctx)
                    .setMessage(ctx.getString(R.string.event_del_comfirm))
                    .setPositiveButton(ctx.getString(R.string.ok)) { d, i ->
                        Toast.makeText(ctx, "Should remove!", Toast.LENGTH_SHORT).show()

                        // 列表视图的删除
                        eventItems.removeAt(position)
                        this.notifyItemRemoved(position)
                        this.notifyItemRangeChanged(position, itemCount)

                        // TODO: 删除和该事件相关的已注册通知
                    }
                    .setNegativeButton(ctx.getString(R.string.no)) { d, i -> /* nothing */}
            dialog.show()
            true
        }

        holder.itemView.setOnClickListener {
            onPositionClickListener.onClick(position)
        }
    }
}