package com.ajt.mijote.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Words")
data class Word(
    @PrimaryKey(autoGenerate = true) var id: Long = 1L,
    var wordOrder : Long = 1L,
    var text: String = "",
    var included: Boolean = true,
    @Ignore var inFocus: Boolean = false
) : Copyable<Word> {
    @Ignore var flipUpsideDown = false
    @Ignore var scrambledText = ""

    fun copyAttributes(wordToClone: Word) {
        id = wordToClone.id
        wordOrder = wordToClone.wordOrder
        text = wordToClone.text
        included = wordToClone.included
    }

    fun toggleIncluded() {
        included = !included
    }

    override fun duplicate() = copy()
}