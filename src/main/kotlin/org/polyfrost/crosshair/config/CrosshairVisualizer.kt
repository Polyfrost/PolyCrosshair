package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.polyui.animate.Animations
import org.polyfrost.polyui.color.PolyColor
import org.polyfrost.polyui.color.argb
import org.polyfrost.polyui.color.mutable
import org.polyfrost.polyui.color.rgba
import org.polyfrost.polyui.component.Drawable
import org.polyfrost.polyui.component.extensions.onClick
import org.polyfrost.polyui.component.extensions.setPalette
import org.polyfrost.polyui.component.extensions.withBoarder
import org.polyfrost.polyui.component.impl.Block
import org.polyfrost.polyui.component.impl.Button
import org.polyfrost.polyui.component.impl.ColorPicker
import org.polyfrost.polyui.component.impl.Group
import org.polyfrost.polyui.operations.Recolor
import org.polyfrost.polyui.utils.ref
import kotlin.math.abs
import kotlin.math.max

class CrosshairVisualizer : Visualizer {

    override fun visualize(prop: Property<*>): Drawable {
        val crosshairs = prop.getAs<List<CrosshairEntry>>()
        val size = prop.getMetadata<Int>("size") ?: 15

        val pickedColor = rgba(255, 255, 255).mutable().ref()
        val pixels = mutableListOf<CrosshairPixel>()

        fun List<CrosshairPixel>.getAt(x: Int, y: Int): CrosshairPixel {
            return this[x + y * size]
        }

        for (index in 0 until size * size) {
            pixels.add(CrosshairPixel(index, size).onClick { event ->
                val state = event.button == 0
                val newPixelValue = if (state) pickedColor.element else CrosshairPixel.indexToColor(index, size)
                set(newPixelValue, state)

                val mirrorMode = PolyCrosshairConfig.MirrorMode.values()[PolyCrosshairConfig.mirror]
                if (mirrorMode != PolyCrosshairConfig.MirrorMode.OFF) { // NOTE: We do not need to run any mathematical calculations unnecessarily if we are not mirroring.
                    val canvasCenter = (size + 1) / 2f - 1
                    val disX = abs(index % size - canvasCenter)
                    val disY = abs(index / size - canvasCenter)

                    when (mirrorMode) {
                        PolyCrosshairConfig.MirrorMode.HORIZONTAL -> {
                            pixels.getAt(size - disX.toInt() - 1, index / size).set(newPixelValue, state)
                        }

                        PolyCrosshairConfig.MirrorMode.VERTICAL -> {
                            pixels.getAt(index % size, size - disY.toInt() - 1).set(newPixelValue, state)
                        }

                        PolyCrosshairConfig.MirrorMode.QUADRANT -> {
                            if (size % 2 == 1 && (disX.toInt() == 0 || disY.toInt() == 0)) {
                                val dis = max(disX, disY).toInt()
                                pixels.getAt(size - dis - 1, canvasCenter.toInt()).set(newPixelValue, state)
                                pixels.getAt(dis, canvasCenter.toInt()).set(newPixelValue, state)
                                pixels.getAt(canvasCenter.toInt(), size - dis - 1).set(newPixelValue, state)
                                pixels.getAt(canvasCenter.toInt(), dis).set(newPixelValue, state)
                            } else {
                                pixels.getAt(size - disX.toInt() - 1, index / size).set(newPixelValue, state)
                                pixels.getAt(index % size, size - disY.toInt() - 1).set(newPixelValue, state)
                                pixels.getAt(size - disX.toInt() - 1, size - disY.toInt() - 1).set(newPixelValue, state)
                            }
                        }

                        PolyCrosshairConfig.MirrorMode.OFF -> {
                            throw IllegalStateException("What the heckers?! This should never happen!")
                        }
                    }
                }

                true
            })
        }

        return Group(
            Block(
                children = pixels.toTypedArray(),
            ),

            Group(
                Group(
                    Button(text = "Export"),
                    Button(text = "Import")
                ),

                Group(
                    Block(color = pickedColor.element)
                        .withBoarder(3f, color = { page.border20 })
                        .onClick { ColorPicker(pickedColor, null, null, polyUI); true },
                    Button(text = "Reset").setPalette { state.danger },
                    Button(text = "Save").setPalette { onBrand.fg }
                )
            )
        )
    }

}

private class CrosshairPixel(
    private val index: Int,
    private val canvasSize: Int
) : Block(
    color = indexToColor(index, canvasSize)
) {

    companion object {

        fun indexToColor(index: Int, size: Int): PolyColor {
            return argb(if (size % 2 == 1 && index % size == size / 2 && index % size == index / size) {
                0x703A3AFF
            } else if ((index % size + index / size) % 2 == 0) {
                0x333333FF
            } else {
                0x444444FF
            })
        }

    }

    private var state = false

    fun set(color: PolyColor, state: Boolean) {
        Recolor(
            drawable = this,
            toColor = color,
            animation = Animations.Default.create(0L), // Instant
        ).add()

        this.state = if (color.a == 0) false else state
    }

}