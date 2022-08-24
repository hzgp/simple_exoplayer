package com.jxkj.player.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jxkj.player.R

/**
 *Desc:
 *Author:Zhu
 *Date:2022/7/31
 */
object NotificationHandle {
    var hasRegisterChannel=false
    fun registerPushChannel(
            context: Context,
            channelId: String ,
            importance: Boolean = true
    ):NotificationChannel? {
        if (hasRegisterChannel)return null
        hasRegisterChannel =true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager: NotificationManager =
                    context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            // 用户可以看到的通知渠道的名字。
            val name: CharSequence = context.resources.getString(R.string.app_name)
            // 用户可以看到的通知渠道的描述。
            val description = "${name}+专用通道"
            val mChannel = NotificationChannel(
                    channelId,
                    name,
                    if (importance) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_MIN
            )
            // 配置通知渠道的属性。
            mChannel.description = description
            // 设置通知出现时的震动（如果Android设备支持的话）。
            mChannel.enableVibration(importance)
            mChannel.vibrationPattern =
                    if (importance) longArrayOf(100, 200, 300, 300) else longArrayOf(0)
            // 最后在notificationmanager中创建该通知渠道。
            mNotificationManager.createNotificationChannel(mChannel)
            return mChannel
        }
        return null
}
    fun createNotification(context: Context,channelId: String,title:String,content:String,isImportance:Boolean=true): NotificationCompat.Builder{
        val notifyBuilder = NotificationCompat.Builder(context, channelId)
        notifyBuilder.setContentText(content).setContentTitle(title)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setVibrate(longArrayOf(0)).priority =if (isImportance) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_MIN
        return notifyBuilder
    }
    fun cancelNotification(context: Context,notificationId:Int){
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}