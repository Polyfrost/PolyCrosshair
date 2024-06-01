@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.gui.elements.BasicElement
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.nanoVGHelper
import org.polyfrost.crosshair.utils.Utils
import kotlin.math.abs
import kotlin.math.max

class Pixel(val index: Int) : BasicElement(16, 16, ColorPalette.PRIMARY, true, 0f) {
    var backgroundColor = 0
    var posX = 0
    var posY = 0

    var color = -1
        set(value) {
            if (value shr 24 == 0) isToggled = false
            ModConfig.drawer[index] = value
            field = value
        }

    var lastToggled = false

    override fun draw(vg: Long, x: Float, y: Float, inputHandler: InputHandler) {
        val size = (256 - (ModConfig.canvaSize - 1)) / ModConfig.canvaSize.toFloat()
        posX = index % 32
        posY = index / 32
        val x1 = x + posX * (size + 1)
        val y1 = y + posY * (size + 1)
        width = size.toInt()
        height = size.toInt()
        this.update(x1, y1, inputHandler)
        nanoVGHelper.drawRect(vg, x1, y1, size, size, currentColor)
    }

    override fun update(x: Float, y: Float, inputHandler: InputHandler) {
        val pos = Utils.indexToPos(index)
        val size = ModConfig.canvaSize
        backgroundColor = if (size % 2 == 1 && pos.x == size / 2 && pos.x == pos.y) {
            OneColor("703A3AFF").rgb
        } else if ((pos.x + pos.y) % 2 == 0) {
            ColorPalette.SECONDARY.normalColor
        } else {
            ColorPalette.SECONDARY.hoveredColor
        }
        hovered = inputHandler.isAreaHovered(x - hitBoxX, y - hitBoxY, (width + hitBoxX).toFloat(), (height + hitBoxY).toFloat())
        if (hovered && OneConfigGui.INSTANCE.currentColorSelector == null) {
            if (inputHandler.isMouseDown) {
                doMirror(true, ModConfig.penColor.rgb)
            }
            if (inputHandler.isMouseDown(1)) {
                doMirror(false, color)
            }
        }
        if (lastToggled != isToggled) {
            lastToggled = isToggled
            if (isToggled) {
                ModConfig.drawer[index] = color
            } else {
                ModConfig.drawer.remove(index)
            }
        }
        currentColor = if (isToggled) color else backgroundColor
    }

    fun doMirror(toggle: Boolean, color: Int) {
        val mode = ModConfig.mirror
        set(toggle, color)
        if (mode == 0) return
        val size = ModConfig.canvaSize

        val center = (size + 1) / 2f - 1
        val disX = center - posX
        val disY = center - posY
        if (mode == 3 && size % 2 == 1 && (disX.toInt() == 0 || disY.toInt() == 0)) {
            val distance = max(abs(disX), abs(disY)).toInt()
            val c = center.toInt()
            setPixel(c + distance, c, toggle, color)
            setPixel(c - distance, c, toggle, color)
            setPixel(c, c + distance, toggle, color)
            setPixel(c, c - distance, toggle, color)
        } else {
            if (mode == 1 || mode == 3) {
                setPixel((center + disX).toInt(), posY, toggle, color)
            }
            if (mode == 2 || mode == 3) {
                setPixel(posX, (center + disY).toInt(), toggle, color)
            }
            if (mode == 3) {
                setPixel((center + disX).toInt(), (center + disY).toInt(), toggle, color)
            }
        }
    }

    fun setPixel(x: Int, y: Int, toggle: Boolean, color: Int) {
        Drawer.pixels[Utils.posToIndex(x, y)].set(toggle, color)
    }

    fun set(toggle: Boolean, color: Int) {
        isToggled = toggle
        if (!toggle) return
        this.color = color
    }

}