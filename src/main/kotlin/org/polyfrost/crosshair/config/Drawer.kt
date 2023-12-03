package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.utils.InputHandler
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.config.configlist.Profile
import java.util.*

@Suppress("UnstableAPIUsage")
object Drawer : BasicOption(null, null, "", "", "", "", 1) {
    private var currentProfile: Profile? = null
    private val pixels: Array<Pixel> = Array(225) { Pixel(it) }

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        for (pixel in pixels) {
            pixel.draw(vg, x.toFloat(), y.toFloat(), inputHandler)
        }
    }

    fun load(profile: Profile) {
        currentProfile = profile
        val bitSet = profile.image
        for (pixel in pixels) {
            pixel.state = bitSet.get(pixel.index)
        }
    }

    override fun finishUpAndClose() {
        val profile = currentProfile ?: return

        val bitSet = BitSet(225)
        for (pixel in pixels) {
            bitSet.set(pixel.index, pixel.state)
        }
        profile.image = bitSet
        PolyCrosshair.updateTexture()
    }

    override fun getHeight() = 254
}