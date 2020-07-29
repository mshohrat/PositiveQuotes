package com.ms.quokkaism.ui.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.db.model.LikeAction
import com.ms.quokkaism.db.model.Quote
import com.ms.quokkaism.extension.isDeviceOnline
import com.ms.quokkaism.model.SyncLikeAction
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.SyncLikeActionsRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    val syncIsRunning = MutableLiveData<Boolean>()

    val quotesFetched = MutableLiveData<Boolean>(false)

    lateinit var lastReadQuotes: LiveData<PagedList<Quote?>>

    init {
        val factory = AppDatabase.getAppDataBase()?.quoteDao()?.getLastReadQuotes()
        factory?.let {
            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(20)
                .setPageSize(10)
                .setPrefetchDistance(5)
                .setEnablePlaceholders(false)
                .build()
            val pagedListBuilder: LivePagedListBuilder<Int, Quote?> = LivePagedListBuilder<Int, Quote?>(it, config)
            lastReadQuotes = pagedListBuilder.build()
        } ?: kotlin.run {
            lastReadQuotes = MutableLiveData()
        }
        handleFirstUnreadQuotesCount()
        handleSyncActionsWithServer()
    }

    private fun handleFirstUnreadQuotesCount() {
        if(isDeviceOnline()) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val firstUnreadQuotes =
                        AppDatabase.getAppDataBase()?.quoteDao()?.getFirstUnreadQuotes()
                    if (firstUnreadQuotes == null || firstUnreadQuotes.size < 10) {
                        fetchUnreadQuotesFromServer()
                    }
                }
            }
        }
    }

    private fun handleSyncActionsWithServer() {
        if(isDeviceOnline()) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    val likeActions = AppDatabase.getAppDataBase()?.likeActionDao()?.getAll()
                    likeActions?.takeIf { it.isNotEmpty() }?.let {
                        syncActionsWithServer(it)
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun syncActionsWithServer(likeActions: List<LikeAction>) {
        if(isDeviceOnline()) {
            syncIsRunning.value = true
            val syncLikeActions = mutableListOf<SyncLikeAction>()
            likeActions.forEach {
                syncLikeActions.add(
                    SyncLikeAction(
                        it.quoteId,
                        it.isLiked == LikeAction.ACTION_LIKE
                    )
                )
            }
            ApiServiceGenerator.getApiService.syncLikeActions(SyncLikeActionsRequest(syncLikeActions))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    it?.let {
                        viewModelScope.launch {
                            AppDatabase.getAppDataBase()?.likeActionDao()?.deleteAll()
                        }
                    }
                    syncIsRunning.value = false
                }, {
                    syncIsRunning.value = false
                })
        }
    }

    @SuppressLint("CheckResult")
    private fun fetchUnreadQuotesFromServer() {
        if(isDeviceOnline()) {
            ApiServiceGenerator.getApiService.getQuotes()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    it?.quoteList?.takeIf { it.isNotEmpty() }?.let {
                        val list = mutableListOf<Quote>()
                        it.forEach {
                            it.text?.let { text ->
                                list.add(
                                    Quote(
                                        it.id,
                                        text,
                                        it.author
                                    )
                                )
                            }
                        }
                        list.takeIf { it.isNotEmpty() }.let {
                            quotesFetched.value = true
                            viewModelScope.launch {
                                AppDatabase.getAppDataBase()?.quoteDao()?.insertQuotes(list)
                            }
                        }
                    }
                }, {

                })
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
                        if (it == null) {
                            dislikeQuote(quote)
                        }
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
                        if (it == null) {
                            likeQuote(quote)
                        }
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

    override fun onCleared() {
        super.onCleared()
    }
}