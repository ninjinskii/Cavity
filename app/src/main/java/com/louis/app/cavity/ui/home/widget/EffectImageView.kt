package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView

class EffectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    @RequiresApi(Build.VERSION_CODES.S)
    private val underlyingContentNode = RenderNode("underlying-content")

    @RequiresApi(Build.VERSION_CODES.S)
    private val blur = RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP)

    private val targets = mutableListOf<Pair<TextView, RenderNode>>()

    // Pre allocated render nodes to use with lines if targeting mutli-line text views.
    @RequiresApi(Build.VERSION_CODES.Q)
    private val additionalRenderNodes = listOf(
        RenderNode("blur-textviewline-1"),
        RenderNode("blur-textviewline-2"),
        RenderNode("blur-textviewline-3")
    )

    @RequiresApi(Build.VERSION_CODES.S)
    fun setTargets(views: List<TextView>) {
        this.targets.clear()
        this.targets.addAll(views.map { it to RenderNode("blur-${it.id}") })
    }

    override fun onDraw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            return super.onDraw(canvas)
        }

        // Consider setUseCompositingLayers ifs this method is called really frequently
        underlyingContentNode.setPosition(0, 0, width, height)
        val contentCanvas = underlyingContentNode.beginRecording()
        super.onDraw(contentCanvas)
        underlyingContentNode.endRecording()
        canvas.drawRenderNode(underlyingContentNode)

        // Image is now drawn as usual, now draw blur behind targeted views
        targets.forEach { pair ->
            val (view, renderNode) = pair
            val layout = view.layout
            val textViewReady = view.layout != null && view.layout.lineCount > 0

            if (textViewReady) {
                var i = 0

                for (line in 0..< layout.lineCount) {
                    val renderNode = if (i++ == 0) renderNode else additionalRenderNodes[i]
                    val lineStart = view.left + layout.getLineLeft(line)
                    val lineEnd = view.left + layout.getLineRight(line)
                    val lineBottom = view.top + layout.getLineBottom(line)
                    val lineTop = view.top + layout.getLineTop(line)
                    val right = lineEnd - lineStart
                    val bottom = lineBottom - lineTop

                    with(renderNode) {
                        setRenderEffect(blur)
                        setPosition(0, 0, right.toInt(), bottom)
                        translationX = lineStart
                        translationY = lineTop.toFloat()
                        val blurCanvas = beginRecording()
                        blurCanvas.translate(-lineStart, -lineTop.toFloat())
                        blurCanvas.drawRenderNode(underlyingContentNode)
                        endRecording()
                        canvas.drawRenderNode(this)
                    }
                }
            }
        }
    }
}
