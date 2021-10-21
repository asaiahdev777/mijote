package com.ajt.mijote.gamefragment

import android.os.CountDownTimer
import android.os.SystemClock
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajt.mijote.Difficulty
import com.ajt.mijote.app.Logger
import com.ajt.mijote.extensions.appViewModel
import com.ajt.mijote.extensions.returnHardCopy
import com.ajt.mijote.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GameViewModel : ViewModel() {

    var difficulty = defaultDifficulty
        private set

    private var questions = mutableListOf<Word>()
    val totalCards get() = questions.size

    val observableChoices = MutableLiveData<List<Word>>()
    val observableQuestion = MutableLiveData<Word>()
    val observableCorrect = MutableLiveData<Int>()
    val observableWrong = MutableLiveData<Int>()
    val observablePoints = MutableLiveData<Long>()
    val observableIndex = MutableLiveData<Pair<Int, Int>>()
    val observableTimeLeft = MutableLiveData<Long>()
    val observableOnTimeOut = MutableLiveData<Unit>()
    val observableOnDone = MutableLiveData<Unit>()

    private lateinit var currentQuestion: Word

    var animate = false
        private set
    private var choices = mutableListOf<Word>()
    private var index = 0
    private var correct = 0
    private var wrong = 0
    private var optionCount = 4
    private var timeLeft = 0L
    private var timeWhenQuestionShown = 0L

    private var accumulatedPoints = 0L
    var actualPoints = 0L
        private set

    private var countingUp = false
    private var markedWrong = false
    val inGameMode get() = index <= questions.lastIndex

    private lateinit var countDownTimer: CountDownTimer

    companion object {
        const val pointsAddedPerCorrectSecond = 100
        const val pointsAddedPerCorrectSecondFast = 300
        const val pointsSubtractedPerWrong = 50
        const val pointsSubtractedPerTimeOut = 30
        const val difficultyKey = "Difficulty"
        val defaultDifficulty = Difficulty.Medium
    }

    fun initialize(words: MutableList<Word>) {
        val default = defaultDifficulty.name
        difficulty = Difficulty.valueOf(appViewModel.sharedPreferences.getString(difficultyKey, default) ?: default)
        questions = words.returnHardCopy
        if (questions.isNotEmpty()) loadQuestion()
    }

    fun restore() {
        if (inGameMode) {
            updateQuestion()
            updateChoices()
            updatePoints()
            updateIndex()
            updateWrong()
            updateCorrect()
        }
    }

    private fun loadQuestion() {
        if (inGameMode) {
            markedWrong = false
            if (::countDownTimer.isInitialized) countDownTimer.cancel()
            createCountDownTimer()

            viewModelScope.launch(Dispatchers.IO) {
                currentQuestion = questions[index]
                currentQuestion.scrambledText = Scrambler.encode(difficulty, currentQuestion.text)
                loadChoices()
                updateQuestion()
            }
            updateIndex()

            if (wrong == 0 && correct == 0) {
                preventAnimate()
                updateWrong()
                updateCorrect()
                updatePoints()
            }
        }
    }

    private fun createCountDownTimer() {
        countDownTimer = object : CountDownTimer(if (countingUp) Long.MAX_VALUE else TimeUnit.SECONDS.toMillis(difficulty.seconds + 1), TimeUnit.SECONDS.toMillis(1)) {

            override fun onTick(millisUntilFinished: Long) = dispatchUpdateTime(millisUntilFinished)

            override fun onFinish() {
                wrong++
                allowAnimate()
                updateWrong()
                applyTimeOutPenalty()
            }
        }
    }

    private fun loadChoices() {
        viewModelScope.launch(Dispatchers.IO) {
            choices = mutableListOf()
            choices.add(currentQuestion)
            choices.addAll(questions.filter { !it.text.equals(currentQuestion.text, true) }.take(optionCount - 1))
            choices.shuffle()
            updateChoices()
        }
    }

    fun startTimer() {
        countDownTimer.start()
    }

    fun dispatchUpdateTime(millisUntilFinished: Long) {
        timeLeft = TimeUnit.MILLISECONDS.toSeconds(if (countingUp) millisUntilFinished - Long.MAX_VALUE else millisUntilFinished)
        observableTimeLeft.postValue(timeLeft)
    }

    fun onClickChoice(choice: Word) {
        if (choice.text.equals(currentQuestion.text, true)) onClickCorrectChoice()
        else onClickWrongChoice()
    }

    private fun onClickWrongChoice() {
        if (!markedWrong) wrong++
        accumulatedPoints -= pointsSubtractedPerWrong
        calculateShowAveragePoints()
        allowAnimate()
        updateWrong()
        markedWrong = true
    }

    private fun onClickCorrectChoice() {
        if (!markedWrong) {
            correct++
            val timeSpentToAnswer = (SystemClock.uptimeMillis() - timeWhenQuestionShown) / 1000F
            val answeredBeforeTimeUp = timeSpentToAnswer <= difficulty.secondsUntilLate
            accumulatedPoints += ((if (answeredBeforeTimeUp) pointsAddedPerCorrectSecondFast else pointsAddedPerCorrectSecond) * (difficulty.seconds.toFloat() / timeSpentToAnswer.coerceAtLeast(1F))).toLong()
            calculateShowAveragePoints()
        }

        allowAnimate()
        updateCorrect()
        moveToNext()
    }

    private fun calculateShowAveragePoints() {
        actualPoints = accumulatedPoints / questions.size
        updatePoints()
    }

    private fun moveToNext() {
        countDownTimer.cancel()
        countingUp = false
        index++
        if (index <= questions.lastIndex) loadQuestion()
        else {
            updateIndex()
            observableOnDone.postValue(Unit)
        }
    }

    private fun applyTimeOutPenalty() {
        accumulatedPoints -= pointsSubtractedPerTimeOut
        choices.reverse()
        observableOnTimeOut.postValue(Unit)
        countingUp = true
        calculateShowAveragePoints()
        createCountDownTimer()
        countDownTimer.start()
    }

    private fun allowAnimate() {
        animate = true
    }

    private fun preventAnimate() {
        animate = false
    }

    private fun updateQuestion() {
        Logger.log(this, "Question updated!")
        observableQuestion.postValue(currentQuestion)
        timeWhenQuestionShown = SystemClock.uptimeMillis()
    }

    private fun updateIndex() = observableIndex.postValue(index to questions.size)

    private fun updateWrong() {
        Logger.log(this, "Wrong updated!")
        observableWrong.postValue(wrong)
    }

    private fun updateCorrect() = observableCorrect.postValue(correct)

    private fun updatePoints() = observablePoints.postValue(actualPoints)

    private fun updateChoices() = observableChoices.postValue(choices)

    fun cancelTimer() = if (::countDownTimer.isInitialized) countDownTimer.cancel() else Unit

    fun restart() {
        index = 0
        correct = 0
        wrong = 0
        actualPoints = 0
        accumulatedPoints = 0
        loadQuestion()
    }

    fun changeDifficulty(newDifficulty: Difficulty) {
        viewModelScope.launch(Dispatchers.IO) {
            difficulty = newDifficulty
            with(appViewModel.sharedPreferencesEditor) {
                putString(difficultyKey, difficulty.name)
                commit()
            }
            launch(Dispatchers.Main) { loadQuestion() }
        }
    }

    override fun onCleared() {
        countDownTimer.cancel()
        super.onCleared()
    }

}