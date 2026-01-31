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

/**
 * An ImageView supposed to have overlaying TextViews
 * TextViews denominated by setTargets() will have their background blurred
 */
class EffectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    @RequiresApi(Build.VERSION_CODES.S)
    private val underlyingContentNode = RenderNode("underlying-content")

    @RequiresApi(Build.VERSION_CODES.S)
    private val blur = RenderEffect.createBlurEffect(25f, 25f, Shader.TileMode.CLAMP)

    private val targets = mutableListOf<Target>()

    @RequiresApi(Build.VERSION_CODES.S)
    fun setTargets(views: List<TextView>) {
        val lowerBound = 1
        val upperBound = 10
        val hasUndefinedTextLineCount = views.any { it.maxLines !in lowerBound..upperBound }

        if (hasUndefinedTextLineCount) {
            throw IllegalStateException("Parameter views must contain only TextViews with a defined maxLines attribute between ${lowerBound}..${upperBound}")
        }

        this.targets.clear()
        val targets = views.map {
            val renderNodes = List(it.maxLines) { index -> RenderNode("blur-${it.id}-line-$index") }
            Target(it, renderNodes)
        }

        this.targets.addAll(targets)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun drawBlurBehindTargets(canvas: Canvas) {
        for (target in targets) {
            val (view, renderNodes) = target
            val layout = view.layout
            val textViewReady = view.layout != null && view.layout.lineCount > 0

            if (!textViewReady) {
                continue
            }

            for (line in 0 until layout.lineCount) {
                val renderNode = renderNodes[line]
                val lineStart = view.left + layout.getLineLeft(line)
                val lineEnd = view.left + layout.getLineRight(line)
                val lineBottom = view.top + layout.getLineBottom(line).toFloat()
                val lineTop = view.top + layout.getLineTop(line).toFloat()
                val right = lineEnd - lineStart
                val bottom = lineBottom - lineTop

                with(renderNode) {
                    setRenderEffect(blur)
                    setPosition(0, 0, right.toInt(), bottom.toInt())
                    translationX = lineStart
                    translationY = lineTop
                    val blurCanvas = beginRecording()
                    blurCanvas.translate(-lineStart, -lineTop)
                    blurCanvas.drawRenderNode(underlyingContentNode)
                    endRecording()
                    canvas.drawRenderNode(this)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            return super.onDraw(canvas)
        }

        // Recording image, and drawing it as an ImageView would do
        underlyingContentNode.setPosition(0, 0, width, height)
        val contentCanvas = underlyingContentNode.beginRecording()
        super.onDraw(contentCanvas)
        underlyingContentNode.endRecording()
        canvas.drawRenderNode(underlyingContentNode)

        // Image is now drawn as usual, now process to compute and draw blur
        drawBlurBehindTargets(canvas)
    }

    data class Target(val view: TextView, val renderNodes: List<RenderNode>)
}
