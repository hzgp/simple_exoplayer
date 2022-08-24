package com.jxkj.player.core.utils

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.jxkj.player.core.bean.PlayItem
import com.jxkj.player.service.BaseDownloadService
import com.jxkj.player.service.CacheService
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.Executors


/**
 *Desc:
 *Author:Zhu
 *Date:2022/8/9
 */
class CachePresenter private constructor() {

    var databaseProvider: StandaloneDatabaseProvider? = null
    var mHttpDataSourceFactory: DataSource.Factory? = null
    var mDataSourceFactory: DataSource.Factory? = null
    var mDownloadCache: Cache? = null
    var mDownloadManager: DownloadManager? = null
    fun getDataBaseProvider(context: Context): StandaloneDatabaseProvider {
        if (databaseProvider == null) databaseProvider = StandaloneDatabaseProvider(context)
        return databaseProvider!!
    }

    fun getHttpDataSourceFactory(): DataSource.Factory {
        if (mHttpDataSourceFactory == null) {
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
            CookieHandler.setDefault(cookieManager)
            mHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        }
        return mHttpDataSourceFactory!!
    }

    fun getDataSourceFactory(context: Context): DataSource.Factory {
        if (mDataSourceFactory == null) {
            mDataSourceFactory = buildReadOnlyCacheDataSource(context)
        }
        return mDataSourceFactory!!
    }

    fun getDownloadCache(context: Context): Cache {
        if (mDownloadCache == null) {
            val cacheMediaDir = File(context.getExternalFilesDir(null), "video_cache")
            if (!cacheMediaDir.exists()) cacheMediaDir.mkdirs()
            mDownloadCache =
                SimpleCache(cacheMediaDir, NoOpCacheEvictor(), getDataBaseProvider(context))
        }
        return mDownloadCache!!
    }

    fun buildReadOnlyCacheDataSource(
        context: Context
    ): CacheDataSource.Factory {
        val upstreamFactory = DefaultDataSource.Factory(context, getHttpDataSourceFactory())
        return CacheDataSource.Factory()
            .setCache(getDownloadCache(context))
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun ensureDownloadManagerInitialized(context: Context) {
        if (mDownloadManager == null) {
            mDownloadManager = DownloadManager(
                context,
                getDataBaseProvider(context),
                getDownloadCache(context),
                getHttpDataSourceFactory(),
                Executors.newFixedThreadPool( /* nThreads= */6)
            )
        }
    }

    fun getDownloadManager(context: Context): DownloadManager {
        ensureDownloadManagerInitialized(context)
        return mDownloadManager!!
    }

    fun addCacheTask(context: Context, mediaItem: PlayItem) {
        BaseDownloadService.sendAddDownload(
            context,
            CacheService::class.java,
            DownloadRequest.Builder(mediaItem.url, Uri.parse(mediaItem.url)).build(),
            false
        )
    }
    fun clearCacheTask(context: Context){
        BaseDownloadService.sendRemoveAllDownloads(context,CacheService::class.java,false)
    }
    fun cancelCacheTask(context: Context){
        BaseDownloadService.sendPauseDownloads(context,CacheService::class.java,false)
    }
    fun resumeCacheTask(context: Context){
        BaseDownloadService.sendResumeDownloads(context,CacheService::class.java,false)
    }

    companion object {
        fun instance(): CachePresenter {
            return Holder.instance
        }
    }

    object Holder {
        var instance = CachePresenter()
    }
}