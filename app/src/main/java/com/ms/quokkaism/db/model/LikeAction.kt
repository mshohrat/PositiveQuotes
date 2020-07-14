package com.ms.quokkaism.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "like_action")
data class LikeAction(
    @PrimaryKey(autoGenerate = true)
    val id : Long? = null,
    val quoteId: Long,
    @ColumnInfo(name = "is_liked")
    val isLiked: Int = ACTION_LIKE
) {
    companion object {
        const val ACTION_LIKE = 1
        const val ACTION_DISLIKE = 0
    }
}