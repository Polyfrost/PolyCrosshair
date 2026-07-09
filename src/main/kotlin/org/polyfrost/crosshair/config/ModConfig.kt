package org.polyfrost.crosshair.config

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.oneconfig.api.config.v1.serialize.ObjectSerializer
import org.polyfrost.crosshair.ui.CrosshairEditor

object ModConfig : KtConfig("polycrosshair.json", "PolyCrosshair", Category.HUD, "/assets/polycrosshair/polycrosshair.svg") {

    init { ObjectSerializer.INSTANCE.registerTypeAdapter(CrosshairDataAdapter()) }

    var enabled by switch(def = true, name = "Enabled")

    var mode by dropdown(options = arrayOf("Vanilla", "Custom"), def = 0, name = "Mode")

    val custom: Boolean get() = mode == 1

    private val editorTree = Tree.tree("editor").apply { setTitle("Crosshair Editor") }

    init {
        makeTree().put(editorTree)
        tree = editorTree
    }

    var scale by slider(min = 0f, max = 200f, def = 100f, name = "Scale %")
    var rotation by slider(min = -180f, max = 180f, def = 0f, name = "Rotation")
    var offsetX by slider(min = -1920f, max = 1920f, def = 0f, name = "X Offset")
    var offsetY by slider(min = -1080f, max = 1080f, def = 0f, name = "Y Offset")
    var centered by switch(def = false, name = "Centered")

    var mirror by dropdown(options = arrayOf("Off", "Horizontal", "Vertical", "Quadrant"), def = 0, name = "Mirror")
    var canvaSize by slider(min = 15f, max = 32f, def = 15f, name = "Canvas Size")

    var penColor by color(name = "Pen Color", def = PolyColor(-1))

    var data by property(
        def = CrosshairData(),
        name = "Editor",
        visualizer = Visualizer { prop -> CrosshairEditor(prop) },
    )

    init { tree = null }

    var dynamicColor by switch(def = false, name = "Dynamic Color (Overlay)")
    var invertColor by switch(def = true, name = "Invert Color")
    var dynamicOpacity by slider(min = 0f, max = 100f, def = 100f, name = "Overlay Opacity")

    var hostile by switch(def = false, name = "Hostile")
    var hostileColor by color(name = "Hostile Color", def = PolyColor(-1))
    var passive by switch(def = false, name = "Passive")
    var passiveColor by color(name = "Passive Color", def = PolyColor(-1))
    var player by switch(def = false, name = "Players")
    var playerColor by color(name = "Player Color", def = PolyColor(-1))

    var showInDebug by switch(def = false, name = "Show in F3 (Debug)")
    var showInGuis by switch(def = true, name = "Show in GUIs")
    var showInThirdPerson by switch(def = true, name = "Show in Third Person")
    var showInSpectator by switch(def = false, name = "Show in Spectator Mode")

    val canvas: Int get() = canvaSize.toInt().coerceIn(15, 32)

    init {
        listOf("scale", "rotation", "offsetX", "offsetY", "centered", "mirror", "canvaSize", "penColor", "data")
            .forEach { hideIf("editor.$it") { !custom } }

        listOf("dynamicOpacity", "hostile", "passive", "player", "invertColor").forEach { hideIf(it) { !dynamicColor } }
        hideIf("hostileColor") { !dynamicColor || !hostile }
        hideIf("passiveColor") { !dynamicColor || !passive }
        hideIf("playerColor") { !dynamicColor || !player }
    }
}
