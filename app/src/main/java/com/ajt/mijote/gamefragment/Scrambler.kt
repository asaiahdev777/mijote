package com.ajt.mijote.gamefragment

import com.ajt.mijote.Difficulty

object Scrambler {

    private val String.splitToWords get() = split(" ").map { it.trim() }.toMutableList()

    private val String.removeVowels get() = replace("[aeiouyAEIOUY]".toRegex(), "")

    fun encode(difficulty: Difficulty, stringToScramble: String) = stringToScramble.toUpperCase().splitToWords/*.shuffled()*/.joinToString("") { word ->
        when (difficulty) {
            Difficulty.Easy -> encodeEasy(word)
            Difficulty.Medium -> encodeMedium(word)
            Difficulty.Difficult -> encodeDifficult(word)
        }
    }

    private fun encodeEasy(stringToScramble: String) = stringToScramble.removeVowels

    private fun encodeMedium(stringToScramble: String): String {
        val splitWordPart1 = with(stringToScramble) { substring(0, length / 2) }.toList().shuffled().joinToString("")
        val splitWordPart2 = with(stringToScramble) { substring(length / 2, length) }.toList().shuffled().joinToString("")
        return (splitWordPart1 + splitWordPart2).removeVowels
    }

    private fun encodeDifficult(stringToScramble: String): String {
        //Remove the vowels
        var noVowelsString = stringToScramble.removeVowels
        //Shift the letters
        //Shift the characters (A -> Z, B -> Y)
        noVowelsString = noVowelsString.toSet().shuffled().joinToString("")
        return noVowelsString.reversed()
    }

}