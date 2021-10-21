package com.ajt.mijote.model

import androidx.fragment.app.Fragment

sealed class Events {
    class AddFragment(val fragment: Fragment)
}