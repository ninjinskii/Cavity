package com.louis.app.cavity.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.util.showSnackbar

class ActivityMain : AppCompatActivity(), SnackbarProvider {
    private lateinit var binding: ActivityMainBinding
    private val addItemViewModel: AddItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
        val navController = navHostFragment?.findNavController()

        binding.navView.setupWithNavController(navController ?: return)

        observe()
    }

    private fun observe() {
        addItemViewModel.userFeedback.observe(this) {
            it.getContentIfNotHandled()?.let { stringRes ->
                onShowSnackbarRequested(stringRes, useAnchorView = false)
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onShowSnackbarRequested(stringRes: Int, useAnchorView: Boolean) {
        val anchor = if (useAnchorView) binding.main.snackbarAnchor else null
        binding.main.coordinator.showSnackbar(stringRes, anchorView = anchor)
    }
}
