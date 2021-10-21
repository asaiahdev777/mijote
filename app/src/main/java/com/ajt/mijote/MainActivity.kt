package com.ajt.mijote

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.ajt.mijote.extensions.appViewModel
import com.ajt.mijote.gamefragment.GameFragment
import com.ajt.mijote.model.Events
import com.ajt.mijote.wordsfragment.WordsFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setTheme(if (appViewModel.getIfInNightMode() || nightMode == Configuration.UI_MODE_NIGHT_YES) R.style.ActivityTheme_Primary_Base_Dark else R.style.ActivityTheme_Primary_Base_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolBar)
        EventBus.getDefault().register(this)

        loadingView.isVisible = savedInstanceState == null
        appViewModel.observableWords.observe(this, Observer {
            loadingView.isVisible = false
            if (savedInstanceState == null) addFragment(Events.AddFragment(if (it.isEmpty()) WordsFragment() else GameFragment()))
            appViewModel.observableWords.removeObservers(this)
        })
        appViewModel.triggerFetch()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun addFragment(addFragmentEvent: Events.AddFragment) {
        with(supportFragmentManager) {
            with(beginTransaction()) {
                if (findFragmentById(R.id.fragmentLayout) == null)
                    add(R.id.fragmentLayout, addFragmentEvent.fragment, null)
                else replace(R.id.fragmentLayout, addFragmentEvent.fragment, null)
            }.commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) with(appViewModel) { menu.addThemeMenuItem(this@MainActivity) }
        return super.onCreateOptionsMenu(menu)
    }

}