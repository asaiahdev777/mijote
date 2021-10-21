package com.ajt.mijote.app

import android.util.Log

object Logger {

    fun log(objectToTag : Any, message: String) = Log.d(objectToTag::class.java.simpleName, message)
}