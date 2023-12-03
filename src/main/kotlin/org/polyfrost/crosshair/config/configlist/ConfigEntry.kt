package org.polyfrost.crosshair.config.configlist

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.gui.pages.ModConfigPage
import cc.polyfrost.oneconfig.renderer.asset.SVG
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.nanoVGHelper

private val MINUS_ICON = SVG("/assets/polycrosshair/minus.svg")
private const val WHITE_90 = 0xE5FFFFFF.toInt()
private val color = OneColor(0, 0, 0, 0)
private val emptyColor = ColorPalette(color, color, color)

@Suppress("UnstableAPIUsage")
class ConfigEntry<T>(
    private val configListOption: ConfigListOption<T>,
    val config: T,
) {
    private val selectButton = BasicButton(672, 32, "", BasicButton.ALIGNMENT_CENTER, emptyColor)
    private val removeButton =
        BasicButton(32, 32, MINUS_ICON, BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY_DESTRUCTIVE)
    private val pageButton = BasicButton(256, 32, "Edit", BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY)
    private val optionPage = OptionPage("Options", configListOption.config.mod)
    private val configPage: ModConfigPage

    init {
        val options: List<BasicOption> = ConfigUtils.getClassOptions(config)

        ConfigUtils.getSubCategory(optionPage, configListOption.category, configListOption.subcategory).options.addAll(
            options
        )

        configListOption.configList.postInitOptions(this, options)
        // subcategory must be generated before creating config page
        configPage = ModConfigPage(optionPage)

        removeButton.setClickAction {
            configListOption.planToRemove(this)
        }

        selectButton.setClickAction {
            configListOption.select(this)
        }

        pageButton.setClickAction {
            OneConfigGui.INSTANCE.openPage(configPage)
        }
    }

    fun drawInList(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        removeButton.draw(vg, x.toFloat(), y.toFloat(), inputHandler)
        selectButton.draw(vg, (x + 48).toFloat(), y.toFloat(), inputHandler)
        nanoVGHelper.drawText(vg, configListOption.configList.getName(config), (x + 64).toFloat(), (y + 17).toFloat(), WHITE_90, 14f, Fonts.MEDIUM)
        pageButton.draw(vg, (x + 736).toFloat(), y.toFloat(), inputHandler)
    }
}
