package com.ms.quokkaism.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.ms.quokkaism.db.model.Quote

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotesList: List<Quote>)

    @Query("UPDATE quotes SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateQuoteIsfavorite(id: Long, isFavorite: Int)

    @Query("UPDATE quotes SET is_read = :isRead WHERE id = :id")
    suspend fun updateQuoteIsRead(id: Long, isRead: Int)

    @Query("SELECT * FROM quotes WHERE is_favorite == 1 ORDER BY id ASC")
    fun getLikedQuotes(): DataSource.Factory<Int, Quote?>

    @Query("SELECT * FROM quotes WHERE is_read == 1 ORDER BY id DESC LIMIT 10")
    fun getLastReadQuotes(): LiveData<List<Quote?>?>?

    @Query("SELECT * FROM quotes WHERE is_read == 0 ORDER BY id ASC LIMIT 1")
    suspend fun getFirstUnreadQuote(): Quote?

    @Query("SELECT * FROM quotes WHERE is_read == 0 ORDER BY id ASC LIMIT 10")
    suspend fun getFirstUnreadQuotes(): List<Quote?>?
}