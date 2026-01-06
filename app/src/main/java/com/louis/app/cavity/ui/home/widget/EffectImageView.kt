package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

    // Pre allocated render nodes to use with lines if targeting textviews.
    @RequiresApi(Build.VERSION_CODES.Q)
    private val additionalRenderNodes = listOf(
        RenderNode("blur-textviewline-1"),
        RenderNode("blur-textviewline-2"),
        RenderNode("blur-textviewline-3")
    )

    private val testPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun setTargets(views: List<TextView>) {
        this.targets.clear()
        this.targets.addAll(views.map { it to RenderNode("blur-${it.id}") })
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun drawBlur(
        canvas: Canvas,
        renderNode: RenderNode,
        left: Int,
        top: Int,
        width: Int,
        height: Int
    ) {
        with(renderNode) {
            setRenderEffect(blur)
            setPosition(0, 0, width, height)
            translationX = left.toFloat()
            translationY = top.toFloat()
            val blurCanvas = beginRecording()
            blurCanvas.translate(-left.toFloat(), -top.toFloat())
            blurCanvas.drawRenderNode(underlyingContentNode)
            endRecording()
            canvas.drawRenderNode(this)
        }
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
            val hasMultipleLines = view.layout != null && view.layout.lineCount > 1

            if (hasMultipleLines) {
                var i = 0

                for (line in 0..< layout.lineCount) {
                    val renderNode = if (i++ == 0) renderNode else additionalRenderNodes[i]
                    val lineStart = view.left + layout.getLineLeft(line)
                    val lineEnd = view.left + layout.getLineRight(line)
                    val lineBottom = view.top + layout.getLineBottom(line)
                    val lineTop = view.top + layout.getLineTop(line)

                    // Debug
                    canvas.drawCircle(
                        lineStart,
                        lineTop.toFloat(),
                        10f,
                        testPaint.apply { color = Color.RED })
                    canvas.drawCircle(
                        lineEnd,
                        lineTop.toFloat(),
                        10f,
                        testPaint.apply { color = Color.CYAN })
                    canvas.drawCircle(
                        lineStart,
                        lineBottom.toFloat(),
                        10f,
                        testPaint.apply { color = Color.BLUE })
                    canvas.drawCircle(
                        lineEnd,
                        lineBottom.toFloat(),
                        10f,
                        testPaint.apply { color = Color.MAGENTA })

                    /*drawBlur(
                        canvas,
                        renderNode,
                        0,
                        0,
                        (lineEnd - lineStart).toInt(),
                        lineBottom - lineTop
                    )*/


                    with(renderNode) {
                        setRenderEffect(blur)
                        setPosition(0, 0, (lineEnd - lineStart).toInt(), lineBottom - lineTop)
                        canvas.drawLine(0f, 0f, lineEnd - lineStart, (lineBottom - lineTop).toFloat(), testPaint.apply { color = Color.GREEN })
                        translationX = lineStart
                        translationY = lineTop.toFloat()
                        val blurCanvas = beginRecording()
                        blurCanvas.translate(-lineStart, -lineTop.toFloat())
                        blurCanvas.drawRenderNode(underlyingContentNode)
                        endRecording()
                        canvas.drawRenderNode(this)
                    }
                }
            } else {
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
}
