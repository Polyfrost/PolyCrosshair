package org.polyfrost.crosshair.config

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Config.Category
import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.crosshair.ui.CrosshairEditor

object ModConfig : KtConfig("polycrosshair.json", "PolyCrosshair", Category.HUD, "/assets/polycrosshair/polycrosshair.svg") {

    var enabled by switch(def = true, name = "Enabled", category = "General")

    var mode by dropdown(options = arrayOf("Vanilla", "Custom"), def = 0, name = "Mode", category = "General")

    val custom: Boolean get() = mode == 1

    var scale by slider(min = 0f, max = 200f, def = 100f, name = "Scale %", category = "Transform")
    var rotation by slider(min = -180f, max = 180f, def = 0f, name = "Rotation", category = "Transform")
    var offsetX by slider(min = -1920f, max = 1920f, def = 0f, name = "X Offset", category = "Transform")
    var offsetY by slider(min = -1080f, max = 1080f, def = 0f, name = "Y Offset", category = "Transform")
    var centered by switch(def = false, name = "Centered", category = "Transform")

    var mirror by dropdown(options = arrayOf("Off", "Horizontal", "Vertical", "Quadrant"), def = 0, name = "Mirror", category = "Crosshair")
    var canvaSize by slider(min = 15f, max = 32f, def = 15f, name = "Canvas Size", category = "Crosshair")

    var penColor by color(name = "Pen Color", def = PolyColor(-1), category = "Crosshair")

    var data by property(
        def = CrosshairData(),
        name = "Editor",
        category = "Crosshair",
        visualizer = Visualizer { prop -> CrosshairEditor(prop) },
    )

    var dynamicColor by switch(def = false, name = "Dynamic Color (Overlay)", category = "Dynamic Color")
    var invertColor by switch(def = true, name = "Invert Color", category = "Dynamic Color")
    var dynamicOpacity by slider(min = 0f, max = 100f, def = 100f, name = "Overlay Opacity", category = "Dynamic Color")

    var hostile by switch(def = false, name = "Hostile", category = "Dynamic Color")
    var hostileColor by color(name = "Hostile Color", def = PolyColor(-1), category = "Dynamic Color")
    var passive by switch(def = false, name = "Passive", category = "Dynamic Color")
    var passiveColor by color(name = "Passive Color", def = PolyColor(-1), category = "Dynamic Color")
    var player by switch(def = false, name = "Players", category = "Dynamic Color")
    var playerColor by color(name = "Player Color", def = PolyColor(-1), category = "Dynamic Color")

    var showInDebug by switch(def = false, name = "Show in F3 (Debug)", category = "Visibility")
    var showInGuis by switch(def = true, name = "Show in GUIs", category = "Visibility")
    var showInThirdPerson by switch(def = true, name = "Show in Third Person", category = "Visibility")
    var showInSpectator by switch(def = false, name = "Show in Spectator Mode", category = "Visibility")

    val canvas: Int get() = canvaSize.toInt().coerceIn(15, 32)

    init {
        listOf("scale", "rotation", "offsetX", "offsetY", "centered", "mirror", "canvaSize", "penColor", "data")
            .forEach { hideIf(it) { !custom } }

        listOf("dynamicOpacity", "hostile", "passive", "player", "invertColor").forEach { hideIf(it) { !dynamicColor } }
        hideIf("hostileColor") { !dynamicColor || !hostile }
        hideIf("passiveColor") { !dynamicColor || !passive }
        hideIf("playerColor") { !dynamicColor || !player }
    }
}
