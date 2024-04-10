package org.polyfrost.crosshair.elements

import cc.polyfrost.oneconfig.gui.animations.*
import cc.polyfrost.oneconfig.gui.elements.BasicElement
import cc.polyfrost.oneconfig.renderer.NanoVGHelper
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import org.polyfrost.crosshair.config.ModConfig
import java.awt.Color

class DualOption(val left: String, val right: String): BasicElement(254, 32, ColorPalette.PRIMARY, true) {
    private var posAnimation: Animation = DummyAnimation(if (ModConfig.mode) 129f else 2f)


    override fun draw(vg: Long, x: Float, y: Float, inputHandler: InputHandler) {
        val nanoVGHelper = NanoVGHelper.INSTANCE
        var toggled = ModConfig.mode

        val hoveredLeft = inputHandler.isAreaHovered(x, y, 127f, 32f)
        val hoveredRight = inputHandler.isAreaHovered(x + 127, y, 127f, 32f)
        nanoVGHelper.drawRoundedRect(vg, x, y, 254f, 32f, Color(42, 44, 48, 255).rgb, 12f)
        nanoVGHelper.drawRoundedRect(vg, x + posAnimation.get(), y + 2, 123f, 28f, Color(20, 82, 204, 255).rgb, 10f)
        if (!hoveredLeft) nanoVGHelper.setAlpha(vg, 0.8f)
        nanoVGHelper.drawText(vg, left, x + (127 - nanoVGHelper.getTextWidth(vg, left, 12f, Fonts.MEDIUM)) / 2f, y + 17, -1, 12f, Fonts.MEDIUM)
        nanoVGHelper.setAlpha(vg, 1f)
        if (!hoveredRight) nanoVGHelper.setAlpha(vg, 0.8f)
        nanoVGHelper.drawText(vg, right, x + 127 + (127 - nanoVGHelper.getTextWidth(vg, right, 12f, Fonts.MEDIUM)) / 2f, y + 17, -1, 12f, Fonts.MEDIUM)
        nanoVGHelper.setAlpha(vg, 1f)
        if ((hoveredLeft && toggled || hoveredRight && !toggled) && inputHandler.isClicked) {
            toggled = !toggled
            ModConfig.mode = toggled
        }
        if (toggled == posAnimation.isReversed) posAnimation = EaseOutExpo(300, 2f, 129f, !toggled)
    }
}