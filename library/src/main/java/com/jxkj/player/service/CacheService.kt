package com.jxkj.player.service

import android.app.Notification
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.util.Util
import com.jxkj.player.utils.NotificationHandle
import com.jxkj.player.core.utils.CachePresenter


/**
 *Desc:
 *Author:Zhu
 *Date:2022/8/9
 */
class CacheService : BaseDownloadService(CACHE_NOTIFICATION_ID) {
    companion object {
        const val CACHE_NOTIFICATION_ID = 12
        const val CACHE_CHANNEL_ID = "cache_channel_id"
        const val JOB_ID=1
    }

    override fun getDownloadManager(): DownloadManager {
        return CachePresenter.instance().getDownloadManager(this)
    }

    override fun getScheduler(): Scheduler? {
      return  if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return NotificationHandle.createNotification(
            applicationContext,
            CACHE_CHANNEL_ID,
            "视频缓存",
            "视频缓存前台服务",false
        ).build()
    }
}