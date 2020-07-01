package com.ms.quokkaism.ui.liked_quotes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.R
import com.ms.quokkaism.db.Quote
import kotlinx.android.synthetic.main.item_liked_quote.view.*

class LikedQuotesAdapter(
    private val likedQuotesList: MutableList<Quote?>,
    private val onItemClickListener: OnItemClickListener? = null
): RecyclerView.Adapter<LikedQuotesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_liked_quote,parent,false))
    }

    override fun getItemCount(): Int {
        return likedQuotesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(likedQuotesList.size <= position) {
            return
        }
        val item = likedQuotesList[position]
        item?.let {
            holder.textTv?.text = it.text
            it.author?.let { author ->
                holder.authorTv?.text = author
                holder.authorTv?.visibility = View.VISIBLE
            }
            if(it.isLiked()) {
                holder.likeBtn?.setImageResource(R.drawable.ic_like_fill_24dp)
            } else {
                holder.likeBtn?.setImageResource(R.drawable.ic_like_24dp)
            }
            holder.shareBtn?.setOnClickListener {
                onItemClickListener?.onItemShareClick(
                    holder.adapterPosition,
                    likedQuotesList[holder.adapterPosition]
                )
            }
            holder.copyBtn?.setOnClickListener {
                onItemClickListener?.onItemCopyClick(
                    holder.adapterPosition,
                    likedQuotesList[holder.adapterPosition]
                )
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

    fun updateItems(likedQuotesList: MutableList<Quote?>?) {
        likedQuotesList?.let {
            this.likedQuotesList.addAll(it)
            notifyDataSetChanged()
        }
    }

    fun likeItemAt(position: Int) {
        if(likedQuotesList.size > position) {
            val item = likedQuotesList[position]
            item?.let {
                it.isFavorite = 1
                notifyDataSetChanged()
            }
        }
    }

    fun dislikeItemAt(position: Int) {
        if(likedQuotesList.size > position) {
            val item = likedQuotesList[position]
            item?.let {
                it.isFavorite = 0
                notifyDataSetChanged()
            }
        }
    }

    fun revertLikeItem(position: Int) {
        if(likedQuotesList.size > position) {
            val item = likedQuotesList[position]
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
        val textTv = itemView.item_liked_quote_text_tv
        val authorTv = itemView.item_liked_quote_author_tv
        val shareBtn = itemView.liked_quote_share_btn
        val copyBtn = itemView.liked_quote_copy_btn
        val likeBtn = itemView.liked_quote_like_btn
    }

    interface OnItemClickListener {
        fun onItemShareClick(position: Int, likedQuote: Quote?)
        fun onItemCopyClick(position: Int, likedQuote: Quote?)
        fun onItemLikeClick(position: Int, likedQuote: Quote?)
    }
}