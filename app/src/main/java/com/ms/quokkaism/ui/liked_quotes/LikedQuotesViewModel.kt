package com.ms.quokkaism.ui.liked_quotes

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.R
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.db.Quote
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.GeneralResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LikedQuotesViewModel : ViewModel() {
    
    private var currentServerPage = 0
    private var totalServerPages = 1

    private var localOffset = 0
    private var localPageSize = 10

    private val _likedQuotes = MutableLiveData<MutableList<Quote?>>()
    val likedQuotes : LiveData<MutableList<Quote?>> = _likedQuotes

    private val _likedQuotesError = MutableLiveData<GeneralResponse?>()
    val likedQuotesError : LiveData<GeneralResponse?> = _likedQuotesError

    private val _like = MutableLiveData<Pair<Int,Boolean>>()
    val like : LiveData<Pair<Int,Boolean>> = _like

    private val _likeError = MutableLiveData<Int>()
    val likeError : LiveData<Int> = _likeError

    fun getLikedQuotes()
    {
        AppDatabase.getAppDataBase()?.quoteDao()?.getLikedQuotes(localOffset,localPageSize)?.value?.takeIf {
            it.isNotEmpty()
        }?.let {
            it.forEach {
                _likedQuotes.value?.add(it)
            }
            localOffset.plus(it.size)
        } ?: kotlin.run {
            fetchLikedQuotes()
        }
    }

    @SuppressLint("CheckResult")
    fun fetchLikedQuotes()
    {
        if(currentServerPage < totalServerPages)
        {
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
                            it.forEach{
                                it?.text?.let { text ->
                                    list.add(Quote(it.id,text,it.author,isFavorite = 1))
                                }
                            }
                            _likedQuotes.value?.addAll(list)
                            AppDatabase.getAppDataBase()?.quoteDao()?.insertQuotes(list)
                        }
                    } ?: kotlin.run {
                        _likedQuotesError.value = GeneralResponse(messageResId = R.string.failed_to_connect_server)
                    }

                },{
                    _likedQuotesError.value = GeneralResponse(it.message)
                })
        }
    }

    @SuppressLint("CheckResult")
    fun like(position:Int, quote: Quote) {
        quote.id?.let { id ->
            ApiServiceGenerator.getApiService.like(id)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    it?.let {
                        AppDatabase.getAppDataBase()?.quoteDao()?.updateQuote(quote.apply { isFavorite = 1 })
                        _like.value = position to true
                    } ?: kotlin.run {
                        _likeError.value = position
                    }
                }, {
                    _likeError.value = position
                })
        }
    }

    @SuppressLint("CheckResult")
    fun dislike(position:Int, quote: Quote) {
        quote.id?.let { id ->
            ApiServiceGenerator.getApiService.dislike(id)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    it?.let {
                        AppDatabase.getAppDataBase()?.quoteDao()?.updateQuote(quote.apply { isFavorite = 0 })
                        _like.value = position to false
                    } ?: kotlin.run {
                        _likeError.value = position
                    }
                }, {
                    _likeError.value = position
                })
        }
    }

}
