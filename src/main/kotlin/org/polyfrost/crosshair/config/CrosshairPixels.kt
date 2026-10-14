package org.polyfrost.crosshair.config

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.InflaterInputStream
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class CrosshairPixels(val size: Int, val argb: IntArray = IntArray(size * size)) {

    init {
        require(argb.size == size * size) { "expected ${size * size} pixels for a ${size}x$size grid, got ${argb.size}" }
    }

    operator fun get(x: Int, y: Int): Int = if (inBounds(x, y)) argb[y * size + x] else 0

    operator fun set(x: Int, y: Int, color: Int) {
        if (inBounds(x, y)) argb[y * size + x] = color
    }

    fun inBounds(x: Int, y: Int): Boolean = x in 0 until size && y in 0 until size

    fun isOpaque(x: Int, y: Int): Boolean = get(x, y) ushr 24 != 0

    fun isEmpty(): Boolean = argb.none { it ushr 24 != 0 }

    fun clear() = argb.fill(0)

    fun copy(): CrosshairPixels = CrosshairPixels(size, argb.copyOf())

    fun copyFrom(other: CrosshairPixels) {
        if (other.size == size) {
            other.argb.copyInto(argb)
        } else {
            other.resized(size, stretch = false).argb.copyInto(argb)
        }
    }

    fun resized(newSize: Int, stretch: Boolean): CrosshairPixels {
        if (newSize == size) return copy()
        val out = CrosshairPixels(newSize)
        if (stretch) {
            for (y in 0 until newSize) {
                val sy = min(size - 1, ((y + 0.5f) * size / newSize).toInt())
                for (x in 0 until newSize) {
                    val sx = min(size - 1, ((x + 0.5f) * size / newSize).toInt())
                    out[x, y] = this[sx, sy]
                }
            }
        } else {
            val offset = ((newSize - size) / 2f).roundToInt()
            for (y in 0 until size) {
                for (x in 0 until size) {
                    out[x + offset, y + offset] = this[x, y]
                }
            }
        }
        return out
    }

    fun toImage(): BufferedImage {
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        img.setRGB(0, 0, size, size, argb, 0, size)
        return img
    }

    fun toBase64(): String {
        val out = ByteArrayOutputStream()
        ImageIO.write(toImage(), "png", out)
        return Base64.getEncoder().encodeToString(out.toByteArray())
    }

    fun toPngBytes(): ByteArray {
        val out = ByteArrayOutputStream()
        ImageIO.write(toImage(), "png", out)
        return out.toByteArray()
    }

    fun silhouette(): CrosshairPixels {
        val out = CrosshairPixels(size)
        for (i in argb.indices) {
            if (argb[i] ushr 24 != 0) out.argb[i] = -1
        }
        return out
    }

    fun scaled(factor: Int): CrosshairPixels {
        if (factor == 1) return copy()
        val hi = size * factor
        val out = CrosshairPixels(hi)
        for (y in 0 until hi) {
            for (x in 0 until hi) out.argb[y * hi + x] = this[x / factor, y / factor]
        }
        return out
    }

    fun outline(thickness: Float, s: Int): CrosshairPixels? {
        val radius = thickness * s
        if (radius < 0.5f) return null

        val hi = size * s
        val shape = BooleanArray(hi * hi)
        val grown = BooleanArray(hi * hi)
        var any = false

        for (y in 0 until size) {
            for (x in 0 until size) {
                if (!isOpaque(x, y)) continue
                any = true
                val bx = x * s
                val by = y * s
                for (dy in 0 until s) {
                    val row = (by + dy) * hi + bx
                    for (dx in 0 until s) shape[row + dx] = true
                }
            }
        }
        if (!any) return null

        val reach = ceil(radius).toInt()
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (!isOpaque(x, y)) continue
                val bx = x * s
                val by = y * s
                for (dy in -reach until s + reach) {
                    val ty = by + dy
                    if (ty < 0 || ty >= hi) continue
                    val oy = when {
                        dy < 0 -> -dy
                        dy >= s -> dy - s + 1
                        else -> 0
                    }
                    for (dx in -reach until s + reach) {
                        val tx = bx + dx
                        if (tx < 0 || tx >= hi) continue
                        val ox = when {
                            dx < 0 -> -dx
                            dx >= s -> dx - s + 1
                            else -> 0
                        }
                        if (ox == 0 && oy == 0) {
                            grown[ty * hi + tx] = true
                        } else {
                            val dist2 = (ox * ox + oy * oy).toFloat()
                            if (dist2 <= radius * radius) grown[ty * hi + tx] = true
                        }
                    }
                }
            }
        }

        val out = CrosshairPixels(hi)
        var drew = false
        for (i in grown.indices) {
            if (grown[i] && !shape[i]) {
                out.argb[i] = -1
                drew = true
            }
        }
        return if (drew) out else null
    }

    companion object {
        fun fromBase64(base64: String): CrosshairPixels? = try {
            val bytes = Base64.getDecoder().decode(base64)
            ImageIO.read(ByteArrayInputStream(bytes))?.let(::fromImage)
        } catch (_: Exception) {
            null
        }

        fun fromLunar(code: String): CrosshairPixels? = runCatching {
            val (_, sizeText, data) = code.trim().split('-', limit = 3)
            val size = sizeText.toInt()
            if (size !in 1..GridSize.MAX) return null
            val raw = Base64.getDecoder().decode(data.takeWhile { it.isLetterOrDigit() || it == '+' || it == '/' })
            val bytes = runCatching { InflaterInputStream(raw.inputStream()).use { it.readBytes() } }.getOrDefault(raw)
            if (bytes.size * 8 < size * size) return null
            val out = CrosshairPixels(size)
            for (i in 0 until size * size) {
                if (bytes[i / 8].toInt() shr (i % 8) and 1 != 0) out.argb[i] = -1
            }
            out
        }.getOrNull()

        fun fromImage(img: BufferedImage): CrosshairPixels? {
            if (img.width <= 0 || img.width != img.height) return null
            val out = CrosshairPixels(img.width)
            img.getRGB(0, 0, img.width, img.height, out.argb, 0, img.width)
            return out
        }

        fun load(base64: String, size: Int, stretch: Boolean = false): CrosshairPixels {
            val parsed = fromBase64(base64) ?: return CrosshairPixels(size)
            return if (parsed.size == size) parsed else parsed.resized(size, stretch)
        }

        fun empty(size: Int): String = CrosshairPixels(size).toBase64()

        const val DEFAULT: String =
            "iVBORw0KGgoAAAANSUhEUgAAAA8AAAAPCAYAAAA71pVKAAAAGUlEQVR42mNgGH7gPxAMd83/iQCjATYkAQAXJEO9Ljp2dQAAAABJRU5ErkJggg=="

        const val ELYTRA: String =
            "iVBORw0KGgoAAAANSUhEUgAAAA8AAAAPCAYAAAA71pVKAAAAS0lEQVR4Xs2LQQ4AIAjD+P+nNVyIFpjxYtxt7Wb2TYYItxEOqhN7wA0sTLlcwJTLBUy5XMCUywVMuRZ0Z/YSXp09pwP5FpfdQLn3mcK+xzn5e4m4AAAAAElFTkSuQmCC"
    }
}
