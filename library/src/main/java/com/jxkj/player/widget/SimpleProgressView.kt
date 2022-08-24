package com.jxkj.player.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 *Desc:
 *Author:Zhu
 *Date:2022/8/15
 */
class SimpleProgressView(context: Context, attributeSet: AttributeSet) :
    View(context, attributeSet) {
    val max = 20
    var progress = 0
        set(value) {
            if (value > max) field = max
            else if (value < 0) field = 0
            else {
                if (field==value)return
                field = value
                invalidate()
            }
        }
    private var boundF = RectF()
    private var bound = Rect()
    private var progressBound = RectF()
    private val bgColor = Color.parseColor("#CCCCCC")
    private val mPaint: Paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            strokeWidth = 1f
        }
    }

    override fun onDraw(canvas: Canvas) {
        val width = width
        val height = height
        boundF.set(0f, 0f, width.toFloat(), height.toFloat())
        bound.set(0, 0, width, height)
        val radios = height * 1.0f / 2
        mPaint.color = bgColor
        canvas.drawRoundRect(boundF, radios, radios, mPaint)
        drawProgress(width.toFloat(), height.toFloat(), canvas)
    }

    private fun drawProgress(width: Float, height: Float, canvas: Canvas) {
        if (progress == 0) return
        val radios = height * 1.0f / 2
        mPaint.color = Color.WHITE
        val progressWidth = width * progress / max
        progressBound.set(0f, 0F, progressWidth, height)
        canvas.drawRoundRect(progressBound, radios, radios, mPaint)
    }
}