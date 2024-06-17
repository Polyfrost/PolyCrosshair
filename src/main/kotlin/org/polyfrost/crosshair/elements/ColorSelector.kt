@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.elements

import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.gui.elements.BasicElement
import cc.polyfrost.oneconfig.gui.elements.ColorSelector
import cc.polyfrost.oneconfig.internal.assets.Images
import cc.polyfrost.oneconfig.renderer.NanoVGHelper
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.renderTick
import org.polyfrost.crosshair.config.ModConfig
import java.awt.Color

class ColorSelector : BasicElement(64, 32, false) {
    private val element = BasicElement(64, 32, false)
    private var colorSelector: ColorSelector? = null
    private var open = false

    override fun draw(vg: Long, x: Float, y: Float, inputHandler: InputHandler) {
        if (OneConfigGui.INSTANCE == null) return
        val nanoVGHelper = NanoVGHelper.INSTANCE

        var color = ModConfig.penColor

        element.update(x, y, inputHandler)
        nanoVGHelper.drawHollowRoundRect(vg, x, y - 1, 64f, 32f, Color(73, 79, 92, 255).rgb, 12f, 2f)
        nanoVGHelper.drawRoundImage(vg, Images.ALPHA_GRID.filePath, x + 5, y + 4, 56f, 24f, 8f, javaClass)
        nanoVGHelper.drawRoundedRect(vg, x + 5, y + 4, 56f, 24f, color.rgb, 8f)
        if (element.isClicked && !open) {
            val finalColor = color
            renderTick(1) {
                open = true
                colorSelector =
                    ColorSelector(finalColor, inputHandler.mouseX(), inputHandler.mouseY(), true, inputHandler)
                OneConfigGui.INSTANCE.initColorSelector(colorSelector)
            }
        }
        if (OneConfigGui.INSTANCE.currentColorSelector !== colorSelector) open = false
        else if (open) color = (OneConfigGui.INSTANCE.color)
        ModConfig.penColor = color
    }

}