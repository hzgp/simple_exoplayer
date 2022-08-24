package com.jxkj.player.core.utils

import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize
import com.jxkj.player.R
import com.jxkj.player.core.activity.BasePlayerActivity
import com.jxkj.player.utils.CommonUtil
import java.lang.ref.WeakReference

/**
 *Desc:
 *Author:Zhu
 *Date:2022/8/8
 */
open class ScreenUtils constructor(activity: BasePlayerActivity) : Player.Listener {
    var mActivity: WeakReference<BasePlayerActivity>? = null
    private var screenWidth = 0
    private var screenHeight = 0
    private var currentFullType = FULL_NON
    private var normalHeight = 0
    private var videoWidth = 0
    private var resetting=false
    private var videoHeight = 0
    var isFullScreen = false
        private set
    private var mScreenType = SCREEN_ORIENTATION_PORTRAIT
    fun isLand(): Boolean {
        return mScreenType != SCREEN_ORIENTATION_PORTRAIT
    }

    fun changeOrientation() {
        val activity = mActivity?.get() ?: return
        if (resetting)return
        resetting=true
        if (isFullScreen) {
            quitFullscreen(activity)
        } else {
            fullscreen(activity)
        }
        resetting=false
    }

    private fun fullscreen(activity: BasePlayerActivity) {
        isFullScreen = true
        if (videoWidth <= videoHeight) {
            currentFullType = FULL_PORTRAIT
            activity.playView?.layoutParams?.height = screenHeight
        } else {
            currentFullType = FULL_LANDSCAPE
            mScreenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            setRequestedOrientation(mScreenType)
            activity.playView?.layoutParams?.height = screenWidth
        }

        activity.getFullScreenButton()?.setImageResource(R.mipmap.video_enlarge_quit)
        CommonUtil.showBar(activity, false)
    }

    private fun quitFullscreen(activity: BasePlayerActivity) {
        isFullScreen = false
        if (isLand()) {
            mScreenType = SCREEN_ORIENTATION_PORTRAIT
            setRequestedOrientation(mScreenType)
        }
        activity.playView?.layoutParams?.height = normalHeight
        CommonUtil.showBar(activity, true)
        activity.getFullScreenButton()?.setImageResource(R.mipmap.video_enlarge)
    }

    private fun updateFullscreen() {
        val activity = mActivity?.get() ?: return
        if (videoWidth <= videoHeight) {
            if (currentFullType == FULL_PORTRAIT) return
            mScreenType = SCREEN_ORIENTATION_PORTRAIT
            setRequestedOrientation(mScreenType)
            currentFullType = FULL_PORTRAIT
            activity.playView?.layoutParams?.height = screenHeight
        } else {
            if (currentFullType == FULL_LANDSCAPE) return
            mScreenType = SCREEN_ORIENTATION_LANDSCAPE
            setRequestedOrientation(mScreenType)
            currentFullType = FULL_LANDSCAPE
            activity.playView?.layoutParams?.height = screenWidth
        }
    }

    private fun setRequestedOrientation(requestedOrientation: Int) {
        val activity = mActivity!!.get() ?: return
        try {
            activity.requestedOrientation = requestedOrientation
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        videoWidth = videoSize.width
        videoHeight = videoSize.height
        if (isFullScreen) {
            updateFullscreen()
        }
    }

    init {
        mActivity = WeakReference(activity)
        screenWidth = CommonUtil.getScreenWidth(activity)
        screenHeight = CommonUtil.getScreenHeight(activity)
        normalHeight = activity.playView?.layoutParams?.height
            ?: activity.resources.getDimensionPixelSize(R.dimen.dp_250)
    }

    companion object {
        const val FULL_NON = -1
        const val FULL_PORTRAIT = 1
        const val FULL_LANDSCAPE = 2
    }
}