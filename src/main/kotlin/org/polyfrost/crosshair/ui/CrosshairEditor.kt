package org.polyfrost.crosshair.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.onClick
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.crosshair.config.CrosshairData
import org.polyfrost.crosshair.config.CrosshairEntry
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.utils.copyToClipboard
import org.polyfrost.crosshair.utils.imageFromClipboard
import org.polyfrost.crosshair.utils.notify
import org.polyfrost.crosshair.utils.toBase64
import org.polyfrost.crosshair.utils.toBufferedImage
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.Base64
import kotlin.math.abs
import kotlin.math.max

@Composable
fun CrosshairEditor(prop: Property<*>) {
    @Suppress("UNCHECKED_CAST")
    val p = prop as Property<CrosshairData>
    val data = remember(prop) { p.get() ?: CrosshairData() }

    val pixels = remember(prop) { mutableStateMapOf<Int, Int>().apply { load(this, data.current.img) } }
    var galleryVersion by remember { mutableStateOf(0) }

    fun persist() {
        data.current.img = toBase64(pixelsToImage(pixels, ModConfig.canvas))
        p.set(data)
        ModConfig.save()
    }

    fun loadEntry(entry: CrosshairEntry) {
        ModConfig.scale = entry.scale.toFloat()
        ModConfig.rotation = entry.rotation.toFloat()
        ModConfig.offsetX = entry.offsetX.toFloat()
        ModConfig.offsetY = entry.offsetY.toFloat()
        ModConfig.centered = entry.centered
        pixels.clear(); load(pixels, entry.img)
        data.current.copyFrom(entry)
        persist()
    }

    val theme = LocalTheme.current

    Column(Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier
                .size(168.dp)
                .border(1.dp, theme.borderColor)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val n = ModConfig.canvas
                        val cell = size.width / n.toFloat()
                        fun paintAt(pos: Offset, erase: Boolean) {
                            val x = (pos.x / cell).toInt()
                            val y = (pos.y / cell).toInt()
                            if (x in 0 until n && y in 0 until n) {
                                paint(pixels, x, y, if (erase) null else ModConfig.penColor.argb)
                            }
                        }
                        val down = awaitFirstDown()
                        paintAt(down.position, false)
                        down.consume()
                        do {
                            val event = awaitPointerEvent()
                            val secondary = event.buttons.isSecondaryPressed
                            event.changes.forEach {
                                if (it.pressed) { paintAt(it.position, secondary); it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                        persist()
                    }
                }
        ) {
            val light = theme.chipBackground
            val dark = theme.componentBackground
            Canvas(Modifier.size(168.dp)) {
                val n = ModConfig.canvas
                val cell = this.size.width / n
                for (y in 0 until n) for (x in 0 until n) {
                    drawRect(if ((x + y) % 2 == 0) dark else light, Offset(x * cell, y * cell), Size(cell, cell))
                    pixels[y * 32 + x]?.let {
                        if (it ushr 24 != 0) drawRect(Color(it), Offset(x * cell, y * cell), Size(cell, cell))
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EditorButton("Save") {
                if (pixels.isEmpty()) notify("Crosshair can't be empty.") else {
                    val entry = CrosshairEntry()
                    entry.copyFrom(data.current)
                    entry.img = toBase64(pixelsToImage(pixels, ModConfig.canvas))
                    if (data.presets.none { it.img == entry.img }) {
                        data.presets.add(entry); galleryVersion++
                    }
                    persist()
                }
            }
            EditorButton("Clear") { pixels.clear(); persist() }
            EditorButton("Import") {
                val img = imageFromClipboard()
                if (img == null) notify("No image found in clipboard.")
                else if (img.width != img.height || img.width !in 15..32)
                    notify("Image must be square, 15x15 to 32x32 (was ${img.width}x${img.height}).")
                else {
                    ModConfig.canvaSize = img.height.toFloat()
                    pixels.clear(); loadImage(pixels, img); persist()
                }
            }
            EditorButton("Export") { copyToClipboard(pixelsToImage(pixels, ModConfig.canvas)) }
        }

        galleryVersion
        if (data.presets.isNotEmpty()) {
            LazyRow(Modifier.height(52.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(data.presets) { entry ->
                    Box(
                        Modifier.size(48.dp)
                            .clip(theme.backgroundShape)
                            .background(theme.componentBackground)
                            .border(1.dp, theme.borderColor, theme.backgroundShape)
                            .clickable { loadEntry(entry) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        thumbnail(entry.img)?.let {
                            Image(it, contentDescription = null, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorButton(text: String, onClick: () -> Unit) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bgColor by animateColorAsState(if (isHovered) Accent.copy(alpha = 0.75f) else Accent)
    Box(
        Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .background(bgColor, theme.buttonShape)
            .onClick(interactionSource) { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = theme.accentTextColor, fontSize = 13.sp)
    }
}

private fun idx(x: Int, y: Int) = y * 32 + x

private fun put(pixels: MutableMap<Int, Int>, x: Int, y: Int, color: Int?) {
    if (x < 0 || y < 0 || x >= 32 || y >= 32) return
    if (color == null || color ushr 24 == 0) pixels.remove(idx(x, y)) else pixels[idx(x, y)] = color
}

private fun paint(pixels: MutableMap<Int, Int>, x: Int, y: Int, color: Int?) {
    put(pixels, x, y, color)
    val n = ModConfig.canvas
    when (ModConfig.mirror) {
        0 -> return
        else -> {
            val center = (n + 1) / 2f - 1
            val disX = center - x
            val disY = center - y
            val mode = ModConfig.mirror
            if (mode == 3 && n % 2 == 1 && (disX.toInt() == 0 || disY.toInt() == 0)) {
                val distance = max(abs(disX), abs(disY)).toInt()
                val c = center.toInt()
                put(pixels, c + distance, c, color); put(pixels, c - distance, c, color)
                put(pixels, c, c + distance, color); put(pixels, c, c - distance, color)
            } else {
                if (mode == 1 || mode == 3) put(pixels, (center + disX).toInt(), y, color)
                if (mode == 2 || mode == 3) put(pixels, x, (center + disY).toInt(), color)
                if (mode == 3) put(pixels, (center + disX).toInt(), (center + disY).toInt(), color)
            }
        }
    }
}

private fun load(pixels: MutableMap<Int, Int>, base64: String) {
    val img = toBufferedImage(base64) ?: return
    loadImage(pixels, img)
}

private fun loadImage(pixels: MutableMap<Int, Int>, img: BufferedImage) {
    for (y in 0 until img.height) for (x in 0 until img.width) {
        val c = img.getRGB(x, y)
        if (c ushr 24 != 0) pixels[idx(x, y)] = c
    }
}

private fun pixelsToImage(pixels: Map<Int, Int>, n: Int): BufferedImage {
    val img = BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB)
    for ((key, color) in pixels) {
        val x = key % 32; val y = key / 32
        if (x < n && y < n) img.setRGB(x, y, color)
    }
    return img
}

private fun thumbnail(base64: String) = try {
    BitmapPainter(loadImageBitmap(ByteArrayInputStream(Base64.getDecoder().decode(base64))))
} catch (_: Exception) {
    null
}
