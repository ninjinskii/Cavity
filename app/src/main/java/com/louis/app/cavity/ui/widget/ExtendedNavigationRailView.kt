package com.louis.app.cavity.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.navigationrail.NavigationRailView
import com.louis.app.cavity.R

class ExtendedNavigationRailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.navigationRailStyle,
    defStyleRes: Int = R.style.Widget_MaterialComponents_NavigationRailView
) :
    NavigationRailView(context, attrs, defStyleAttr, defStyleRes) {

    override fun getMaxItemCount() = super.getMaxItemCount() + 3
}
