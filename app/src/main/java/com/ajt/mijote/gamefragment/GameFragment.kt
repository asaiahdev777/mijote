package com.ajt.mijote.gamefragment

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ajt.mijote.*
import com.ajt.mijote.app.Logger
import com.ajt.mijote.extensions.add
import com.ajt.mijote.extensions.addSub
import com.ajt.mijote.extensions.appViewModel
import com.ajt.mijote.model.Events
import com.ajt.mijote.model.Word
import com.ajt.mijote.wordsfragment.WordsFragment
import kotlinx.android.synthetic.main.layout_game_fragment.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

class GameFragment : Fragment() {

    private val gameViewModel by lazy { ViewModelProvider(this).get(GameViewModel::class.java) }

    private val adapter by lazy { ChoicesAdapter(layoutInflater, gameViewModel) }

    private lateinit var statsBar: ViewGroup
    private lateinit var pointsViewBubble: TextView
    private lateinit var correctViewBubble: TextView
    private lateinit var wrongViewBubble: TextView
    private lateinit var timerViewBubble: TextView

    private val encouragements by lazy {
        listOf(
            R.string.yourSuperSmart,
            R.string.youCanDoIt,
            R.string.keepGoing,
            R.string.yourTheBest,
            R.string.thisIsEasy).map { getString(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.layout_game_fragment, container, false)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameViewModel.cancelTimer()

        statsBar = layoutInflater.inflate(R.layout.layout_game_timer_bubble, view as ViewGroup, false) as ViewGroup

        pointsViewBubble = statsBar[0] as TextView
        correctViewBubble = statsBar[1] as TextView
        wrongViewBubble = statsBar[2] as TextView
        timerViewBubble = statsBar[3] as TextView

        choicesView.adapter = adapter

        (activity as? AppCompatActivity)?.supportActionBar?.subtitle = ""
        with(gameViewModel) {
            observableChoices.observe(this@GameFragment, Observer { adapter.update(it) })
            observableQuestion.observe(this@GameFragment, Observer { loadQuestion(it) })
            observableIndex.observe(this@GameFragment, Observer { (activity as AppCompatActivity).supportActionBar?.subtitle = getString(R.string.nSlashNDone, it.first, it.second) })
            observableWrong.observe(this@GameFragment, Observer { updateWrongCount(it) })
            observableCorrect.observe(this@GameFragment, Observer { updateCorrectCount(it) })
            observablePoints.observe(this@GameFragment, Observer { updatePointsCount(it) })
            observableOnTimeOut.observe(this@GameFragment, Observer { alertTimesUp() })
            observableOnDone.observe(this@GameFragment, Observer { showSummaryScreen() })
            observableTimeLeft.observe(this@GameFragment, Observer {
                timerViewBubble.text = "$it"
                if (it in 0..difficulty.secondsUntilLate) alertTimeRunningOut()
            })
        }
        restartButton.setOnClickListener { restart() }
        if (savedInstanceState == null)
            with(appViewModel) {
                observableWords.observe(this@GameFragment, Observer { gameViewModel.initialize(it) })
                triggerFetch()
            } else gameViewModel.restore()

        setHasOptionsMenu(true)
    }

    private fun updateWrongCount(wrong: Int) {
        wrongViewBubble.animateUpdate(getString(R.string.minusN, wrong))
        wrongProgressBar.apply {
            progress = wrong
            max = gameViewModel.totalCards
        }
        wrongView.text = "$wrong"
    }

    private fun updateCorrectCount(correct: Int) {
        correctViewBubble.animateUpdate(getString(R.string.plusN, correct))
        correctProgressBar.apply {
            progress = correct
            max = gameViewModel.totalCards
        }
        Logger.log(this, "Correct: $correct")
        correctView.text = "$correct"
    }

    private fun updatePointsCount(points: Long) {
        val text = "$points"
        scoreView.text = text
        pointsViewBubble.animateUpdate(text)
    }

    private fun loadQuestion(question: Word) {
        if (gameViewModel.inGameMode) {
            questionView.rotation = 0F
            questionView.text = question.scrambledText
            timerViewBubble.setTextColor(requireContext().getColor(android.R.color.white))
            messageView.text = encouragements.random()
            gameViewModel.startTimer()
        }
    }

    private fun alertTimeRunningOut() {
        messageView.setText(R.string.hurryTime)
        timerViewBubble.setTextColor(requireContext().getColor(R.color.red))
    }

    private fun alertTimesUp() {
        questionView.rotation = 180F
        messageView.text = (encouragements.filter { it != messageView.text }).random()
        adapter.toggleFlipUpsideDown(choicesView)
    }

    private fun TextView.animateUpdate(text: String) {
        this.text = text
        if (gameViewModel.animate && gameViewModel.inGameMode) startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up))
    }

    private fun showSummaryScreen() {
        gameAreaGroup.isVisible = false
        summaryGroup.isVisible = true/*
        val numbers = (0..gameViewModel.actualPoints + 1).toList()
        var index = -1

        val handler = scoreView?.handler
        if (handler != null) {
            val runnable = object : Runnable {

                override fun run() {
                    index += 5
                    if (index <= gameViewModel.actualPoints) scoreView.text = "${numbers[index]}"
                    if (scoreView.isVisible) handler.postDelayed(this, 1L)
                }
            }
            runnable.run()
        } else */
        scoreView.text = "${gameViewModel.actualPoints}"
    }

    private fun restart() {
        gameAreaGroup.isVisible = true
        summaryGroup.isVisible = false
        gameViewModel.restart()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.apply {
            add(R.string.stats).apply {
                actionView = statsBar
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
            add(R.string.edit) { EventBus.getDefault().post(Events.AddFragment(WordsFragment())) }
            addSub(R.string.difficulty) { menu ->
                Difficulty.values().forEach { difficulty ->
                    menu.add(difficulty.nameRes) {
                        gameViewModel.changeDifficulty(difficulty)
                    }
                }
            }
        }
    }

}