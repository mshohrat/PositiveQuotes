package com.ms.quokkaism.ui.setting.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.R
import com.ms.quokkaism.model.ProfileSetting
import kotlinx.android.synthetic.main.item_setting_time.view.*

class SettingTimeAdapter(private val itemClickListener: OnItemClickListener,private var selectedItem: Int? = -1): RecyclerView.Adapter<SettingTimeAdapter.ViewHolder>() {

    private val itemList: MutableList<Int> = mutableListOf()

    init {
        itemList.add(ProfileSetting.INTERVAL_8_HOURS)
        itemList.add(ProfileSetting.INTERVAL_12_HOURS)
        itemList.add(ProfileSetting.INTERVAL_DAILY)
        itemList.add(ProfileSetting.INTERVAL_3_HOURS)
        itemList.add(ProfileSetting.INTERVAL_4_HOURS)
        itemList.add(ProfileSetting.INTERVAL_6_HOURS)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_setting_time,parent,false))
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.timeTv?.text = itemList[position].toString()
        if(selectedItem == itemList[position])
        {
            holder.itemView.isSelected = true
            holder.timeTv.isSelected = true
            holder.currencyTv.isSelected = true
        }
        else
        {
            holder.itemView.isSelected = false
            holder.timeTv.isSelected = false
            holder.currencyTv.isSelected = false
        }
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(holder.adapterPosition,itemList[holder.adapterPosition])
            selectedItem = itemList[holder.adapterPosition]
            notifyDataSetChanged()
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val timeTv = itemView.item_setting_time_title_tv
        val currencyTv = itemView.item_setting_time_currency_tv
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int,item: Int)
    }
}