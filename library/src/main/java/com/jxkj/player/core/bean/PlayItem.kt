package com.jxkj.player.core.bean

/**
 *Desc:
 *Author:Zhu
 *Date:2022/8/9
 */
class PlayItem(var url: String) {
    var name: String? = null
        get() = if (field.isNullOrEmpty()) url2name(url) else field
    constructor(url: String, name: String? = null) : this(url) {
        this.name = name
    }

    companion object {
        fun url2name(url: String): String {
            val temp = url.substringBeforeLast('.')
            if (temp.isEmpty())return ""
            return temp.substringAfterLast('/')
        }
    }

    override fun toString(): String {
        return name?:""
    }
}