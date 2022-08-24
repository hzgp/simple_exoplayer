package com.jxkj.player.core.listener

import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.jxkj.player.utils.CommonUtil
import kotlin.math.abs

/**
 *Desc:
 *Author:Zhu
 *Date:2022/8/15
 */
class PlayerTouchListener(
    context: Context,
    val unit: Int = 20,
    refWidth: Float = 300f,
    refHeight: Float = 200f
) :
    View.OnTouchListener {
    private var isMove = false
    private var startX = 0.0f
    private var startY = 0.0f
    private var rwidth = 0
    private var rheight = 0
    private var unitX = 0.0f
    private var unitY = 0.0f
    private lateinit var position: SlidePosition
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                reset(v, event)
            }
            MotionEvent.ACTION_UP -> {
                if (!isMove) v.performClick()
                else mVerticalSlideListener?.let { it(SlidePosition.NONE, -1, false) }
            }
            MotionEvent.ACTION_MOVE -> {
                isMove = true
                handleSlideVertical(event)
            }
        }
        return true
    }

    private fun reset(v: View, event: MotionEvent) {
        isMove = false
        startX = event.x
        startY = event.y
        val viewWidth = v.width
        unitX = rwidth * 1.0f / unit
        unitY = rheight * 1.0f / unit
        position = if (startX <= viewWidth / 2) SlidePosition.LIFT else SlidePosition.RIGHT
    }

    private fun handleHorizontal(event: MotionEvent) {
        mHorizontalSlideListener?.let {
            val newY = event.y
            val offsetY = newY - startY
        }
    }

    private fun handleSlideVertical(event: MotionEvent) {
        mVerticalSlideListener?.let {
            val newY = event.y
            val offsetY = newY - startY
            if (abs(offsetY) >= unitY) {
                startY = newY
                it(position, (offsetY / unitY * -1).toInt(), true)
            }
        }
    }

    var mVerticalSlideListener: VerticalSlideListener? = null
    private var mHorizontalSlideListener: HorizontalSlideListener? = null

    init {
        val size = CommonUtil.dp2px(context, refWidth, refHeight)
        rwidth = size[0]
        rheight = size[1]
    }

}
typealias VerticalSlideListener = (SlidePosition, Int, Boolean) -> Unit
typealias HorizontalSlideListener = (Float) -> Unit