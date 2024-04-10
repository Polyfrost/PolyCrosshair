@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.*
import cc.polyfrost.oneconfig.config.data.*
import cc.polyfrost.oneconfig.config.elements.*
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.utils.Utils
import java.lang.reflect.Field

object ModConfig : Config(Mod(PolyCrosshair.NAME, ModType.HUD), "${PolyCrosshair.MODID}/config.json") {

    @Exclude
    var mode = false

    @Exclude
    var drawer = HashMap<Int, Int>()

    var currentCrosshair = ""

    @CustomOption
    var crosshairs = ArrayList<String>()

    var penColor = OneColor(-1)

    @Dropdown(
        name = "Mirror",
        options = ["Off", "Horizontal", "Vertical", "Quadrant"]
    )
    var mirror = 0

    @Slider(
        name = "Canva Size",
        min = 15f, max = 32f
    )
    var canvaSize = 15
        get() = field.coerceIn(15, 32)

    @Switch(name = "Dynamic Color (Overlay)")
    var dynamicColor = false

    @Switch(name = "Invert Color")
    var invertColor = false

    @Slider(name = "Overlay Opacity", min = 0f, max = 100f)
    var dynamicOpacity = 100

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

    @Switch(name = "Show in GUIs")
    var showInGuis = true

    @Switch(name = "Show in Third Person")
    var showInThirdPerson = true

    @Switch(name = "Show in Spectator Mode")
    var showInSpectator = false

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
        val options = listOf("hostile", "passive", "player", "hostileColor", "passiveColor", "playerColor", "dynamicOpacity")
        for (i in options) {
            hideIf(i) { !dynamicColor }
        }
        addDependency(options[3], options[0])
        addDependency(options[4], options[1])
        addDependency(options[5], options[2])
        addListener("canvaSize") {
            for (i in drawer) {
                val pos = Utils.indexToPos(i.key)
                if (pos.x >= canvaSize || pos.y >= canvaSize) {
                    Drawer.pixels[i.key].isToggled = false
                }
            }
        }
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
