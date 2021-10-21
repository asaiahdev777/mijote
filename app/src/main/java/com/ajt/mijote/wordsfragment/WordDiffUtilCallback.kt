package com.ajt.mijote.wordsfragment

import androidx.recyclerview.widget.DiffUtil
import com.ajt.mijote.model.Word

class WordDiffUtilCallback(private val oldWords : MutableList<Word>, private val newWords : MutableList<Word>) : DiffUtil.Callback() {

    override fun getOldListSize() = oldWords.size

    override fun getNewListSize() = newWords.size

    override fun areItemsTheSame(oldItemPosition : Int, newItemPosition: Int) = oldWords[oldItemPosition] == newWords[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = areItemsTheSame(oldItemPosition, newItemPosition)

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }

}