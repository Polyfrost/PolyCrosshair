@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.config

import club.sk1er.patcher.config.OldPatcherConfig
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.utils.*
import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown
import org.polyfrost.oneconfig.api.config.v1.annotations.Include
import org.polyfrost.oneconfig.api.config.v1.annotations.RadioButton
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider

object PolyCrosshairConfig : Config(
    "${PolyCrosshair.MODID}.json",
    "/${PolyCrosshair.MODID}.svg",
    PolyCrosshair.NAME,
    Category.QOL
) {

    val isCustom: Boolean
        get() = mode == 1

    @RadioButton(
        title = "Mode",
        options = ["Vanilla", "Custom"]
    )
    var mode = 0

    @Crosshairs
    var crosshairs = mutableListOf(CrosshairEntry(CrosshairEntry.DEFAULT))

    @Dropdown(
        title = "Mirror",
        options = ["Off", "Horizontal", "Vertical", "Quadrant"]
    )
    var mirror = 0

    @Slider(
        title = "Canva Size",
        min = 15f, max = 32f
    )
    var canvaSize = 15
        get() = field.coerceIn(15, 32)

    var currentCustomCrosshair = CrosshairEntry(CrosshairEntry.DEFAULT)

    var renderConfig = RenderConfig()

    init {
//        this.generateOptionList(currentCustomCrosshair, mod.defaultPage, this.mod, false)
//        this.generateOptionList(renderConfig, mod.defaultPage, this.mod, false)
        var options = listOf("hostile", "passive", "player", "hostileColor", "passiveColor", "playerColor", "dynamicOpacity")
        for (option in options) {
            getProperty(option).addDisplayCondition { if (renderConfig.dynamicColor) Property.Display.HIDDEN else Property.Display.SHOWN }
        }

        getProperty("crosshairs").addMetadata("size", canvaSize)

        addDependency(options[3], options[0])
        addDependency(options[4], options[1])
        addDependency(options[5], options[2])
        addDependency("centered", "mode")
        options = listOf("mirror", "canvaSize")
        options.forEach {
            getProperty(it).addDisplayCondition { if (mode == 0) Property.Display.HIDDEN else Property.Display.SHOWN }
        }

        addCallback("canvaSize") {
            getProperty("crosshairs").addMetadata("size", canvaSize)
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

//    override fun getCustomOption(
//        field: Field,
//        annotation: CustomOption,
//        page: OptionPage,
//        mod: Mod,
//        migrate: Boolean,
//    ): BasicOption? {
//        Drawer.addHideCondition { !mode }
//        ConfigUtils.getSubCategory(page, "General", "").options.add(Drawer)
//        return null
//    }

    enum class MirrorMode {

        OFF,
        HORIZONTAL,
        VERTICAL,
        QUADRANT

    }

}
