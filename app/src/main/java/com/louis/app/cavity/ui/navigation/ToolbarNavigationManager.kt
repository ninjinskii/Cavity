package com.louis.app.cavity.ui.navigation

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.transition.TransitionManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.louis.app.cavity.R

class ToolbarNavigationManager(
    private val activity: AppCompatActivity,
    private val toolbar: Toolbar,
    private val drawerLayout: DrawerLayout? = null
) {

    private val fragmentManager = activity.supportFragmentManager
    private var arrowDrawable: DrawerArrowDrawable? = null
    private var animator: ValueAnimator? = null

    fun setup() {
        activity.setSupportActionBar(toolbar)

        // Listener sur le back stack
        fragmentManager.addOnBackStackChangedListener(
            object : FragmentManager.OnBackStackChangedListener {
                override fun onBackStackChanged() {
                    updateNavigationIcon()
                    fragmentManager.removeOnBackStackChangedListener(this)
                }
            })

        // état initial
        updateNavigationIcon()

        toolbar.setNavigationOnClickListener {
            if (isTopLevel()) {
                drawerLayout?.openDrawer(GravityCompat.START)
            } else {
                activity.onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun isTopLevel(): Boolean {
        return fragmentManager.backStackEntryCount == 0
    }

    private fun updateNavigationIcon() {
        val isTopLevel = isTopLevel()

        val newIcon = if (isTopLevel) {
            AppCompatResources.getDrawable(activity, R.drawable.ic_heart)
        } else {
            AppCompatResources.getDrawable(activity, R.drawable.ic_arrow_back)
        }

        arrow(isTopLevel && drawerLayout != null)
    }

    private fun arrow(showAsDrawerIndicator: Boolean) {
        val (arrow, animate) =
            arrowDrawable?.run { this to true }
                ?: (DrawerArrowDrawable(activity).also { arrowDrawable = it } to false)

        animateIconChange(arrow)
//        toolbar.navigationIcon = arrow

        val endValue = if (showAsDrawerIndicator) 0f else 1f
        if (animate) {
            val startValue = arrow.progress
            animator?.cancel()
            animator = ObjectAnimator.ofFloat(arrow, "progress", startValue, endValue)
            (animator as ObjectAnimator).start()
        } else {
            arrow.progress = endValue
        }
    }

    private fun animateIconChange(newIcon: Drawable?) {
        val currentIcon = toolbar.navigationIcon

        val shouldAnimate = currentIcon != null && newIcon != null

        if (shouldAnimate) {
            TransitionManager.beginDelayedTransition(toolbar)
        }

        toolbar.navigationIcon = newIcon
    }
}
