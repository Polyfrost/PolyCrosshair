@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.elements

import cc.polyfrost.oneconfig.gui.elements.*
import cc.polyfrost.oneconfig.renderer.asset.*
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.nanoVGHelper
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.config.Drawer
import org.polyfrost.crosshair.utils.Utils
import java.io.File
import java.util.UUID

private val remove = SVG("/assets/polycrosshair/minus.svg")

class PresetElement(val base64: String) : BasicElement(149, 149, ColorPalette.SECONDARY, true) {
    val removeButton = BasicButton(32, 32, remove, 2, ColorPalette.PRIMARY_DESTRUCTIVE)
    val copyButton = BasicButton(32, 32, "", 2, ColorPalette.PRIMARY)
    val bufferedImage = Utils.toBufferedImage(base64)
    val fileName = UUID.randomUUID().toString()
    val image = Image(Utils.export(bufferedImage, fileName), AssetHelper.DEFAULT_FLAGS or 32)

    init {
        removeButton.setClickAction {
            Drawer.removeQueue.add(base64)
        }
        copyButton.setClickAction {
            Utils.copy(bufferedImage)
        }
    }

    override fun draw(vg: Long, x: Float, y: Float, inputHandler: InputHandler?) {
        super.draw(vg, x, y, inputHandler)
        nanoVGHelper.drawImage(vg, image, x + 7, y + 7, 135f, 135f, -1)
        if (hovered) {
            copyButton.draw(vg, x + 117, y + 32, inputHandler)
            removeButton.draw(vg, x + 117, y, inputHandler)
        }
    }

    fun onRemove() {
        File(PolyCrosshair.path + fileName + ".png").delete()
        Drawer.elements.remove(base64)
    }

    override fun onClick() {
        if (copyButton.isHovered) return
        if (removeButton.isHovered) return
        Drawer.loadImage(bufferedImage, false)
    }
}