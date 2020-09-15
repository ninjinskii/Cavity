package com.louis.app.cavity.ui

import android.animation.AnimatorInflater
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.util.L

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isToolbarShadowShown = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        setupDrawer()
    }

    private fun setupDrawer() {
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
}
