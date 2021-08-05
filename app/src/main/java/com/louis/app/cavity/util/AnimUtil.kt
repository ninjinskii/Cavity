package com.louis.app.cavity.util

import android.animation.AnimatorInflater
import android.view.View
import androidx.core.animation.doOnEnd
import com.google.android.material.card.MaterialCardView
import com.louis.app.cavity.R

object AnimUtil {
    fun flipContainer(appearing: View, disappearing: View) {
        val context = appearing.context

        // We need to get different animators each time to prevent bugs when animating multiple items
        val flipOut = AnimatorInflater.loadAnimator(context, R.animator.flip_out)
        val flipIn = AnimatorInflater.loadAnimator(context, R.animator.flip_in)
        val scale = context.resources.displayMetrics.density
        val cameraDist = 8000 * scale

        appearing.cameraDistance = cameraDist
        disappearing.cameraDistance = cameraDist

        appearing.setVisible(true)

        var elevation = 0f

        if (appearing is MaterialCardView) {
            elevation = appearing.elevation
            appearing.elevation = 0f
            disappearing.elevation = 0f
        }

        flipOut.setTarget(disappearing)
        flipIn.setTarget(appearing)
        flipOut.start()
        flipIn.start()
        flipIn.doOnEnd {
            disappearing.setVisible(false)
            appearing.elevation = elevation
            disappearing.elevation = elevation
        }
    }
}
