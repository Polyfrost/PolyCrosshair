package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.gui.elements.BasicElement
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette

@Suppress("UnstableAPIUsage")
class Pixel(val index: Int) : BasicElement(16, 16, ColorPalette.PRIMARY, true, 0f) {
    var state = false

    override fun draw(vg: Long, x: Float, y: Float, inputHandler: InputHandler) {
        val xPos = index % 15
        val yPos = index / 15
        super.draw(vg, x + xPos * 17f, y + yPos * 17f, inputHandler)
    }

    override fun update(x: Float, y: Float, inputHandler: InputHandler) {
        super.update(x, y, inputHandler)
        if (!hovered) return

        state = when {
            inputHandler.isMouseDown -> true
            inputHandler.isMouseDown(1) -> false
            else -> return
        }
        setColorPalette(if (state) ColorPalette.PRIMARY else ColorPalette.SECONDARY)
    }
}