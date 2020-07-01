package com.ms.quokkaism.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ms.quokkaism.App

@Database(entities = [Quote::class],version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quoteDao() : QuoteDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(): AppDatabase? {
            if (INSTANCE == null){
                synchronized(AppDatabase::class){
                    INSTANCE = Room.databaseBuilder(App.appContext, AppDatabase::class.java, "myDB").build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }
    }
}