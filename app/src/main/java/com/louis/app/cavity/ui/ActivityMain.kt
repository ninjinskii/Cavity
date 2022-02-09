package com.louis.app.cavity.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.util.showSnackbar

class ActivityMain : AppCompatActivity(), SnackbarProvider {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: Fragment
    private val addItemViewModel: AddItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
        val navController = navHostFragment.findNavController()

        binding.navView.setupWithNavController(navController)
        binding.navigationRail?.setupWithNavController(navController)

        fixShitty2_4_0NavigationComponentBehavior()
        observe()
        maybeLockDrawer()
    }

    private fun fixShitty2_4_0NavigationComponentBehavior() {
        val navController = navHostFragment.findNavController()

        fun doNavigate(@IdRes id: Int): Boolean {
            val options = NavOptions.Builder()
                .setRestoreState(false) // f*ck off
                .build()

            navController.navigate(id, null, options)

            return true
        }

        // When navigating from the drawer to a saved (automatically saved from 2.4.0),
        // non top destination, drawer does not close
        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.drawer.closeDrawer(GravityCompat.START)
        }

        // Override default NavigationUI component to disable restore state
        binding.navView.setNavigationItemSelectedListener {
            doNavigate(it.itemId)
        }

        // Override default NavigationUI component to disable restore state
        binding.navigationRail?.setOnItemSelectedListener {
            doNavigate(it.itemId)
        }

    }

    private fun observe() {
        addItemViewModel.userFeedback.observe(this) {
            it.getContentIfNotHandled()?.let { stringRes ->
                onShowSnackbarRequested(stringRes)
            }
        }
    }

    private fun maybeLockDrawer() {
        val hasNavigationRail = binding.navigationRail != null

        if (hasNavigationRail) {
            binding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    fun requestMediaPersistentPermission(mediaUri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        try {
            contentResolver?.takePersistableUriPermission(mediaUri, flags)
                ?: throw NullPointerException()
        } catch (e: SecurityException) {
            onShowSnackbarRequested(R.string.persistent_permission_error)
        } catch (e: NullPointerException) {
            onShowSnackbarRequested(R.string.base_error)
        }
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onShowSnackbarRequested(stringRes: Int) {
        val currentDestination = navHostFragment.findNavController().currentDestination
        val isHome = currentDestination?.id == R.id.home_dest
        val anchor = if (isHome) binding.main.snackbarAnchor else null

        binding.main.coordinator.showSnackbar(stringRes, anchorView = anchor)
    }
}
