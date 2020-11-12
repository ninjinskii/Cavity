package com.louis.app.cavity.ui

import android.animation.AnimatorInflater
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showSnackbar

class ActivityMain : AppCompatActivity(), SnackbarProvider {
    lateinit var navView: NavigationView
    lateinit var drawer: DrawerLayout
    private lateinit var binding: ActivityMainBinding
    private var isToolbarShadowShown = true
    private var hasCustomToolbar = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        exposeNavigationStuff()
    }

    private fun exposeNavigationStuff() {
        navView = binding.navView
        drawer = binding.drawer
    }

    override fun onShowSnackbarRequested(stringRes: Int) {
        binding.main.coordinator.showSnackbar(stringRes, anchorView = binding.main.snackbarAnchor)
    }
}
