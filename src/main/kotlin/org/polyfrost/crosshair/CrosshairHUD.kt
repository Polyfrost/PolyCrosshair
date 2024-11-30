package org.polyfrost.crosshair

import org.polyfrost.oneconfig.api.config.v1.annotations.Include
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.polyui.component.impl.Image
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.utils.getResourceStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

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
    var currentCrosshair: String? = null
        set(value) {
            field = value
            setCrosshair(value)
        }

    private val target = Paths.get("polycrosshair.png")

    override fun category() = Category.COMBAT

    override fun initialize() {
        setCrosshair(currentCrosshair)
    }

    override fun hasBackground() = false

    override fun create() = Image(target.toUri().toString())

    private fun setCrosshair(crosshair: String?) {
        if (crosshair.isNullOrEmpty() || !Paths.get(crosshair).exists()) {
            Files.copy(getResourceStream("assets/polycrosshair/default.png"), target, StandardCopyOption.REPLACE_EXISTING)
        } else Files.copy(Paths.get(crosshair), target, StandardCopyOption.REPLACE_EXISTING)
        val it = get()
        if (!it.initialized) return
        it.renderer.delete(it.image)
        val size = it.polyUI.size
        it.x = size.x / 2f - it.size.x / 2f
        it.y = size.y / 2f - it.size.y / 2f
    }

    override fun defaultPosition() = Vec2(1920f / 2f - 7f, 1080f / 2f - 7f)

    override fun multipleInstancesAllowed() = false

    override fun id() = "polycrosshair.json"

    override fun title() = "PolyCrosshair"

    override fun update() = false
}