@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.*
import cc.polyfrost.oneconfig.config.data.*
import cc.polyfrost.oneconfig.config.elements.*
import org.polyfrost.crosshair.PolyCrosshair
import java.lang.reflect.Field

object ModConfig : Config(Mod(PolyCrosshair.NAME, ModType.HUD), "${PolyCrosshair.MODID}/config.json") {

    @CustomOption
    var crosshair = HashMap<Int, PixelInfo>()

    var presets = ArrayList<String>() //base64

    @Exclude
    var penColor = OneColor(255, 255, 255, 255)

    @Switch(name = "Dynamic Color")
    var dynamicColor = false

    @Switch(name = "Invert Color")
    var invertColor = false

    @Switch(name = "Hostile")
    var hostile = false

    @Color(name = "Color")
    var hostileColor = OneColor(-1)

    @Switch(name = "Passive")
    var passive = false

    @Color(name = "Color")
    var passiveColor = OneColor(-1)

    @Switch(name = "Players")
    var player = false

    @Color(name = "Color")
    var playerColor = OneColor(-1)

    @Switch(name = "Show in F3 (Debug)")
    var showInDebug = false

    @Switch(name = "Show in Third Person")
    var showInThirdPerson = false

    @Switch(name = "Show in Spectator Mode")
    var showInSpectator = true

    @Slider(name = "Rotation", min = -180f, max = 180f)
    var rotation = 0

    @Slider(name = "Scale %", min = 0f, max = 200f)
    var scale = 100

    @Slider(name = "X Offset", min = -500f, max = 500f)
    var offsetX = 0

    @Slider(name = "Y Offset", min = -500f, max = 500f)
    var offsetY = 0

    init {
        initialize()
        for (i in 0..224) {
            Drawer.pixels[i].isToggled = false
            val crosshair = crosshair[i] ?: continue
            Drawer.pixels[i].isToggled = true
            Drawer.pixels[i].color = crosshair.color
        }
        val options = listOf("hostile", "passive", "player", "hostileColor", "passiveColor", "playerColor")
        for (i in options) {
            hideIf(i) { !dynamicColor }
        }
        addDependency(options[3], options[0])
        addDependency(options[4], options[1])
        addDependency(options[5], options[2])
    }

    override fun getCustomOption(
        field: Field,
        annotation: CustomOption,
        page: OptionPage,
        mod: Mod,
        migrate: Boolean,
    ): BasicOption? {
        ConfigUtils.getSubCategory(page, "General", "").options.add(Drawer)
        return null
    }

}
