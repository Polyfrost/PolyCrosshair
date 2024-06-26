@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.*
import cc.polyfrost.oneconfig.config.data.*
import cc.polyfrost.oneconfig.config.elements.*
import club.sk1er.patcher.config.OldPatcherConfig
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.utils.*
import java.lang.reflect.Field

object ModConfig : Config(Mod(PolyCrosshair.NAME, ModType.HUD, "/${PolyCrosshair.MODID}.svg"), "${PolyCrosshair.MODID}/config.json") {

    @Exclude
    var drawer = HashMap<Int, Int>()

    @DualOption(
        name = "Mode",
        left = "Vanilla",
        right = "Custom",
        size = 2
    )
    var mode = false

    @CustomOption
    var newCrosshairs = arrayListOf(CrosshairEntry())

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

    var newCurrentCrosshair = CrosshairEntry()

    var renderConfig = RenderConfig()

    init {
        initialize()
        this.generateOptionList(newCurrentCrosshair, mod.defaultPage, this.mod, false)
        this.generateOptionList(renderConfig, mod.defaultPage, this.mod, false)
        var options = listOf("hostile", "passive", "player", "hostileColor", "passiveColor", "playerColor", "dynamicOpacity")
        for (i in options) {
            hideIf(i) { !renderConfig.dynamicColor }
        }
        addDependency(options[3], options[0])
        addDependency(options[4], options[1])
        addDependency(options[5], options[2])
        options = listOf("mirror", "canvaSize")
        options.forEach { hideIf(it) { !mode } }
        addListener("canvaSize") {
            for (i in drawer) {
                val pos = indexToPos(i.key)
                if (pos.x >= canvaSize || pos.y >= canvaSize) {
                    Drawer.pixels[i.key].isToggled = false
                }
            }
        }

        if (!renderConfig.didPatcherMigration) {
            try {
                Class.forName("club.sk1er.patcher.config.OldPatcherConfig")
                var didAnything = false
                if (OldPatcherConfig.guiCrosshair) {
                    renderConfig.showInGuis = false
                    didAnything = true
                }
                if (OldPatcherConfig.crosshairPerspective) {
                    renderConfig.showInThirdPerson = false
                    didAnything = true
                }
                if (OldPatcherConfig.removeInvertFromCrosshair) {
                    renderConfig.invertColor = false
                    didAnything = true
                }
                renderConfig.didPatcherMigration = true
                save()
                if (didAnything) {
                    notify("Migrated Patcher settings replaced by PolyCrosshair. Please check PolyCrosshair's settings to make sure they are correct.")
                }
            } catch (_: ClassNotFoundException) {
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
        Drawer.addHideCondition { !mode }
        ConfigUtils.getSubCategory(page, "General", "").options.add(Drawer)
        return null
    }

}
