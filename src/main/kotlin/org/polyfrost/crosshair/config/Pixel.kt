@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.gui.elements.BasicElement
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette

class Pixel(val index: Int) : BasicElement(16, 16, ColorPalette.PRIMARY, true, 0f) {

    var color = -1
        set(value) {
            if (value shr 24 == 0) isToggled = false
            ModConfig.crosshair[index]?.color = value
            field = value
        }

    var lastToggled = false

    override fun draw(vg: Long, x: Float, y: Float, inputHandler: InputHandler) {
        super.draw(vg, x + index % 15 * 17f, y + index / 15 * 17f, inputHandler)
    }

    override fun update(x: Float, y: Float, inputHandler: InputHandler) {
        hovered = inputHandler.isAreaHovered(x - hitBoxX, y - hitBoxY, (width + hitBoxX).toFloat(), (height + hitBoxY).toFloat())
        if (hovered) {
            if (inputHandler.isMouseDown) {
                isToggled = true
                color = ModConfig.penColor.rgb
            }
            if (inputHandler.isMouseDown(1)) isToggled = false
        }
        if (lastToggled != isToggled) {
            lastToggled = isToggled
            if (isToggled) {
                ModConfig.crosshair[index] = PixelInfo(color)
            } else {
                ModConfig.crosshair.remove(index)
            }
        }
        currentColor = if (isToggled) color else if (index % 2 == 0) ColorPalette.SECONDARY.normalColor else ColorPalette.SECONDARY.hoveredColor
    }



}