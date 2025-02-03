package com.louis.app.cavity.ui.search.widget

import android.view.View
import com.louis.app.cavity.util.extractMargin
import com.louis.app.cavity.util.extractPadding

class InsettableInfo(val view: View) {
    val initialPadding = view.extractPadding()
    val initialMargin = view.extractMargin()
}
