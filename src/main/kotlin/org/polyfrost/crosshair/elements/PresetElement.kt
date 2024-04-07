@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.elements

import cc.polyfrost.oneconfig.gui.elements.*
import cc.polyfrost.oneconfig.renderer.asset.*
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.nanoVGHelper
import org.polyfrost.crosshair.config.Drawer
import java.io.File

private val remove = SVG("/assets/polycrosshair/minus.svg")

class PresetElement(val path: String) : BasicElement(149, 149, ColorPalette.SECONDARY, true) {
    val removeButton = BasicButton(32, 32, remove, 2, ColorPalette.PRIMARY_DESTRUCTIVE)

    init {
        removeButton.setClickAction {
            Drawer.removeQueue.add(this)
            File(path).delete()
        }
    }

    override fun draw(vg: Long, x: Float, y: Float, inputHandler: InputHandler?) {
        super.draw(vg, x, y, inputHandler)
        nanoVGHelper.drawImage(vg, Image(path, AssetHelper.DEFAULT_FLAGS or 32), x + 7, y + 7, 135f, 135f, -1)
        if (hovered) removeButton.draw(vg, x + 117, y, inputHandler)
    }

    override fun onClick() {
        if (removeButton.isHovered) return
        Drawer.loadFromFile(path, false)
    }
}