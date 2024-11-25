package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.annotations.Button
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.utils.v1.dsl.runAsync

class CrosshairEntry(var base64: String) {

    companion object {

        const val DEFAULT = "iVBORw0KGgoAAAANSUhEUgAAAA8AAAAPCAYAAAA71pVKAAAAEUlEQVR42mNgGAWjYBQMIgAAA5MAAecADfkAAAAASUVORK5CYII\u003d"

    }

    @Slider(title = "Scale %", min = 0f, max = 200f)
    var scale = 100

    @Slider(title = "Rotation", min = -180f, max = 180f)
    var rotation = 0

    @Slider(title = "X Offset", min = -1920f, max = 1920f)
    var offsetX = 0

    @Slider(title = "Y Offset", min = -1080f, max = 1080f)
    var offsetY = 0

    @Switch(title = "Centered", description = "In vanilla Minecraft, the crosshair is not centered. Enable this option to center the crosshair.")
    var centered = false

    @Button(title = "Transform", text = "Reset")
    var transformReset = Runnable {
        runAsync {
            val base64 = PolyCrosshairConfig.currentCustomCrosshair.base64
            PolyCrosshairConfig.currentCustomCrosshair.loadFrom(CrosshairEntry(DEFAULT))
            PolyCrosshairConfig.currentCustomCrosshair.base64 = base64
//            save(Drawer.saveFromDrawer(false))
        }
    }

    fun loadFrom(entry: CrosshairEntry) {
//        val newFields = ConfigUtils.getClassFields(entry::class.java)
//        val fields = ConfigUtils.getClassFields(this::class.java)
//        for (i in 0..<fields.size) {
//            fields[i].set(this, ConfigUtils.getField(newFields[i], entry))
//        }
    }
}