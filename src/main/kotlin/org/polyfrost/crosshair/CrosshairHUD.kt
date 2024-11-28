package org.polyfrost.crosshair

import org.polyfrost.oneconfig.api.config.v1.annotations.Include
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.polyui.component.impl.Image
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.utils.getResourceStream
import java.nio.file.Files
import java.nio.file.Paths

object CrosshairHUD : Hud<Image>() {
    @Switch(title = "Show in F3")
    private var showInDebug = false

    @Switch(title = "Show in GUIs")
    private var showInGUIs = true

    @Switch(title = "Show in Spectator")
    private var showInSpectator = true

    @Switch(title = "Show in 3rd Person")
    private var showInThirdPerson = true

    @Include
    var currentCrosshair: String = "null"

    override fun category() = Category.COMBAT

    override fun create(): Image {
        if (currentCrosshair == "null") {
            Files.copy(getResourceStream("assets/polycrosshair/default.png"), Paths.get("polycrosshair.png"))
        } else {
            Files.copy(Paths.get(currentCrosshair), Paths.get("polycrosshair.png"))
        }
        return Image("polycrosshair.png")
    }

    fun reload() {
        get().renderer.delete(get().image)
    }

    override fun defaultPosition() = Vec2(1920f / 2f - 7f, 1080f / 2f - 7f)

    override fun id() = "polycrosshair.json"

    override fun title() = "PolyCrosshair"

    override fun update() = false
}