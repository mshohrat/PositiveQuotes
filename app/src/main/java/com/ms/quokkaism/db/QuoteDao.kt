package com.ms.quokkaism.db

import androidx.paging.DataSource
import androidx.room.*
import com.ms.quokkaism.db.model.Quote


@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuote(quote: Quote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuotes(quotesList: List<Quote>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateQuotes(quotesList: List<Quote>)

    @Transaction
    suspend fun upsert(quotes: List<Quote>) {
        insertQuotes(quotes)
        quotes.forEach {
            it.id?.let { id ->
                updateQuote(id,it.text,it.author,it.isFavorite,it.isRead)
            }
        }
    }

    @Query("UPDATE quotes SET text = :text,author = :author,is_favorite = :isFavorite,is_read = :isRead WHERE id = :id")
    suspend fun updateQuote(id: Long, text: String,author: String?, isFavorite: Int,isRead: Int)

    @Query("UPDATE quotes SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateQuoteIsfavorite(id: Long, isFavorite: Int)

    @Query("UPDATE quotes SET is_read = :isRead,modified_at=:time WHERE id = :id")
    suspend fun updateQuoteIsRead(id: Long, isRead: Int,time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM quotes WHERE is_favorite == 1 ORDER BY modified_at ASC")
    fun getLikedQuotes(): DataSource.Factory<Int, Quote?>

    @Query("SELECT * FROM quotes WHERE is_read == 1 ORDER BY modified_at DESC")
    fun getLastReadQuotes(): DataSource.Factory<Int,Quote?>

    @Query("SELECT * FROM quotes WHERE is_read == 0 ORDER BY modified_at ASC LIMIT 1")
    suspend fun getFirstUnreadQuote(): Quote?

    @Query("SELECT * FROM quotes WHERE is_read == 0 ORDER BY modified_at ASC LIMIT 10")
    suspend fun getFirstUnreadQuotes(): List<Quote?>?
}