package com.louis.app.cavity.ui

import android.animation.ObjectAnimator
import android.app.ActivityManager.TaskDescription
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ActivityMainBinding
import com.louis.app.cavity.ui.account.LoginViewModel
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.ui.settings.SettingsViewModel
import com.louis.app.cavity.ui.tasting.TastingViewModel
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.showSnackbar
import com.louis.app.cavity.util.themeColor
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.louis.app.cavity.ui.home.FragmentHome
import com.louis.app.cavity.ui.navigation.GlobalRoute
import com.louis.app.cavity.ui.navigation.NavigationProvider
import com.louis.app.cavity.ui.navigation.Navigator
import com.louis.app.cavity.util.L
import kotlinx.coroutines.launch
import kotlin.math.abs

class ActivityMain : AppCompatActivity(), SnackbarProvider, NavigationProvider {
    private lateinit var binding: ActivityMainBinding
    private val addItemViewModel: AddItemViewModel by viewModels()
    private val tastingViewModel: TastingViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()

    override val navigator = Navigator(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        val isAndroid31 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val isAndroid35 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

        if (!isAndroid31) {
            setTheme(R.style.CavityTheme)
        } else {
            initSplashScreen()
        }

        if (isAndroid35) {
            enableEdgeToEdge()
        }

        checkPreventScreenshot()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        applyInsets()
        polishAppSwitcherApparence()
        setupNavigation()
        observe()
        setupOnBackPressed()

        if (hasNavigationRail()) {
            lockDrawer()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                UiEventManager.events.collect {
                    when (it) {
                        is UiEvent.WineUpdated -> {
                            L.v("ActivityMain: receive wine updated wineId: ${it.wineId}, countyId: ${it.countyId}, message: ${it.message}")
                            sharedViewModel.updateWineState(it)
                            showSnackbar(it.message, R.id.snackbarAnchor)
                        }
                        is UiEvent.Snackbar -> showSnackbar(it.message, it.anchorViewId)
                        is UiEvent.ActionSnackbar -> showSnackbar(
                            it.message,
                            it.anchorViewId,
                            it.actionLabel,
                            it.action
                        )
                    }
                }
            }
        }
    }

    private fun checkPreventScreenshot() {
        val isPreventScreenshotsEnabled = settingsViewModel.getPreventScreenshots()
        val isAndroid33 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val secureFlag = WindowManager.LayoutParams.FLAG_SECURE

        if (isPreventScreenshotsEnabled) {
            window.setFlags(secureFlag, secureFlag)
        } else {
            window.clearFlags(secureFlag)
        }

        if (isAndroid33) {
            setRecentsScreenshotEnabled(!isPreventScreenshotsEnabled)
        }
    }

    private fun initSplashScreen() {
        // Avoids flashing content when it is ready to drawn but splash screen icon anim
        // isn't finished yet. Content will be redrawn when splash screen finishes
        var preventedInitialDraw = false
        val splashScreen = installSplashScreen()

        val content: View? = findViewById(android.R.id.content)
        content?.viewTreeObserver?.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return if (preventedInitialDraw) {
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    true
                } else {
                    preventedInitialDraw = true
                    false
                }
            }
        })

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashScreenView = splashScreenViewProvider.view
            val animationDuration = splashScreenViewProvider.iconAnimationDurationMillis
            val animationStart = splashScreenViewProvider.iconAnimationStartMillis
            val remainingDuration =
                (animationDuration - abs(animationStart - System.currentTimeMillis()))
                    .coerceAtLeast(0L)

            ObjectAnimator.ofFloat(splashScreenView, View.ALPHA, 1f, 0f).apply {
                duration = 300
                startDelay = remainingDuration - 400
                doOnEnd { splashScreenViewProvider.remove() }
                start()
            }
        }
    }

    private fun applyInsets() {
        binding.navView.prepareWindowInsets { view, windowInsets, left, top, _, bottom ->
            view.updatePadding(top = top, left = left, bottom = bottom)
            val screenWidth = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    val windowMetrics = windowManager.currentWindowMetrics
                    val screenInsets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                        WindowInsetsCompat.Type.systemBars() or
                                WindowInsetsCompat.Type.displayCutout()
                    )

                    windowMetrics.bounds.width() - screenInsets.left - screenInsets.right
                }

                else -> {
                    // Mandatory for api < R
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay.width
                }
            }

            view.measure(screenWidth, 0)
            view.layoutParams.width = view.measuredWidth + left

            windowInsets
        }
    }

    // We have to support old android 7.1 TaskDescription constructor
    @Suppress("deprecation")
    private fun polishAppSwitcherApparence() {
        val appName = getString(R.string.app_name)
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        setTaskDescription(
            TaskDescription(
                appName,
                bitmap,
                themeColor(com.google.android.material.R.attr.colorSurface)
            )
        )
    }

    private fun setupNavigation() {
        binding.navView.apply {
            navigator.syncMenu(menu)
            setNavigationItemSelectedListener { item ->
                item.isChecked = true
                navigator.navigate(GlobalRoute.To(item.itemId))
                binding.drawer.close()
                true
            }
        }

        binding.navigationRail?.apply {
            navigator.syncMenu(menu)
            setOnItemSelectedListener { item ->
                item.isChecked = true
                navigator.navigate(GlobalRoute.To(item.itemId))
                true
            }
        }
    }

    private fun observe() {
        addItemViewModel.userFeedback.observe(this) {
            it.getContentIfNotHandled()?.let { stringRes ->
                onShowSnackbarRequested(stringRes)
            }
        }

        loginViewModel.userFeedback.observe(this) {
            it.getContentIfNotHandled()?.let { stringRes ->
                onShowSnackbarRequested(stringRes)
            }
        }

        loginViewModel.userFeedbackString.observe(this) {
            it.getContentIfNotHandled()?.let { string ->
                binding.main.coordinator.showSnackbar(string)
            }
        }

        tastingViewModel.hasTastingToday.observe(this) { hasTastingToday ->
            showTastingIndicator(hasTastingToday)
        }
    }

    private fun showTastingIndicator(show: Boolean) {
        if (hasNavigationRail()) {
            binding.navigationRail!!.getOrCreateBadge(R.id.tasting_dest).apply {
                backgroundColor =
                    binding.navigationRail!!.context
                        .themeColor(com.google.android.material.R.attr.colorPrimaryFixed)
                isVisible = show
            }
        } else {
            val tastingItem = binding.navView.menu[1]

            if (show) {
                tastingItem.setActionView(R.layout.dot)
            } else {
                tastingItem.actionView = null
            }
        }
    }

    private fun lockDrawer() {
        binding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun hasNavigationRail() = binding.navigationRail != null

    fun requestMediaPersistentPermission(mediaUri: Uri, silent: Boolean = false) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        try {
            contentResolver?.takePersistableUriPermission(mediaUri, flags)
                ?: throw NullPointerException()
        } catch (_: SecurityException) {
            if (!silent) {
                onShowSnackbarRequested(R.string.persistent_permission_error)
            }
        } catch (_: NullPointerException) {
            if (!silent) {
                onShowSnackbarRequested(R.string.base_error)
            }
        }
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun showSnackbar(
        @StringRes message: Int,
        @IdRes anchorViewId: Int?,
        @StringRes actionLabel: Int? = null,
        action: (() -> Unit) = {}
    ) {
        val anchorView: View? = anchorViewId?.let { findViewById(it) }
        binding.main.coordinator.showSnackbar(message, actionLabel, anchorView) { view ->
            action.invoke()
        }
    }

    override fun onResume() {
        super.onResume()
        navigator.setup()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        settingsViewModel.notifyWindowFocusChanged(hasFocus)
    }

    override fun onShowSnackbarRequested(stringRes: Int) {
        binding.root.post {
            val currentDestination = navigator.getCurrentFragment()
            val isHome = currentDestination is FragmentHome
            val anchor = if (isHome) binding.main.snackbarAnchor else null
            binding.main.coordinator.showSnackbar(stringRes, anchorView = anchor)
        }
    }
}
