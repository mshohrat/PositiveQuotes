package com.ms.quokkaism.ui.liked_quotes

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.ms.quokkaism.R
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.db.model.LikeAction
import com.ms.quokkaism.db.model.Quote
import com.ms.quokkaism.extension.isDeviceOnline
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.GeneralResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class LikedQuotesViewModel : ViewModel() {
    
    private var currentServerPage = 0
    private var totalServerPages = 1

    lateinit var _likedQuotes : LiveData<PagedList<Quote?>>

    private val _likedQuotesError = MutableLiveData<GeneralResponse?>()
    val likedQuotesError : LiveData<GeneralResponse?> = _likedQuotesError

    init {
        val factory = AppDatabase.getAppDataBase()?.quoteDao()?.getLikedQuotes()
        factory?.let {
            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(20)
                .setPageSize(20)
                .setPrefetchDistance(5)
                .setEnablePlaceholders(false)
                .build()
            val pagedListBuilder: LivePagedListBuilder<Int, Quote?> = LivePagedListBuilder<Int, Quote?>(it, config)
            pagedListBuilder.setBoundaryCallback(LikedQuotesBoundaryCallback())
            _likedQuotes = pagedListBuilder.build()
        } ?: kotlin.run {
            _likedQuotes = MutableLiveData()
        }
    }

    @SuppressLint("CheckResult")
    internal fun fetchLikedQuotes()
    {
        if(isDeviceOnline()) {
            if (currentServerPage < totalServerPages) {
                currentServerPage++
                ApiServiceGenerator.getApiService.getLikedQuotes(currentServerPage)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({
                        it?.let {
                            currentServerPage = it.currentPage
                            totalServerPages = it.totalPages
                            it.likedQuotes?.takeIf { it.isNotEmpty() }?.let {
                                val list = mutableListOf<Quote>()
                                it.forEach {
                                    it?.text?.let { text ->
                                        list.add(
                                            Quote(
                                                it.id,
                                                text,
                                                it.author,
                                                isRead = 1,
                                                isFavorite = 1
                                            )
                                        )
                                    }
                                }
                                viewModelScope.launch {
                                    AppDatabase.getAppDataBase()?.quoteDao()?.upsert(list)
                                }
                            }
                        }

                    }, {
                        _likedQuotesError.value = GeneralResponse(messageResId = R.string.failed_to_connect_server)
                    })
            }
        }
    }

    @SuppressLint("CheckResult")
    fun like(position:Int, quote: Quote) {
        likeQuote(quote)
        quote.id?.let { id ->
            if(isDeviceOnline()) {
                ApiServiceGenerator.getApiService.like(id)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({

                    }, {
                        dislikeQuote(quote)
                    })
            }
            else
            {
                addLikeAction(id)
            }
        }
    }

    @SuppressLint("CheckResult")
    fun dislike(position:Int, quote: Quote) {
        dislikeQuote(quote)
        quote.id?.let { id ->
            if(isDeviceOnline()) {
                ApiServiceGenerator.getApiService.dislike(id)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({

                    }, {
                        likeQuote(quote)
                    })
            }
            else
            {
                addDislikeAction(id)
            }
        }
    }

    private fun addLikeAction(quoteId: Long) {
        viewModelScope.launch {
            AppDatabase.getAppDataBase()?.likeActionDao()?.insert(
                LikeAction(
                    quoteId = quoteId,
                    isLiked = LikeAction.ACTION_LIKE
                )
            )
        }
    }

    private fun addDislikeAction(quoteId: Long) {
        viewModelScope.launch {
            AppDatabase.getAppDataBase()?.likeActionDao()?.insert(
                LikeAction(
                    quoteId = quoteId,
                    isLiked = LikeAction.ACTION_DISLIKE
                )
            )
        }
    }

    private fun likeQuote(quote: Quote) {
        quote.id?.let {
            viewModelScope.launch {
                AppDatabase.getAppDataBase()?.quoteDao()?.updateQuoteIsfavorite(it,1)
            }
        }
    }

    private fun dislikeQuote(quote: Quote) {
        quote.id?.let {
            viewModelScope.launch {
                AppDatabase.getAppDataBase()?.quoteDao()?.updateQuoteIsfavorite(it,0)
            }
        }
    }

    inner class LikedQuotesBoundaryCallback : PagedList.BoundaryCallback<Quote?>() {
        override fun onItemAtEndLoaded(itemAtEnd: Quote) {
            super.onItemAtEndLoaded(itemAtEnd)
            fetchLikedQuotes()
        }

        override fun onZeroItemsLoaded() {
            super.onZeroItemsLoaded()
            fetchLikedQuotes()
        }
    }

}
