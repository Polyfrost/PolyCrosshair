package org.polyfrost.crosshair.config

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class Circle(val x: Float, val y: Float, val radius: Float, val thickness: Float, val color: Int) {

    fun distanceToRing(px: Float, py: Float): Float {
        val dx = px - x
        val dy = py - y
        return abs(sqrt(dx * dx + dy * dy) - radius)
    }
}

class CrosshairCircles(val size: Int, val list: MutableList<Circle> = mutableListOf()) {

    fun isEmpty(): Boolean = list.isEmpty()

    fun copy(): CrosshairCircles = CrosshairCircles(size, list.toMutableList())

    fun resized(newSize: Int, stretch: Boolean): CrosshairCircles {
        if (newSize == size) return copy()
        val moved = if (stretch) {
            val k = newSize.toFloat() / size
            list.map { Circle(it.x * k, it.y * k, it.radius * k, it.thickness * k, it.color) }
        } else {
            val offset = ((newSize - size) / 2f).roundToInt()
            list.map { it.copy(x = it.x + offset, y = it.y + offset) }
        }
        return CrosshairCircles(newSize, moved.toMutableList())
    }

    fun encode(): String =
        if (list.isEmpty()) "" else "$size;" + list.joinToString(";") {
            "${it.x}:${it.y}:${it.radius}:${it.thickness}:${Integer.toHexString(it.color)}"
        }

    fun paint(target: CrosshairPixels, scale: Int, override: Int? = null) {
        for (circle in list) {
            val color = override ?: circle.color
            forEachTexel(circle, scale, 0f) { i, coverage -> target.argb[i] = over(target.argb[i], color, coverage) }
        }
    }

    fun coverage(scale: Int, grow: Float): FloatArray {
        val out = FloatArray(size * scale * size * scale)
        for (circle in list) {
            forEachTexel(circle, scale, grow) { i, coverage -> if (coverage > out[i]) out[i] = coverage }
        }
        return out
    }

    private inline fun forEachTexel(circle: Circle, scale: Int, grow: Float, block: (Int, Float) -> Unit) {
        val hi = size * scale
        val half = (circle.thickness / 2f + grow) * scale
        if (half <= 0f) return
        val cx = circle.x * scale
        val cy = circle.y * scale
        val radius = circle.radius * scale
        val reach = radius + half + 1f
        val x0 = floor(cx - reach).toInt().coerceAtLeast(0)
        val x1 = ceil(cx + reach).toInt().coerceAtMost(hi - 1)
        val y0 = floor(cy - reach).toInt().coerceAtLeast(0)
        val y1 = ceil(cy + reach).toInt().coerceAtMost(hi - 1)
        val peak = min(1f, half * 2f)
        for (y in y0..y1) {
            val dy = y + 0.5f - cy
            for (x in x0..x1) {
                val dx = x + 0.5f - cx
                val distance = abs(sqrt(dx * dx + dy * dy) - radius)
                val coverage = (half - distance + 0.5f).coerceIn(0f, peak)
                if (coverage > 0f) block(y * hi + x, coverage)
            }
        }
    }

    companion object {

        fun decode(encoded: String): CrosshairCircles? {
            if (encoded.isEmpty()) return null
            return try {
                val parts = encoded.split(";")
                val size = parts[0].toInt()
                val list = parts.drop(1).mapTo(mutableListOf()) { part ->
                    val f = part.split(":")
                    Circle(f[0].toFloat(), f[1].toFloat(), f[2].toFloat(), f[3].toFloat(), f[4].toLong(16).toInt())
                }
                CrosshairCircles(size, list)
            } catch (_: Exception) {
                null
            }
        }

        fun load(encoded: String, size: Int, stretch: Boolean = false): CrosshairCircles {
            val parsed = decode(encoded) ?: return CrosshairCircles(size)
            return if (parsed.size == size) parsed else parsed.resized(size, stretch)
        }

        private fun over(dst: Int, src: Int, coverage: Float): Int {
            val sa = (src ushr 24) / 255f * coverage
            if (sa <= 0f) return dst
            val da = (dst ushr 24) / 255f * (1f - sa)
            val oa = sa + da
            fun channel(shift: Int): Int {
                val s = (src ushr shift and 0xFF) * sa
                val d = (dst ushr shift and 0xFF) * da
                return ((s + d) / oa).roundToInt().coerceIn(0, 255)
            }
            val alpha = (oa * 255f).roundToInt().coerceIn(0, 255)
            return (alpha shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
        }
    }
}
