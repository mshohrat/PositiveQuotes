package com.ms.quokkaism.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.R
import com.ms.quokkaism.db.Quote
import kotlinx.android.synthetic.main.item_quote_fullscreen.view.*

class QuoteFullscreenAdapter(
    private val itemList: MutableList<Quote?> = mutableListOf(),
    private val onItemClickListener: OnItemClickListener? = null
): RecyclerView.Adapter<QuoteFullscreenAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_quote_fullscreen,parent,false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(itemList.size <= position)
        {
            return
        }
        val item = itemList[position]
        item?.let {
            holder.textTv?.text = it.text
            it.author?.takeIf { it.isNotEmpty() }?.let { author ->
                holder.authorTv?.text = author
                holder.authorTv?.visibility = View.VISIBLE
            }
            if(it.isLiked()) {
                holder.likeBtn?.setImageResource(R.drawable.ic_like_fill)
            } else {
                holder.likeBtn?.setImageResource(R.drawable.ic_like)
            }
            holder.shareBtn?.setOnClickListener { v ->
                onItemClickListener?.onItemShareClick(holder.adapterPosition,it)
            }
            holder.copyBtn?.setOnClickListener {v ->
                onItemClickListener?.onItemCopyClick(holder.adapterPosition,it)
            }
            holder.likeBtn?.setOnClickListener {v ->
                onItemClickListener?.onItemLikeClick(holder.adapterPosition,it)
                if(it.isLiked()) {
                    it.isFavorite = 0
                } else {
                    it.isFavorite = 1
                }
                notifyDataSetChanged()
            }
        }
    }

    fun likeItemAt(position: Int) {
        if(itemList.size > position) {
            val item = itemList[position]
            item?.let {
                it.isFavorite = 1
                notifyDataSetChanged()
            }
        }
    }

    fun dislikeItemAt(position: Int) {
        if(itemList.size > position) {
            val item = itemList[position]
            item?.let {
                it.isFavorite = 0
                notifyDataSetChanged()
            }
        }
    }

    fun revertLikeItem(position: Int) {
        if(itemList.size > position) {
            val item = itemList[position]
            item?.let {
                if(it.isLiked()) {
                    it.isFavorite = 0
                } else {
                    it.isFavorite = 1
                }
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textTv = itemView.item_quote_fullscreen_text
        val authorTv = itemView.item_quote_fullscreen_author
        val shareBtn = itemView.item_quote_fullscreen_share_btn
        val copyBtn = itemView.item_quote_fullscreen_copy_btn
        val likeBtn = itemView.item_quote_fullscreen_like_btn
    }

    interface OnItemClickListener {
        fun onItemShareClick(position: Int, quote: Quote)
        fun onItemCopyClick(position: Int, quote: Quote)
        fun onItemLikeClick(position: Int, quote: Quote)
    }
}