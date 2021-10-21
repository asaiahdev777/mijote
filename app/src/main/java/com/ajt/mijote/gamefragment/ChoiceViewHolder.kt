package com.ajt.mijote.gamefragment

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ajt.mijote.R

class ChoiceViewHolder(row: View) : RecyclerView.ViewHolder(row) {
    val choiceButton : Button = itemView.findViewById(R.id.choiceButton)
}