package com.ajt.mijote.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.view.Menu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.ajt.mijote.MainActivity
import com.ajt.mijote.R
import com.ajt.mijote.db.WordRepository
import com.ajt.mijote.extensions.add
import com.ajt.mijote.extensions.returnHardCopy
import com.ajt.mijote.model.Word
import com.ajt.mijote.wordsfragment.WordDiffUtilCallback
import com.ajt.mijote.wordsfragment.WordsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val observableWords = MutableLiveData<MutableList<Word>>()

    private var cachedWords = mutableListOf<Word>()

    val wordsAvailable get() = observableWords.value?.isNotEmpty() ?: false

    val sharedPreferences: SharedPreferences by lazy { application.getSharedPreferences(CustomApp.settingsFileName, Application.MODE_PRIVATE) }
    val sharedPreferencesEditor: SharedPreferences.Editor by lazy { sharedPreferences.edit() }

    private var inNightMode = defaultNightMode

    companion object {
        const val nightModeKey = "InNightMode"
        const val defaultNightMode = false
    }
    fun triggerFetch() {
        //If we enter a fragment and the cache is empty, fill it
        if (observableWords.value == null) getWords()
        //Else, force an update
        else observableWords.postValue(cachedWords.copyAndSortByOrder)
    }

    fun addWord() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalWords = observableWords.value?.size ?: 0
            val word = Word(id = Date().time, wordOrder = totalWords + 1L).apply { inFocus = true }
            WordRepository.insertWord(word)
            observableWords.value?.let { words ->
                words.add(word)
                cachedWords = words.returnHardCopy
                observableWords.postValue(words.copyAndSortByOrder)
            }
        }
    }

    fun updateWord(word: Word, update: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.updateWord(word)
            observableWords.value?.let { words ->
                words.find { it.id == word.id }?.copyAttributes(word)
                cachedWords = words.returnHardCopy
                if (update) observableWords.postValue(words.copyAndSortByOrder)
            }
        }
    }

    fun updateWords(words: MutableList<Word>) {
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.updateWords(words)
            cachedWords = words.returnHardCopy
            /*observableWords.value?.let { words ->
                observableWords.postValue(words.copyAndSortByOrder)
            }*/
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.deleteWord(word)
            observableWords.value?.let { words ->
                words.removeAll { it.id == word.id }
                cachedWords = words.returnHardCopy
                observableWords.postValue(words.copyAndSortByOrder)
            }
        }
    }

    fun deleteAllWords() {
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.deleteAllWords()
            cachedWords = mutableListOf()
            observableWords.postValue(mutableListOf())
        }
    }

    fun toggleIncludedExcluded(include: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.toggleIncludedExcluded(include)
            observableWords.value?.let { words ->
                words.forEach { it.included = include }
                cachedWords = words.returnHardCopy
                observableWords.postValue(words.copyAndSortByOrder)
            }
        }
    }

    fun toggleWordIncludedExcluded(word: Word) {
        //word.toggleIncluded()
        updateWord(word.copy().apply { toggleIncluded() }, true)
    }

    fun getWords() {
        viewModelScope.launch(Dispatchers.IO) {
            val words = WordRepository.returnWords()
            cachedWords = words.returnHardCopy
            observableWords.postValue(words.copyAndSortByOrder)
        }
    }

    fun calculateDiff(
        wordsAdapter: WordsAdapter,
        oldWords: MutableList<Word>,
        newWords: MutableList<Word>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallback(oldWords, newWords))
            launch(Dispatchers.Main) {
                diffResult.dispatchUpdatesTo(wordsAdapter)
            }
        }
    }

    fun Menu.addThemeMenuItem(activity: Activity) {
        add(R.string.nightMode) {
            inNightMode = !inNightMode
            changeTheme(activity)
        }.apply {
            isCheckable = true
            isChecked = inNightMode
        }
    }

    fun getIfInNightMode() : Boolean {
        inNightMode = sharedPreferences.getBoolean(nightModeKey, defaultNightMode)
        return inNightMode
    }

    private fun changeTheme(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferencesEditor.putBoolean(nightModeKey, inNightMode).apply()
            viewModelScope.launch(Dispatchers.Main) {
                activity.recreate()
            }
        }
    }


    private val MutableList<Word>.copyAndSortByOrder get() = sortedBy { it.wordOrder }.returnHardCopy

    override fun onCleared() {
        super.onCleared()
        WordRepository.closeDown()
    }
}