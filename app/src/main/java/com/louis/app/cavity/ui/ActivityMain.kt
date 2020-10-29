package com.louis.app.cavity.ui

import android.animation.AnimatorInflater
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showSnackbar

class ActivityMain : AppCompatActivity(), SnackbarProvider {
    private lateinit var binding: ActivityMainBinding
    private var isToolbarShadowShown = true
    private var hasCustomToolbar = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        setupDrawer()
    }

    private fun setupDrawer() {
        setSupportActionBar(binding.main.toolbar)

        val navController = findNavController(R.id.navHostFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawer)

        binding.main.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    fun setToolbarShadow(setVisible: Boolean) {
        val toolbar = binding.main.toolbarLayout

        if (setVisible && !isToolbarShadowShown) {
            toolbar.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(this, R.animator.show_elevation)
            isToolbarShadowShown = true
        } else {
            toolbar.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(this, R.animator.hide_elevation)
            isToolbarShadowShown = false
        }
    }

    fun hideMainToolbar() = binding.main.toolbarLayout.setVisible(false)

    fun showMainToolbar() = binding.main.toolbarLayout.setVisible(true)

    override fun onShowSnackbarRequested(stringRes: Int) {
        binding.main.coordinator.showSnackbar(stringRes, anchorView = binding.main.snackbarAnchor)
    }
}
