package com.ajt.mijote.wordsfragment

import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ajt.mijote.R

class WordsViewHolder(row: View) : RecyclerView.ViewHolder(row) {
    val positionView : TextView = itemView.findViewById(R.id.positionView)
    val nameView : EditText = itemView.findViewById(R.id.textView)
    val moreButton : ImageView = itemView.findViewById(R.id.moreButton)
}