package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor

class RenderConfig {

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

    var didPatcherMigration = false

}