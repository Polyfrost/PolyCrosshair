package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.polyui.color.PolyColor

class RenderConfig {

    @Switch(title = "Dynamic Color (Overlay)")
    var dynamicColor = false

    @Switch(title = "Invert Color")
    var invertColor = true

    @Slider(title = "Overlay Opacity", min = 0f, max = 100f)
    var dynamicOpacity = 100

    @Switch(title = "Hostile")
    var hostile = false

    @Color(title = "Color")
    var hostileColor = PolyColor.WHITE

    @Switch(title = "Passive")
    var passive = false

    @Color(title = "Color")
    var passiveColor = PolyColor.WHITE

    @Switch(title = "Players")
    var player = false

    @Color(title = "Color")
    var playerColor = PolyColor.WHITE

    @Switch(title = "Show in F3 (Debug)")
    var showInDebug = false

    @Switch(title = "Show in GUIs")
    var showInGuis = true

    @Switch(title = "Show in Third Person")
    var showInThirdPerson = true

    @Switch(title = "Show in Spectator Mode")
    var showInSpectator = false

    var didPatcherMigration = false

}