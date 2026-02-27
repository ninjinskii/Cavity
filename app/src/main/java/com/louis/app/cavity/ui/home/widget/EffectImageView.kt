package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
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
        if (views.isEmpty()) {
            this.targets.clear()
            return
        }

        val lowerBound = 1
        val upperBound = 10
        val hasUndefinedTextLineCount = views.any { it.maxLines !in lowerBound..upperBound }

        if (hasUndefinedTextLineCount) {
            throw IllegalStateException("Parameter views must contain only TextViews with a defined maxLines attribute between ${lowerBound}..${upperBound}")
        }

        this.targets.clear()
        val targets = views.map {
            val renderNodes = List(it.maxLines) { index -> RenderNode("blur-${it.id}-line-$index") }
            Target(it, getRelativePositionToParent(it), renderNodes)
        }

        this.targets.addAll(targets)
    }

    private fun getRelativePositionToParent(view: View): IntArray {
        val location = IntArray(2)
        var currentView: View? = view
        var currentParent: View? = currentView?.parent as? View

        if (currentView === this.parent) {
            location[0] = 0
            location[1] = 0
            return location
        }

        while (currentParent != null && currentParent !== this.parent) {
            val tempLocation = IntArray(2)
            currentView?.getLocationInParent(tempLocation)

            val layoutParams = currentView?.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                location[0] += tempLocation[0] + layoutParams.leftMargin
                location[1] += tempLocation[1] + layoutParams.topMargin
            } else {
                location[0] += tempLocation[0]
                location[1] += tempLocation[1]
            }

            location[0] += currentView?.paddingLeft ?: 0
            location[1] += currentView?.paddingTop ?: 0

            currentView = currentParent
            currentParent = currentView.parent as? View
        }


        if (currentParent === this.parent) {
            currentView?.getLocationInParent(location)

            val layoutParams = currentView?.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                location[0] += layoutParams.leftMargin
                location[1] += layoutParams.topMargin
            }


            location[0] += (currentView?.paddingLeft ?: 0)
            location[1] += (currentView?.paddingTop ?: 0)
        }

        return location
    }

    fun View.getLocationInParent(location: IntArray) {
        val locationInParent = IntArray(2)
        this.getLocationInWindow(locationInParent)
        location[0] = locationInParent[0]
        location[1] = locationInParent[1]
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun drawBlurBehindTargets(canvas: Canvas) {
        for (target in targets) {
            val (view, spacings, renderNodes) = target
            val layout = view.layout
            val textViewReady = view.layout != null && view.layout.lineCount > 0

            if (!textViewReady) {
                continue
            }

            for (line in 0 until layout.lineCount) {
                val renderNode = renderNodes[line]
                val lineStart = view.left + layout.getLineLeft(line) + spacings[0]
                val lineEnd = view.left + layout.getLineRight(line) + spacings[0]
                val lineBottom = view.top + layout.getLineBottom(line).toFloat() + spacings[1]
                val lineTop = view.top + layout.getLineTop(line).toFloat() + spacings[1]
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
        val hasImage = this.drawable != null

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S || !hasImage) {
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

    data class Target(
        val view: TextView,
        val spacing: IntArray,
        val renderNodes: List<RenderNode>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Target

            if (view != other.view) return false
            if (!spacing.contentEquals(other.spacing)) return false
            if (renderNodes != other.renderNodes) return false

            return true
        }

        override fun hashCode(): Int {
            var result = view.hashCode()
            result = 31 * result + spacing.contentHashCode()
            result = 31 * result + renderNodes.hashCode()
            return result
        }
    }
}
