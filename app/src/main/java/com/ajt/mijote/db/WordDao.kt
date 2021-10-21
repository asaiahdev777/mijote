package com.ajt.mijote.db

import androidx.room.*
import com.ajt.mijote.model.Word

@Dao
interface WordDao {

    @Query("SELECT * FROM Words ORDER BY Words.wordOrder")
    fun getAllWords(): MutableList<Word>

    @Insert(entity = Word::class)
    fun insertWord(word: Word)

    @Insert(entity = Word::class)
    fun insertWords(words: MutableList<Word>)

    @Update(entity = Word::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateWord(word: Word)

    @Update(entity = Word::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateWords(words: MutableList<Word>)

    @Delete(entity = Word::class)
    fun deleteWord(word: Word)

    @Query("DELETE FROM Words")
    fun deleteAllWords()

    @Query("UPDATE Words SET included = :include")
    fun toggleIncludedExcluded(include: Boolean)
}