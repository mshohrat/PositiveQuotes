package com.ms.quokkaism.ui.home

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.db.Quote
import com.ms.quokkaism.network.base.ApiServiceGenerator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HomeViewModel : ViewModel() {

    private val _quotes = MutableLiveData<MutableList<Quote?>>()
    val quotes : LiveData<MutableList<Quote?>> = _quotes

    private val _like = MutableLiveData<Pair<Int,Boolean>>()
    val like : LiveData<Pair<Int,Boolean>> = _like

    private val _likeError = MutableLiveData<Int>()
    val likeError : LiveData<Int> = _likeError

    @SuppressLint("CheckResult")
    fun getQuotes() {
        val lastReadQuotes = AppDatabase.getAppDataBase()?.quoteDao()?.getLastReadQuotes()
        lastReadQuotes?.value?.takeIf { it.isNotEmpty() }?.let {
            _quotes.value = it.toMutableList()
        } ?: kotlin.run {
            _quotes.value = mutableListOf()
            ApiServiceGenerator.getApiService.getQuotes()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    it?.quoteList?.takeIf { it.isNotEmpty() }?.let {
                        val list = mutableListOf<Quote>()
                        it.forEach {
                            it.text?.let { text ->
                                AppDatabase.getAppDataBase()?.quoteDao()?.insertQuote(
                                    Quote(it.id,text,it.author)
                                )

                            }
                        }
                    }
                },{

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