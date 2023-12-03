package org.polyfrost.crosshair.config.configlist

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.renderer.asset.SVG
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.drawHollowRoundedRect
import java.util.*


private val PLUS_ICON = SVG("/assets/polycrosshair/plus.svg")

@Suppress("UnstableAPIUsage")
class ConfigListOption<T>(
    val configList: ConfigList<T>,
    val config: Config,
    description: String,
    category: String,
    subcategory: String,
) : BasicOption(null, null, "", description, category, subcategory, 1) {
    private val addButton = BasicButton(32, 32, PLUS_ICON, BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY)
    private val configEntryList = configList.mapTo(ArrayList()) { config ->
        ConfigEntry(this, config)
    }
    private var planToRemove: ConfigEntry<T>? = null

    init {
        if (configEntryList.isEmpty()) add()
        addButton.setClickAction {
            add()
        }
    }

    private fun add() {
        val hud = configList.newConfig()
        configEntryList.add(ConfigEntry(this, hud))
        configList.add(hud)
    }


    override fun getHeight() = configEntryList.size * 48 + 32

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        var nextY = y
        val x1 = x - 242

        for (configEntry in configEntryList) {
            configEntry.drawInList(vg, x1, nextY, inputHandler)
            if (configEntry.config == configList.selectedProfile) vg.drawHollowRoundedRect(
                x = x1 + 46.5f,
                y = nextY - 1.5f,
                width = 672f,
                height = 32f,
                radius = 10f,
                color = ColorPalette.PRIMARY.normalColor,
                thickness = 3f
            )
            nextY += 48
        }

        addButton.draw(vg, x1.toFloat(), nextY.toFloat(), inputHandler)

        checkToRemove()
    }

    fun planToRemove(configEntry: ConfigEntry<T>) {
        planToRemove = configEntry
    }

    fun select(configEntry: ConfigEntry<T>) {
        configList.selectedProfile = configEntry.config
        configList.onSelected(configEntry.config)
    }

    private fun checkToRemove() {
        val removing = planToRemove ?: return
        planToRemove = null
        configEntryList.remove(removing)
        configList.remove(removing.config)
        if (configEntryList.isEmpty()) {
            add()
        }
        if (removing.config == configList.selectedProfile) {
            select(configEntryList.first())
        }
    }

}