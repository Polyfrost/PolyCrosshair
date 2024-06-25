package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.annotations.Button
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.utils.dsl.runAsync
import org.polyfrost.crosshair.utils.Utils

class CrosshairEntry(
    var img: String = "iVBORw0KGgoAAAANSUhEUgAAAA8AAAAPCAYAAAA71pVKAAAAGUlEQVR42mNgGH7gPxAMd83/iQCjATYkAQAXJEO9Ljp2dQAAAABJRU5ErkJggg\u003d\u003d",
) {

    @Slider(name = "Scale %", min = 0f, max = 200f)
    var scale = 100

    @Slider(name = "Rotation", min = -180f, max = 180f)
    var rotation = 0

    @Slider(name = "X Offset", min = -1920f, max = 1920f)
    var offsetX = 0

    @Slider(name = "Y Offset", min = -1080f, max = 1080f)
    var offsetY = 0

    @Button(name = "Transform", text = "Reset", size = 2)
    var transformReset = Runnable {
        runAsync {
            val img = ModConfig.newCurrentCrosshair.img
            ModConfig.newCurrentCrosshair.loadFrom(CrosshairEntry())
            ModConfig.newCurrentCrosshair.img = img
            Utils.save(Drawer.saveFromDrawer(false))
        }
    }

    fun loadFrom(entry: CrosshairEntry) {
        val newFields = ConfigUtils.getClassFields(entry::class.java)
        val fields = ConfigUtils.getClassFields(this::class.java)
        for (i in 0..<fields.size) {
            fields[i].set(this, ConfigUtils.getField(newFields[i], entry))
        }
    }
}