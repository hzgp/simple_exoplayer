package com.jxkj.player.core.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.jxkj.player.R
import com.jxkj.player.core.ExPlayerHandler
import com.jxkj.player.core.bean.CachePolicy
import com.jxkj.player.core.bean.PlayItem
import com.jxkj.player.core.utils.ScreenUtils
import com.jxkj.player.service.CacheService
import com.jxkj.player.utils.NotificationHandle
import com.jxkj.player.widget.SimpleProgressView

/**
 *Desc:
 *Author:Zhu
 *Date:2022/8/5
 */
abstract class BasePlayerActivity : AppCompatActivity() {
    var playView: StyledPlayerView? = null
        private set
    private var progress: SimpleProgressView? = null
    protected lateinit var orientationUtil: ScreenUtils

    open protected fun layoutId(): Int {
        return R.layout.activity_base_play
    }

    open protected fun prePrepare(){

    }
    open protected fun autoPlay(): Boolean {
        return true
    }

    protected fun setRepeatModel(@Player.RepeatMode model: Int) {
        player?.setRepeatModel(model)
    }

    open protected fun setCachePolicy(policy: CachePolicy) {
       player?.setCachePolicy(policy)
    }

    protected var player: ExPlayerHandler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHandle.registerPushChannel(this, CacheService.CACHE_CHANNEL_ID, false)
        /**必须提供@+id/play_view 的PlayerView*/
        setContentView(R.layout.activity_base_play)
        progress = findViewById(R.id.progress)
        window.run {
            attributes.screenBrightness = 0.8f
            attributes = attributes
        }
        initViews()
        prePrepare()
        preparePlayer()
    }


    private fun preparePlayer() {
        player?.run {
            addMediaItemsByItem(playList())
            prepare()
        }
    }

    open fun getFullScreenButton(): ImageView? {
        val fullScreen: ImageView? = findViewById(R.id.fullscreen)
        return fullScreen
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }


    override fun onResume() {
        super.onResume()
        player?.run { if (autoPlay()) play() }
    }

    open fun initViews() {
        LayoutInflater.from(this).inflate(layoutId(), findViewById(R.id.root), true)
        playView = findViewById(R.id.play_view)

        playView?.let {
            player = ExPlayerHandler.create(it, getVideoTitle())
            orientationUtil = ScreenUtils(this)
            player?.addListener(orientationUtil)
        }
        getFullScreenButton()?.setOnClickListener { orientationUtil.changeOrientation() }
        findViewById<View?>(R.id.back)?.setOnClickListener { onBackPressed() }
    }

    abstract fun playList(): List<PlayItem>
    protected fun getVideoTitle(): TextView? {
        val titleView: TextView? = findViewById(R.id.video_title)
        return titleView
    }

    override fun onBackPressed() {
        if (orientationUtil.isFullScreen) {
            orientationUtil.changeOrientation()
            return
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.onDestroy()
    }
}