package com.louis.app.cavity.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.util.showSnackbar

class ActivityMain : AppCompatActivity(), SnackbarProvider {
    private lateinit var binding: ActivityMainBinding
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    private var isToolbarShadowShown = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        setupDrawer()
    }

    private fun setupDrawer() {
        navController = findNavController(R.id.navHostFragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawer)
        navigationView = binding.navView
    }

    fun setToolbarShadow(setVisible: Boolean) {
//        val toolbar = binding.main.toolbarLayout
//
//        if (setVisible && !isToolbarShadowShown) {
//            toolbar.stateListAnimator =
//                AnimatorInflater.loadStateListAnimator(this, R.animator.show_elevation)
//            isToolbarShadowShown = true
//        } else {
//            toolbar.stateListAnimator =
//                AnimatorInflater.loadStateListAnimator(this, R.animator.hide_elevation)
//            isToolbarShadowShown = false
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(findNavController(R.id.navHostFragment), appBarConfiguration)
    }

    override fun onShowSnackbarRequested(stringRes: Int) {
        binding.main.coordinator.showSnackbar(stringRes, anchorView = binding.main.snackbarAnchor)
    }
}
