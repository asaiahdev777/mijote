package com.ajt.mijote.gamefragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajt.mijote.R
import com.ajt.mijote.model.Word

class ChoicesAdapter(private val layoutInflater: LayoutInflater, private val gameViewModel: GameViewModel) : RecyclerView.Adapter<ChoiceViewHolder>() {

    private var choices = listOf<Word>()
    private lateinit var recyclerView: RecyclerView
    private var height = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        recyclerView.doOnLayout { height = it.measuredHeight }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChoiceViewHolder(layoutInflater.inflate(R.layout.choice_item, parent, false))

    override fun onBindViewHolder(holder: ChoiceViewHolder, position: Int) {
        val word = choices[position]
        with(holder) {
            itemView.rotation = if (word.flipUpsideDown) 180F else 0F
            (itemView.layoutParams as RecyclerView.LayoutParams).apply {
                height = (this@ChoicesAdapter.height / itemCount) - (itemCount * bottomMargin)
                if (position == choices.lastIndex) bottomMargin = 0
            }
            choiceButton.text = word.text
            choiceButton.setOnClickListener { gameViewModel.onClickChoice(word) }
        }
    }

    override fun getItemCount() = choices.size

    fun update(newWords: List<Word>) {
        choices = newWords
        choices.forEach { it.flipUpsideDown = false }
        notifyDataSetChanged()
    }

    fun toggleFlipUpsideDown(recyclerView: RecyclerView) {
        choices.forEach { it.flipUpsideDown = !it.flipUpsideDown }
        notifyItemRangeChanged(0, choices.size, 0)
        //TransitionManager.beginDelayedTransition(recyclerView)
    }
}