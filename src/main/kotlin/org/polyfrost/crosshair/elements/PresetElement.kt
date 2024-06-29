@file:Suppress("UnstableAPIUsage")

package org.polyfrost.crosshair.elements

import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.gui.elements.BasicElement
import cc.polyfrost.oneconfig.platform.Platform
import cc.polyfrost.oneconfig.renderer.asset.AssetHelper
import cc.polyfrost.oneconfig.renderer.asset.Image
import cc.polyfrost.oneconfig.renderer.asset.SVG
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.nanoVGHelper
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.config.CrosshairEntry
import org.polyfrost.crosshair.config.Drawer
import org.polyfrost.crosshair.utils.copy
import org.polyfrost.crosshair.utils.export
import org.polyfrost.crosshair.utils.toBufferedImage
import java.io.File
import java.util.*

private val remove = SVG("/assets/polycrosshair/trashcan.svg")
private val copy = SVG("/assets/polycrosshair/copy.svg")

class PresetElement(val crosshair: CrosshairEntry) : BasicElement(149, 149, ColorPalette.SECONDARY, true) {
    val removeButton = BasicButton(32, 32, remove, 2, ColorPalette.TERTIARY)
    val copyButton = BasicButton(32, 32, copy, 2, ColorPalette.TERTIARY)
    val bufferedImage = toBufferedImage(crosshair.img)
    val fileName = UUID.randomUUID().toString()
    val image = Image(export(bufferedImage, fileName), AssetHelper.DEFAULT_FLAGS or 32)

    init {
        removeButton.setClickAction {
            Drawer.removeQueue.add(crosshair)
        }
        copyButton.setClickAction {
            copy(bufferedImage)
        }
    }

    override fun update(x: Float, y: Float, inputHandler: InputHandler) {
        hovered = Drawer.inArea && inputHandler.isAreaHovered(x - hitBoxX, y - hitBoxY, (width + hitBoxX).toFloat(), (height + hitBoxY).toFloat())
        pressed = hovered && Platform.getMousePlatform().isButtonDown(0)
        clicked = inputHandler.isClicked(false) && hovered

        if (clicked) {
            toggled = !toggled
            onClick()
        }

        currentColor = if (hoverFx) colorAnimation.getColor(hovered, pressed)
        else colorAnimation.getColor(false, false)
    }

    override fun draw(vg: Long, x: Float, y: Float, inputHandler: InputHandler?) {
        super.draw(vg, x, y, inputHandler)
        val half = 135 / 2f
        nanoVGHelper.translate(vg, x + 7 + half, y + 7 + half)
        nanoVGHelper.rotate(vg, crosshair.rotation.toDouble())
        nanoVGHelper.drawImage(vg, image, -half, -half, 135f, 135f, -1)
        nanoVGHelper.rotate(vg, -crosshair.rotation.toDouble())
        nanoVGHelper.translate(vg, -(x + 7 + half), -(y + 7 + half))
        if (hovered) {
            copyButton.draw(vg, x + 117, y + 32, inputHandler)
            removeButton.draw(vg, x + 117, y, inputHandler)
        }
    }

    fun onRemove() {
        File(PolyCrosshair.path + fileName + ".png").delete()
        Drawer.elements.remove(crosshair)
    }

    override fun onClick() {
        if (copyButton.isHovered) return
        if (removeButton.isHovered) return
        Drawer.loadImage(bufferedImage, false, crosshair)
    }
}