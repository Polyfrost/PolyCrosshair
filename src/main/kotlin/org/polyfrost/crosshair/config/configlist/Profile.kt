package org.polyfrost.crosshair.config.configlist

import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.OneColor
import java.util.*

class Profile {
    private var imgBase64: String =
        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" // 225 zeros | todo: get some preset crosshairs

    @Exclude
    var image: BitSet = try {
        BitSet.valueOf(Base64.getDecoder().decode(imgBase64))

    } catch (e: Exception) {
        BitSet(225)
    }
        set(value) {
            field = value
            imgBase64 = Base64.getEncoder().encodeToString(value.toByteArray())
        }

    @Text(name = "Profile Name", size = 2)
    val name = "Profile "

    @Color(name = "Crosshair Colour")
    val mainColor = OneColor(0xFFFFFF)

    @Switch(name = "Invert Color")
    val invertColor = false

    @Slider(name = "Rotation", min = 0f, max = 360f)
    val rotation = 0f

    @Slider(name = "Scale", min = 0f, max = 360f)
    val scale = 0f

    @Slider(name = "Offset (X)", min = -500f, max = 500f)
    val offsetX = 0f

    @Slider(name = "Offset (Y)", min = -500f, max = 500f)
    val offsetY = 0f
}