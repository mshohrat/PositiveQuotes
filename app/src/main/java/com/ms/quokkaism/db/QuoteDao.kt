package com.ms.quokkaism.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertQuote(quote: Quote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertQuotes(quotesList: List<Quote>)

    @Update
    fun updateQuote(quote: Quote)

    @Query("SELECT * FROM quotes WHERE is_favorite == 1 ORDER BY id ASC LIMIT :pageSize OFFSET :offset")
    fun getLikedQuotes(offset: Int,pageSize: Int): LiveData<List<Quote?>?>?

    @Query("SELECT * FROM quotes WHERE is_read == 1 ORDER BY id DESC LIMIT 1")
    fun getLastReadQuote(): LiveData<Quote?>?

    @Query("SELECT * FROM quotes WHERE is_read == 1 ORDER BY id DESC LIMIT 10")
    fun getLastReadQuotes(): LiveData<List<Quote?>?>?

    @Query("SELECT * FROM quotes WHERE is_read == 0 ORDER BY id DESC LIMIT 1")
    fun getLastUnreadQuote(): LiveData<Quote?>?
}