package com.ajt.mijote.model

interface Copyable<T> {
    fun duplicate(): T
}