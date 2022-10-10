package com.ms.quokkaism.ui.sidemenu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.ms.quokkaism.R
import com.ms.quokkaism.model.Profile

class SideMenuAdapter(private val onItemClickListener: OnItemClickListener?) : RecyclerView.Adapter<SideMenuAdapter.ViewHolder>() {

    private val authItemList = arrayOf(R.string.home,R.string.likes,R.string.setting,R.string.about_us)
    private val guestItemList = arrayOf(R.string.home,R.string.setting,R.string.about_us)
    private var itemList : Array<Int>
    private var selectedPosition = 0

    init {
        itemList = if(Profile.isUserGuest().not()) {
            authItemList
        } else {
            guestItemList
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_side_menu,parent,false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView?.setText(itemList[position])
        holder.itemView.isSelected = position == selectedPosition
        holder.itemView.setOnClickListener {
            it.context?.let {
                onItemClickListener?.onItemClick(holder.adapterPosition,itemList[position])
            }
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    fun selectItem(@StringRes item: Int) {
        selectedPosition = if(itemList.contains(item)) {
            itemList.indexOf(item)
        } else {
            -1
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView as? MaterialTextView
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int,@StringRes item: Int)
    }
}