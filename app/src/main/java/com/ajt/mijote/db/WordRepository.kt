package com.ajt.mijote.db

import com.ajt.mijote.model.Word

object WordRepository {

    private lateinit var appDatabase: AppDatabase

    fun init(database: AppDatabase) {
        appDatabase = database
    }

    fun returnWords() = appDatabase.getWordDao().getAllWords()

    fun insertWord(word: Word) = appDatabase.getWordDao().insertWord(word)

    //fun insertWords(words: MutableList<Word>) = appDatabase.getWordDao().insertWords(words)

    fun updateWord(word: Word) = appDatabase.getWordDao().updateWord(word)

    fun updateWords(words: MutableList<Word>) = appDatabase.getWordDao().updateWords(words)

    fun deleteWord(word: Word) = appDatabase.getWordDao().deleteWord(word)

    fun deleteAllWords() = appDatabase.getWordDao().deleteAllWords()

    fun toggleIncludedExcluded(include : Boolean) = appDatabase.getWordDao().toggleIncludedExcluded(include)

    fun closeDown() = AppDatabase.evict()

}