package com.ms.quokkaism.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.R
import com.ms.quokkaism.db.model.Quote
import kotlinx.android.synthetic.main.item_quote_fullscreen.view.*

class QuoteFullscreenAdapter(
    private val onItemClickListener: OnItemClickListener? = null
): PagedListAdapter<Quote?,QuoteFullscreenAdapter.ViewHolder>(QuotesFullscreenDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_quote_fullscreen,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
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

class QuotesFullscreenDiffCallback: DiffUtil.ItemCallback<Quote?>() {

    override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
        return oldItem == newItem
    }

}