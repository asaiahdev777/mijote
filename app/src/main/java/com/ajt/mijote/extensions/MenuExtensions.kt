package com.ajt.mijote.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import androidx.core.view.children
import androidx.core.view.forEach
import com.ajt.mijote.R


fun createMenu(
        anchor: View,
        onDismiss: PopupMenu.OnDismissListener? = null,
        onClick: (Menu.(Menu) -> Unit)? = null
) = PopupMenu(anchor.context, anchor).apply {
            gravity = Gravity.TOP or Gravity.CENTER
            setOnDismissListener(onDismiss)
            MenuCompat.setGroupDividerEnabled(menu, true)
            if (onClick != null) menu.onClick(menu)
            menu.clearHeaders()
            show()
        }

fun View.onClickMenu(
        overrideAnchor: View? = null,
        useContext: Context? = null,
        onDismiss: PopupMenu.OnDismissListener? = null,
        onClick: Menu.(Menu) -> Unit
) {
    setOnClickListener {
        PopupMenu(useContext ?: context, overrideAnchor ?: this).apply {
            setOnDismissListener(onDismiss)
            MenuCompat.setGroupDividerEnabled(menu, true)
            menu.onClick(menu)
            menu.clearHeaders()
            show()
        }
    }
}

inline fun Menu.add(
    title: CharSequence,
    itemId: Int = 0,
    group: Int = 0,
    crossinline onClick: (MenuItem) -> Unit
): MenuItem = add(group, itemId, 0, title).apply {
    clearHeaders()
    setOnMenuItemClickListener {
        onClick(this)
        false
    }
}

inline fun Menu.add(title: Int, itemId: Int = 0, group: Int = 0, noinline onClick: ((MenuItem) -> Unit)? = null): MenuItem =
        add(group, itemId, 0, title).apply {
            clearHeaders()
            setOnMenuItemClickListener {
                onClick?.invoke(this)
                false
            }
        }

fun Menu.addSub(title: Int, group: Int = 0, withMenu: ((SubMenu) -> Unit)? = null): SubMenu =
        addSubMenu(group, 0, 0, title).apply {
            MenuCompat.setGroupDividerEnabled(this, true)
            if (withMenu != null) withMenu(this)
        }

fun Menu.addSub(title: String, group: Int = 0, withMenu: ((Menu) -> Unit)? = null): SubMenu =
        addSubMenu(group, 0, 0, title).apply {
            MenuCompat.setGroupDividerEnabled(this, true)
            if (withMenu != null) withMenu(this)
        }

fun Menu.clearHeaders() {
    forEach {
        it.subMenu?.apply {
            clearHeader()
            item?.subMenu?.clearHeaders()
        }
    }
}

fun Menu.onClick(id: Int, onClick: (MenuItem) -> Unit) =
        findItem(id).setOnMenuItemClickListener {
            onClick(it)
            false
        }!!

fun MenuItem.onClick(onClick: (MenuItem) -> Unit) =
        setOnMenuItemClickListener {
            onClick(it)
            false
        }!!

fun Context.getThemeColor(res: Int) = TypedValue().apply { theme.resolveAttribute(res, this, true) }.data

fun Menu.tintIcons(context: Context) {
    children.forEach {
        it.icon?.setTintList(ColorStateList.valueOf(context.getThemeColor(R.attr.iconColorContrasted)))
    }
}