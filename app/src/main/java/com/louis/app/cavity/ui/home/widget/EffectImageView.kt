package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView

class EffectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    @RequiresApi(Build.VERSION_CODES.R)
    private val underlyingContentNode = RenderNode("underlying-content")

    @RequiresApi(Build.VERSION_CODES.R)
    private val blur = RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP)

    private val targets = mutableListOf<Pair<View, RenderNode>>()

    @RequiresApi(Build.VERSION_CODES.R)
    fun setTargets(views: List<View>) {
        this.targets.clear()
        this.targets.addAll(views.map { it to RenderNode("blur-${it.id}") })
    }

    override fun onDraw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            return super.onDraw(canvas)
        }

        // consider setUseCompositingLayers is this method is called really frequently
        underlyingContentNode.setPosition(0, 0, width, height)
        val contentCanvas = underlyingContentNode.beginRecording()
        super.onDraw(contentCanvas)
        underlyingContentNode.endRecording()
        canvas.drawRenderNode(underlyingContentNode)

        // Image is now drawn as usual, now draw blur behind targeted views

        targets.forEach { pair ->
            val (view, renderNode) = pair

            with(renderNode) {
                setRenderEffect(blur)
                setPosition(0, 0, view.width, view.height)
                translationX = view.left.toFloat()
                translationY = view.top.toFloat()
                val blurCanvas = beginRecording()
                blurCanvas.translate(-view.left.toFloat(), -view.top.toFloat())
                blurCanvas.drawRenderNode(underlyingContentNode)
                endRecording()
                canvas.drawRenderNode(this)
            }
        }
    }
}
