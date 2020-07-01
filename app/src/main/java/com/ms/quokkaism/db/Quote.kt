package com.ms.quokkaism.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id : Long? = null,
    val text : String,
    val author : String?,
    @ColumnInfo(name = "is_read")
    val isRead : Int = 0,
    @ColumnInfo(name = "is_favorite")
    var isFavorite : Int = 0
) {
    fun isLiked(): Boolean {
        return isFavorite == 1
    }
}