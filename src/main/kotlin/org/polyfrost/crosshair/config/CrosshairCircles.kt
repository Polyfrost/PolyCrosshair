package org.polyfrost.crosshair.config

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

sealed interface VectorShape {
    val thickness: Float
    val color: Int
    val minX: Float
    val minY: Float
    val maxX: Float
    val maxY: Float

    fun distanceTo(px: Float, py: Float): Float

    fun scaled(k: Float): VectorShape

    fun moved(offset: Float): VectorShape

    fun flippedX(edge: Float): VectorShape

    fun flippedY(edge: Float): VectorShape

    fun encode(): String
}

data class Circle(val x: Float, val y: Float, val radius: Float, override val thickness: Float, override val color: Int) : VectorShape {
    override val minX get() = x - radius
    override val minY get() = y - radius
    override val maxX get() = x + radius
    override val maxY get() = y + radius

    override fun distanceTo(px: Float, py: Float): Float {
        val dx = px - x
        val dy = py - y
        return abs(sqrt(dx * dx + dy * dy) - radius)
    }

    override fun scaled(k: Float) = Circle(x * k, y * k, radius * k, thickness * k, color)

    override fun moved(offset: Float) = copy(x = x + offset, y = y + offset)

    override fun flippedX(edge: Float) = copy(x = edge - x)

    override fun flippedY(edge: Float) = copy(y = edge - y)

    override fun encode() = "$x:$y:$radius:$thickness:${Integer.toHexString(color)}"
}

data class Line(val x1: Float, val y1: Float, val x2: Float, val y2: Float, override val thickness: Float, override val color: Int) : VectorShape {
    override val minX get() = min(x1, x2)
    override val minY get() = min(y1, y2)
    override val maxX get() = max(x1, x2)
    override val maxY get() = max(y1, y2)

    val length: Float get() = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))

    override fun distanceTo(px: Float, py: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val length = sqrt(dx * dx + dy * dy)
        if (length == 0f) return Float.POSITIVE_INFINITY
        val along = ((px - x1) * dx + (py - y1) * dy) / length
        val across = abs((px - x1) * dy - (py - y1) * dx) / length
        val overshoot = max(-along, along - length)
        // Overshoot is negative inside the line, so the sides set coverage along the middle. The ends become square caps that fade over one texel, the same as the sides.
        return max(across, thickness / 2f + overshoot)
    }

    override fun scaled(k: Float) = Line(x1 * k, y1 * k, x2 * k, y2 * k, thickness * k, color)

    override fun moved(offset: Float) = Line(x1 + offset, y1 + offset, x2 + offset, y2 + offset, thickness, color)

    override fun flippedX(edge: Float) = copy(x1 = edge - x1, x2 = edge - x2)

    override fun flippedY(edge: Float) = copy(y1 = edge - y1, y2 = edge - y2)

    override fun encode() = "$x1:$y1:$x2:$y2:$thickness:${Integer.toHexString(color)}"
}

class CrosshairCircles(val size: Int, val list: MutableList<VectorShape> = mutableListOf()) {

    fun isEmpty(): Boolean = list.isEmpty()

    fun copy(): CrosshairCircles = CrosshairCircles(size, list.toMutableList())

    fun resized(newSize: Int, stretch: Boolean): CrosshairCircles {
        if (newSize == size) return copy()
        val moved = if (stretch) {
            val k = newSize.toFloat() / size
            list.map { it.scaled(k) }
        } else {
            val offset = ((newSize - size) / 2f).roundToInt().toFloat()
            list.map { it.moved(offset) }
        }
        return CrosshairCircles(newSize, moved.toMutableList())
    }

    fun encode(): String =
        if (list.isEmpty()) "" else "$size;" + list.joinToString(";") { it.encode() }

    fun paint(target: CrosshairPixels, scale: Int, override: Int? = null) {
        for (shape in list) {
            val color = override ?: shape.color
            forEachTexel(shape, scale, 0f) { i, coverage -> target.argb[i] = over(target.argb[i], color, coverage) }
        }
    }

    fun coverage(scale: Int, grow: Float): FloatArray {
        val out = FloatArray(size * scale * size * scale)
        for (shape in list) {
            forEachTexel(shape, scale, grow) { i, coverage -> if (coverage > out[i]) out[i] = coverage }
        }
        return out
    }

    private inline fun forEachTexel(shape: VectorShape, scale: Int, grow: Float, block: (Int, Float) -> Unit) {
        val hi = size * scale
        val half = (shape.thickness / 2f + grow) * scale
        if (half <= 0f) return
        val reach = half + 1f
        val x0 = floor(shape.minX * scale - reach).toInt().coerceAtLeast(0)
        val x1 = ceil(shape.maxX * scale + reach).toInt().coerceAtMost(hi - 1)
        val y0 = floor(shape.minY * scale - reach).toInt().coerceAtLeast(0)
        val y1 = ceil(shape.maxY * scale + reach).toInt().coerceAtMost(hi - 1)
        val peak = min(1f, half * 2f)
        for (y in y0..y1) {
            val py = (y + 0.5f) / scale
            for (x in x0..x1) {
                val distance = shape.distanceTo((x + 0.5f) / scale, py) * scale
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
                    when (f.size) {
                        5 -> Circle(f[0].toFloat(), f[1].toFloat(), f[2].toFloat(), f[3].toFloat(), f[4].toLong(16).toInt())
                        6 -> Line(f[0].toFloat(), f[1].toFloat(), f[2].toFloat(), f[3].toFloat(), f[4].toFloat(), f[5].toLong(16).toInt())
                        else -> throw IllegalArgumentException(part)
                    }
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
