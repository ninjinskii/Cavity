package com.louis.app.cavity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.louis.app.cavity.ui.CountyScrollableTab

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tab = findViewById<CountyScrollableTab>(R.id.tab)
        tab.addTabs(listOf(
            "Jan 2018", "Feb 2018", "Mar 2018", "Apr 2018", "May 2018", "Jun 2018", "July 2018",
            "Aug 2018", "Sep 2018", "Oct 2018", "Nov 2018", "Dec 2018"
        ))
    }
}