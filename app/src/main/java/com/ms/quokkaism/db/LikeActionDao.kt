package com.ms.quokkaism.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ms.quokkaism.db.model.LikeAction

@Dao
interface LikeActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(likeAction: LikeAction)

    @Query("SELECT * FROM like_action")
    suspend fun getAll(): List<LikeAction>?

    @Query("DELETE FROM like_action")
    suspend fun deleteAll()
}