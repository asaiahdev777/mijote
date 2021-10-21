package com.ajt.mijote.app

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.ajt.mijote.db.AppDatabase
import com.ajt.mijote.db.WordRepository

class CustomApp : Application() {

    companion object {
        lateinit var appViewModel : AppViewModel
        const val settingsFileName = "Settings.xml"
    }

    override fun onCreate() {
        super.onCreate()
        appViewModel = ViewModelProvider.AndroidViewModelFactory(this).create(AppViewModel::class.java)
        WordRepository.init(AppDatabase.get(this))
    }
}