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
        tab?.addTabs(listOf(
            "Jan 2018", "Feb 2018", "Mar 2018", "Apr 2018", "May 2018", "Jun 2018", "July 2018",
            "Aug 2018", "Sep 2018", "Oct 2018", "Nov 2018", "Dec 2018"
        ))
    }
}