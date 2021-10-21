package com.ajt.mijote


enum class Difficulty(val nameRes: Int, val seconds: Long, val secondsUntilLate: Int) {
    Easy(R.string.easy, 3, 2),
    Medium(R.string.medium, 5, 3),
    Difficult(R.string.difficult, 7, 5)
}