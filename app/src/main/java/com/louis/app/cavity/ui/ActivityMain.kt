package com.louis.app.cavity.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
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

        initDialogWatcher(navController)
        binding.navView.setupWithNavController(navController)

        observe()
    }

    private fun observe() {
        addItemViewModel.userFeedback.observe(this) {
            it.getContentIfNotHandled()?.let { stringRes ->
                onShowSnackbarRequested(stringRes)
            }
        }
    }

    private fun initDialogWatcher(navController: NavController) {
        navController.addOnDestinationChangedListener { controller, _, _ ->
//            controller.currentDestination?.id?.let {
//                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
//                val fragment = navHostFragment?.childFragmentManager!!.fragments[0]
//
//                if (fragment is DialogWatcher) {
//                    fragment.closeMaybeOpenedDialog()
//                }
//            }
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
