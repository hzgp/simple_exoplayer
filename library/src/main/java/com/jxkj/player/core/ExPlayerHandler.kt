package com.jxkj.player.core

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.jxkj.player.R
import com.jxkj.player.core.bean.CachePolicy
import com.jxkj.player.core.bean.PlayItem
import com.jxkj.player.core.listener.PlayerTouchListener
import com.jxkj.player.core.listener.SlidePosition
import com.jxkj.player.core.utils.CachePresenter
import com.jxkj.player.widget.SimpleProgressView

/**
 *Desc:
 *Author:Zhu
 *Date:2022/8/5
 */
class ExPlayerHandler private constructor() {
    private var player: ExoPlayer? = null
    private var attachedActivity: Activity? = null
    private lateinit var factory: ProgressiveMediaSource.Factory
    private var mCachePolicy = CachePolicy.ALL
    private val audioManager: AudioManager? by lazy {
        attachedActivity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    }

    private var progressView: View? = null
    private var titleView: TextView? = null
    private var isShowProcess = false
    private var process: SimpleProgressView? = null
    private var ivProcessIcon: ImageView? = null
    private var playerView: StyledPlayerView? = null
        set(value) {
            field = value
            if (value == null) return
            progressView = value.findViewById(R.id.progress_container)
            process = progressView?.findViewById(R.id.progress)
            ivProcessIcon = progressView?.findViewById(R.id.progress_icon)
        }

    fun setCachePolicy(policy: CachePolicy) {
        this.mCachePolicy = policy
    }

    var repeatMode = Player.REPEAT_MODE_ALL
        private set

    fun addListener(listener: Player.Listener) {
        player?.addListener(listener)
    }

    fun setAppScreenBrightness(increment: Int, isFirst: Boolean) {
        attachedActivity?.let {

            val window = it.window
            val lp = window.attributes
            var newScreenBrightness = lp.screenBrightness + (0.05f * increment)
            if (newScreenBrightness <= 0) newScreenBrightness = 0.1f
            else if (newScreenBrightness > 1) newScreenBrightness = 1f
            if (lp.screenBrightness == newScreenBrightness) return@let
            lp.screenBrightness = newScreenBrightness
            if (isFirst) {
                ivProcessIcon?.setImageResource(R.mipmap.ic_screen_light)
                process?.progress = (lp.screenBrightness * 20).toInt()
            }
            process?.let { it.progress += increment }
            window.attributes = lp
        }

    }

    private var pitch = -1f
    fun setPitch(increment: Int, isFirst: Boolean) {
        audioManager?.run {
            val volume = getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (pitch < 0) pitch = volume * 1.0f / maxVolume
            var newPitch = pitch + (0.05f * increment)
            if (newPitch < 0) newPitch = 0.0f
            else if (newPitch > 1) newPitch = 1f
            process?.let {
                if (isFirst) {
                    it.progress = (newPitch * 20).toInt()
                    ivProcessIcon?.setImageResource(if (it.progress > 0) R.mipmap.ic_volume else R.mipmap.ic_no_volume)
                }
                it.progress += increment
            }
            if (pitch == 0.0f && newPitch != 0.0f) {
                ivProcessIcon?.setImageResource(R.mipmap.ic_volume)
            } else if (pitch != 0.0f && newPitch == 0.0f) {
                ivProcessIcon?.setImageResource(R.mipmap.ic_no_volume)
            }
            if (pitch == newPitch) return@run
            pitch = newPitch
            setStreamVolume(AudioManager.STREAM_MUSIC, ((maxVolume * pitch).toInt()), 0)
        }
    }

    fun setRepeatModel(@Player.RepeatMode repeatMode: Int) {
        if (repeatMode == this.repeatMode) return
        this.repeatMode = repeatMode
        player?.repeatMode = repeatMode
    }

    fun prepare() {
        player?.run {
            if (isPlaying) return@run
            prepare()
        }
    }

    fun play() {
        player?.run {
            if (isPlaying) return@run
            play()
        }
    }

    fun next(): Boolean {
        return player?.let {
            if (it.hasNextMediaItem()) {
                it.seekToNext()
                true
            } else false
        } ?: false
    }

    fun previous(): Boolean {
        return player?.let {
            if (it.hasPreviousMediaItem()) {
                it.seekToPrevious()
                true
            } else false
        } ?: false
    }

    fun pause() {
        player?.run { if (isPlaying) pause() }
    }

    fun initListener() {
        val activity = attachedActivity ?: return
        val touchListener = PlayerTouchListener(activity)
        touchListener.mVerticalSlideListener = { slidePosition, increment, isShow ->
            updateVertical(slidePosition, increment, isShow)
        }
        playerView?.setOnTouchListener(touchListener)
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val tag = mediaItem?.localConfiguration?.tag
                titleView?.text = tag.toString()
                if (!(tag is PlayItem)) return
                val cachePresenter = CachePresenter.instance()
                if (mCachePolicy == CachePolicy.ALL || mCachePolicy == CachePolicy.CURRENT)
                    cachePresenter.addCacheTask(activity, tag)
                if (mCachePolicy == CachePolicy.CURRENT) {
                    cachePresenter.clearCacheTask(activity)
                }
            }
        }
        addListener(listener)
    }

    private fun updateVertical(slidePosition: SlidePosition, increment: Int, show: Boolean) {
        if (show) {
            if (!isShowProcess) {
                progressView?.visibility = View.VISIBLE
            }
            when (slidePosition) {
                SlidePosition.LIFT -> setAppScreenBrightness(increment, !isShowProcess)
                SlidePosition.RIGHT -> setPitch(increment, !isShowProcess)
                else -> {
                }
            }
            isShowProcess = show
        } else if (isShowProcess) {
            isShowProcess = show
            progressView?.visibility = View.GONE
        }
    }

    fun addMediaItem(mediaUrl: String) {
        player?.run {
            val mediaItem = factory.createMediaSource(MediaItem.fromUri(mediaUrl))
            addMediaSource(mediaItem)
        }
    }

    fun addMediaItemsByUrl(mediaUrls: List<String>) {
        player?.run {
            mediaUrls.forEach { mediaUrl ->
                val mediaItem = factory.createMediaSource(
                    MediaItem.Builder().setUri(mediaUrl).setTag(PlayItem(mediaUrl)).build()
                )
                addMediaSource(mediaItem)
            }
            playbackParameters = PlaybackParameters(1.0f)
        }
    }

    fun addMediaItemsByItem(playItems: List<PlayItem>) {
        player?.run {
            playItems.forEach { playItem ->
                val mediaItem = factory.createMediaSource(
                    MediaItem.Builder().setUri(playItem.url).setTag(playItem).build()
                )//
                addMediaSource(mediaItem)
            }
        }
    }

    fun addMediaItem(mediaUri: Uri) {
        player?.run {
            val mediaItem =
                factory.createMediaSource(
                    MediaItem.Builder().setUri(mediaUri).setTag(PlayItem(mediaUri.path ?: ""))
                        .build()
                )
            addMediaSource(mediaItem)
        }
    }

    fun addMediaItemsByUri(mediaUris: List<Uri>) {
        player?.run {
            mediaUris.forEach { mediaUri ->
                val mediaItem = factory.createMediaSource(MediaItem.fromUri(mediaUri))
                addMediaSource(mediaItem)
            }
        }
    }

    companion object {
        fun create(playerView: StyledPlayerView, titleView: TextView? = null): ExPlayerHandler {
            val exoPlayer = ExPlayerHandler()
            val context = playerView.context
            exoPlayer.titleView = titleView
            exoPlayer.playerView = playerView
            if (context is Activity) exoPlayer.attachedActivity = context
            val applicationContext = context.applicationContext
            val cacheDataSourceFactory =
                CachePresenter.instance().buildReadOnlyCacheDataSource(applicationContext)
            val videoTrackSelectionFactory = DefaultRenderersFactory(applicationContext)
            // 创建播放器实例
            val playerBuilder = ExoPlayer.Builder(applicationContext, videoTrackSelectionFactory)
            // .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            val player = playerBuilder.build()
            player.repeatMode = Player.REPEAT_MODE_ALL
            playerView.player = player
            return exoPlayer.also {
                it.player = player
                it.factory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                it.initListener()
            }
        }
    }


    fun onDestroy() {
        player?.let { exoplayer ->
            exoplayer.release()
            if (mCachePolicy == CachePolicy.CURRENT) attachedActivity?.run {
                CachePresenter.instance().cancelCacheTask(this)
            }
            player = null
        }
    }
}