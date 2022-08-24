package com.jxkj.player.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.TintContextWrapper
import androidx.fragment.app.FragmentActivity
import com.jxkj.player.R

/**
 * 公共类
 * Created by shuyu on 2016/11/11.
 */
object CommonUtil {
    /**
     * Get activity from context object
     *
     * @param context something
     * @return object of Activity or null if it is not Activity
     */
    fun scanForActivity(context: Context?): Activity? {
        if (context == null) return null
        if (context is Activity) {
            return context
        } else if (context is TintContextWrapper) {
            return scanForActivity(context.baseContext)
        } else if (context is ContextWrapper) {
            return scanForActivity(context.baseContext)
        }
        return null
    }

    /**
     * 获取状态栏高度
     *
     * @param context 上下文
     * @return 状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources
            .getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /**
     * 获取ActionBar高度
     *
     * @param activity activity
     * @return ActionBar高度
     */
    fun getActionBarHeight(activity: Activity): Int {
        val tv = TypedValue()
        return if (activity.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, activity.resources.displayMetrics)
        } else 0
    }

    @SuppressLint("RestrictedApi")
    fun hideSupportActionBar(context: Context?, actionBar: Boolean, statusBar: Boolean) {
        if (actionBar) {
            val appCompatActivity = getAppCompActivity(context)
            if (appCompatActivity != null) {
                val ab = appCompatActivity.supportActionBar
                if (ab != null) {
                    ab.setShowHideAnimationEnabled(false)
                    ab.hide()
                }
            }
        }
        if (statusBar) {
            if (context is FragmentActivity) {
                context.window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            } else if (context is Activity) {
                context.window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            } else {
                getActivityNestWrapper(context)!!.window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        }
    }

    fun showBar(context: Context?, show: Boolean) {
        if (show) showSupportActionBar(context, true, true) else hideSupportActionBar(
            context,
            true,
            true
        )
    }

    @SuppressLint("RestrictedApi")
    fun showSupportActionBar(context: Context?, actionBar: Boolean, statusBar: Boolean) {
        if (actionBar) {
            val appCompatActivity = getAppCompActivity(context)
            if (appCompatActivity != null) {
                val ab = appCompatActivity.supportActionBar
                if (ab != null) {
                    ab.setShowHideAnimationEnabled(false)
                    ab.show()
                }
            }
        }
        if (statusBar) {
            if (context is FragmentActivity) {
                context.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else if (context is Activity) {
                context.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                getActivityNestWrapper(context)!!.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }

    fun getAppCompActivity(context: Context?): AppCompatActivity? {
        if (context == null) return null
        if (context is AppCompatActivity) {
            return context
        } else if (context is ContextThemeWrapper) {
            return getAppCompActivity(context.baseContext)
        }
        return null
    }

    /**
     * Get Activity from context
     *
     * @param context
     * @return AppCompatActivity if it's not null
     */
    fun getActivityNestWrapper(context: Context?): Activity? {
        return getActivityContext(context)
    }

    /**
     * 获取屏幕的宽度px
     *
     * @param context 上下文
     * @return 屏幕宽px
     */
    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    /**
     * 获取屏幕的高度px
     *
     * @param context 上下文
     * @return 屏幕高px
     */
    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics() // 创建了一张白纸
        windowManager.defaultDisplay.getMetrics(outMetrics) // 给白纸设置宽高
        return outMetrics.heightPixels
    }

    /**
     * 下载速度文本
     */
    fun getTextSpeed(speed: Long): String {
        var text = ""
        if (speed >= 0 && speed < 1024) {
            text = "$speed KB/s"
        } else if (speed >= 1024 && speed < 1024 * 1024) {
            text = java.lang.Long.toString(speed / 1024) + " KB/s"
        } else if (speed >= 1024 * 1024 && speed < 1024 * 1024 * 1024) {
            text = java.lang.Long.toString(speed / (1024 * 1024)) + " MB/s"
        }
        return text
    }

    private fun getActivityContext(context: Context?): Activity? {
        if (context == null) return null else if (context is Activity) return context else if (context is TintContextWrapper) return scanForActivity(
            context.baseContext
        ) else if (context is ContextWrapper) return scanForActivity(
            context.baseContext
        )
        return null
    }


    fun dp2px(context: Context , vararg dps:Float):IntArray {
        val scale = context.resources.displayMetrics.density
        val result = IntArray(dps.size)
        var i =0
        dps.forEach {
            result[i]=(scale*it+0.5f).toInt()
            i++
        }
        return result;
    }
}