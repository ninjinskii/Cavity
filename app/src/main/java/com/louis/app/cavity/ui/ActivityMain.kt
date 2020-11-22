package com.louis.app.cavity.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.util.showSnackbar

class ActivityMain : AppCompatActivity(), SnackbarProvider {
    lateinit var navView: NavigationView
    lateinit var drawer: DrawerLayout
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        exposeNavigationStuff()
    }

    private fun exposeNavigationStuff() {
        navView = binding.navView
        drawer = binding.drawer
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onShowSnackbarRequested(stringRes: Int) {
        binding.main.coordinator.showSnackbar(stringRes, anchorView = binding.main.snackbarAnchor)
    }
}
