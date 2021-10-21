package com.ajt.mijote.extensions

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.postDelayed
import com.ajt.mijote.app.CustomApp
import com.ajt.mijote.model.Copyable

inline val appViewModel get() = CustomApp.appViewModel

//Used to load some text into an EditText and position the cursor at the end of its contents
fun TextView.loadText(s: Any) {
    text = if (s !is CharSequence) s.toString() else s
    if (this is EditText) setSelection(text.length, text.length)
    post { scrollTo(0, 0) }
}


fun dpToPx(dp: Int) = (dp * Resources.getSystem().displayMetrics.scaledDensity + 0.5f).toInt()

val View.inputMethodManager get() = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

fun View.showKB() {
    requestFocus()
    postDelayed(16) { inputMethodManager.showSoftInput(this, 0) }
}

//Universal function for hiding keyboard
fun View.hideKB() {
    clearFocus()
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.forceShowKB(onVisible: (() -> Unit)? = null) {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            onVisible?.invoke()
            postDelayed(25L) { showKB() }
        }

        override fun onViewDetachedFromWindow(v: View?) {
            //inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        }
    })
    showKB()
}

@Suppress("UNCHECKED_CAST")
val <T> List<T>.returnHardCopy get() = map { (it as Copyable<T>).duplicate() }.toMutableList()