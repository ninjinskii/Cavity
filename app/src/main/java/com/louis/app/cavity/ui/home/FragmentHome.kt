package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.louis.app.cavity.R
import com.louis.app.cavity.ui.CountyScrollableTab

class FragmentHome : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tab = activity?.findViewById<CountyScrollableTab>(R.id.tab)
        tab?.addTabs(
            listOf(
                "Alsace", "Beaujolais", "Bourgogne", "Bordeaux", "Italie", "Suisse",
                "Langudoc-Roussillon", "Jura"
            )
        )
    }
}