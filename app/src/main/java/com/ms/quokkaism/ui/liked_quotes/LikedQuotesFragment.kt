package com.ms.quokkaism.ui.liked_quotes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.R
import com.ms.quokkaism.db.Quote
import com.ms.quokkaism.extension.toggleSideMenu
import com.ms.quokkaism.ui.liked_quotes.adapter.LikedQuotesAdapter
import com.ms.quokkaism.util.LoadingDialog
import kotlinx.android.synthetic.main.fragment_liked_quotes.*

class LikedQuotesFragment : Fragment(), LikedQuotesAdapter.OnItemClickListener {

    private lateinit var viewModel: LikedQuotesViewModel
    private var loadingDialog: LoadingDialog? = null
    private var likedQuotesAdapter: LikedQuotesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_liked_quotes, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LikedQuotesViewModel::class.java)
        subscribeToViewModel()
        subscribeToViewEvents()
        //loadingDialog = showLoadingDialog()
        viewModel.getLikedQuotes()
    }

    private fun subscribeToViewModel() {
        viewModel.likedQuotes.observe(viewLifecycleOwner, Observer { list ->
            list.takeIf { it.isNotEmpty() }.let {
                likedQuotesAdapter?.updateItems(list) ?: kotlin.run {
                    likedQuotesAdapter = LikedQuotesAdapter(list,this)
                }
            }
            //dismissDialog(loadingDialog)
        })
        viewModel.likedQuotesError.observe(viewLifecycleOwner, Observer {
            //dismissDialog(loadingDialog)
            it?.message?.let {
                Toast.makeText(activity,it, Toast.LENGTH_LONG).show()
            } ?: kotlin.run {
                Toast.makeText(activity,it?.messageResId ?: 0, Toast.LENGTH_LONG).show()
            }

        })

        viewModel.like.observe(viewLifecycleOwner, Observer {
            if(it.second) {
                likedQuotesAdapter?.likeItemAt(it.first)
            } else {
                likedQuotesAdapter?.dislikeItemAt(it.first)
            }
        })

        viewModel.likeError.observe(viewLifecycleOwner, Observer {
            likedQuotesAdapter?.revertLikeItem(it)
        })
    }

    private fun subscribeToViewEvents() {
        liked_quotes_rv?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                    liked_quotes_rv?.layoutManager?.takeIf { it is LinearLayoutManager }?.let {
                        if((it as LinearLayoutManager).findLastVisibleItemPosition() ==
                            liked_quotes_rv?.adapter?.itemCount?.minus(1)) {
                            //loadingDialog = showLoadingDialog()
                            viewModel.getLikedQuotes()
                        }
                    }
                }
            }
        })

        liked_quotes_menu_btn?.setOnClickListener {
            toggleSideMenu()
        }
    }

    override fun onItemCopyClick(position: Int, likedQuote: Quote?) {
        val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipData = ClipData.newPlainText("quote",likedQuote?.text)
        clipboardManager?.setPrimaryClip(clipData)
        Toast.makeText(activity,R.string.the_text_copied_successfully,Toast.LENGTH_SHORT).show()
    }

    override fun onItemShareClick(position: Int, likedQuote: Quote?) {
        val intent = Intent(Intent.ACTION_SEND)
        val shareBody = likedQuote?.text
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
    }

    override fun onItemLikeClick(position: Int, likedQuote: Quote?) {
        likedQuote?.id?.let {
            if(likedQuote.isLiked()) {
                viewModel.dislike(position,likedQuote)
            } else {
                viewModel.like(position,likedQuote)
            }
        }
    }

    override fun onDestroyView() {
        //dismissDialog(loadingDialog)
        super.onDestroyView()
    }

}
