package com.github.stevenyomi.baozibanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.ByteArrayOutputStream

class BaoziBanner(var level: Int) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val level = this.level
        if (level == DISABLED) return response
        val body = response.body ?: return response
        val contentType = body.contentType() ?: return response
        if (contentType.type != "image") return response
        val host = request.url.host
        if (host != "s1.baozimh.com" && host != "s2.baozimh.com") return response

        val content = body.bytes()
        val bitmap = BitmapFactory.decodeByteArray(content, 0, content.size)
        val (top, bottom) = BannerChecker.check(bitmap, level)
        return if (top == 0 && bottom == 0) {
            response.newBuilder().body(content.toResponseBody(contentType)).build()
        } else {
            val result = Bitmap.createBitmap(bitmap, 0, top, bitmap.width, bitmap.height - top - bottom)
            val output = ByteArrayOutputStream()
            result.compress(Bitmap.CompressFormat.JPEG, 90, output)
            val responseBody = output.toByteArray().toResponseBody(jpegMediaType)
            response.newBuilder().body(responseBody).build()
        }
    }

    companion object {
        const val DISABLED = 0
        const val NORMAL = 1
        const val EXTRA = 2

        const val VERSION_NAME = "1.0"
        const val PREF = "BAOZI_BANNER"
        const val PREF_TITLE = "移除包子漫画横幅 (v$VERSION_NAME)"
        const val PREF_SUMMARY = "已选择：%s\n" +
                "普通模式只能移除上端或下端的一处横幅，强力模式可以移除多个层叠的横幅。" +
                "修改后，已加载的图片需要清除缓存才能生效。\n" +
                "目前只对宽度不小于 800 像素的图片有效，并且要求横幅位置居中。" +
                "如果遇到任何无法移除的横幅，欢迎到 GitHub 上搜索 baozibanner 仓库并创建 Issue 提供对应的作品章节。"

        val PREF_ENTRIES = arrayOf("禁用", "普通模式", "强力模式")
        val PREF_VALUES = arrayOf("0", "1", "2")

        private val jpegMediaType = "image/jpeg".toMediaType()
    }
}
