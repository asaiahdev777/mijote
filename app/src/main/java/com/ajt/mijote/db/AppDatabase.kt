package com.ajt.mijote.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ajt.mijote.model.Word
import java.io.File

@Database(entities = [Word::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getWordDao(): WordDao

    companion object {
        private const val databaseName = "Words.db"
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            if (instance == null) synchronized(AppDatabase::class) {
                val path = File(context.getExternalFilesDirs(null)!![0], databaseName)
                instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, path.absolutePath).fallbackToDestructiveMigration().setJournalMode(JournalMode.TRUNCATE).build()
            }
            return instance!!
        }

        fun evict() {
            instance?.close()
            instance = null
        }
    }

}