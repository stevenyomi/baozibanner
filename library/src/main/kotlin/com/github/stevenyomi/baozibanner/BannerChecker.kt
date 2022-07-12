package com.github.stevenyomi.baozibanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlin.math.abs

object BannerChecker {
    private const val w = BANNER_WIDTH
    private const val h = BANNER_HEIGHT
    private const val size = w * h
    private const val threshold = w * h * 3 // 1 per pixel per channel
    private val bannerBuffer by lazy {
        val buffer = Base64.decode(BANNER_BASE64, Base64.DEFAULT)
        val banner = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
        val pixels = IntArray(size)
        banner.getPixels(pixels, 0, w, 0, 0, w, h)
        pixels
    }

    internal fun check(image: Bitmap, level: Int): Pair<Int, Int> {
        val imgW = image.width
        val imgH = image.height
        if (imgW < w || imgH < h) return Pair(0, 0)
        if ((imgW - w) % 2 != 0) return Pair(0, 0)
        val pad = (imgW - w) / 2
        val isNormal = level == BaoziBanner.NORMAL
        val bannerBuf = bannerBuffer
        val buf = IntArray(size)

        var top = 0
        while (top + h <= imgH) {
            image.getPixels(buf, 0, w, pad, top, w, h)
            if (isIdentical(bannerBuf, buf)) top += h else break
            if (isNormal) break
        }
        if (isNormal && top > 0) return Pair(top, 0)

        var bottom = 0
        while (top + bottom + h <= imgH) {
            image.getPixels(buf, 0, w, pad, imgH - h - bottom, w, h)
            if (isIdentical(bannerBuf, buf)) bottom += h else break
            if (isNormal) break
        }

        return Pair(top, bottom)
    }

    private fun isIdentical(a: IntArray, b: IntArray): Boolean {
        var diff = 0
        for (i in 0 until size) {
            val pixel0 = a[i]
            val pixel1 = b[i]
            diff += abs((pixel0 and 0xFF) - (pixel1 and 0xFF))
            diff += abs((pixel0 shr 8 and 0xFF) - (pixel1 shr 8 and 0xFF))
            diff += abs((pixel0 shr 16 and 0xFF) - (pixel1 shr 16 and 0xFF))
            if (diff > threshold) return false
        }
        return true
    }
}
