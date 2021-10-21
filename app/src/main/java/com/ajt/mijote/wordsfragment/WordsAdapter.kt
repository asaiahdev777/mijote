package com.ajt.mijote.wordsfragment

import android.content.Context
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajt.mijote.R
import com.ajt.mijote.extensions.forceShowKB
import com.ajt.mijote.extensions.loadText
import com.ajt.mijote.extensions.returnHardCopy
import com.ajt.mijote.extensions.appViewModel
import com.ajt.mijote.model.Word
import java.util.*


class WordsAdapter(private val layoutInflater: LayoutInflater, private val context: Context) : RecyclerView.Adapter<WordsViewHolder>() {

    private var forceUpdate = true
    private var words = mutableListOf<Word>()

    companion object {
        const val updatePositionsPayload = "UPDATE_POSITIONS"
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        addItemTouchHelper(recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    }

    private fun addItemTouchHelper(recyclerView: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                rearrangeWords(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                words.forEachIndexed { index, word -> word.wordOrder = index + 1L }
                appViewModel.updateWords(words)
            }

            override fun isLongPressDragEnabled() = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val word = words[position]

                if (direction == ItemTouchHelper.END) showDeleteDialog(word, position)
                else if (direction == ItemTouchHelper.START) appViewModel.toggleWordIncludedExcluded(word)
            }
        }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun <T> List<T>.rearrange(from: Int, to: Int) {
        if (from != to) {
            if (from > to) rearrange(from - 1, to)
            else rearrange(from + 1, to)
            Collections.swap(this, from, to)
        }
    }

    private fun rearrangeWords(from: Int, to: Int) {
        words.rearrange(from, to)
        //rearrange(words, from, to)
        notifyItemMoved(from, to)
        updatePositions()
    }

    private fun updatePositions() = notifyItemRangeChanged(0, words.size, updatePositionsPayload)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WordsViewHolder(layoutInflater.inflate(R.layout.layout_words_item, parent, false))

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else holder.updatePosition(position)
    }

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int) {
        val word = words[position]
        with(holder) {
            val text = SpannableString(word.text)
            if (!word.included) text.setSpan(StrikethroughSpan(), 0, text.length, 0)
            nameView.loadText(text)
            nameView.addBatchEdit(word) { word.text = it }
            updatePosition(position)
            if (word.inFocus) nameView.forceShowKB()
            word.inFocus = false
        }
    }

    private fun WordsViewHolder.updatePosition(position: Int) {
        positionView.text = itemView.context.getString(R.string.numberPeriod, position + 1)
    }

    private fun EditText.addBatchEdit(word: Word, onChange: (String) -> Unit) {

        fun removeTextWatcher() = if (tag is TextWatcher) removeTextChangedListener(tag as TextWatcher) else Unit

        removeTextWatcher()

        inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
        setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                removeTextWatcher()
                appViewModel.getWords()
            } else tag = doAfterTextChanged {
                onChange(it?.toString() ?: "")
                appViewModel.updateWord(word)
            }
        }
    }

    override fun getItemCount() = words.size

    fun update(newWords: MutableList<Word>) {
        val oldWords = words.returnHardCopy
        words = newWords.returnHardCopy
        if (!forceUpdate) appViewModel.calculateDiff(this, oldWords, newWords) else notifyDataSetChanged()
        forceUpdate = false
    }

    fun addWord(word: Word) {
        words.add(word)
        notifyItemInserted(words.lastIndex)
    }

    private fun showDeleteDialog(word: Word, position: Int) {
        AlertDialog.Builder(context).apply {
            setCancelable(false)
            setTitle(context.getString(R.string.deleteWordPrompt, word.text))
            setPositiveButton(R.string.delete) { _, _ ->
                forceUpdate = true
                appViewModel.deleteWord(word)
            }
            setNegativeButton(android.R.string.cancel) { _, _ -> notifyItemChanged(position) }
        }.show()
    }

}