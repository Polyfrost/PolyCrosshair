package org.polyfrost.crosshair

import net.minecraft.client.renderer.texture.TextureUtil
import org.polyfrost.oneconfig.api.config.v1.annotations.Button
import org.polyfrost.oneconfig.api.config.v1.annotations.Include
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.utils.getResourceStream
import org.polyfrost.universal.UMatrixStack
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import net.minecraft.client.renderer.GlStateManager as GL

object CrosshairHUD : LegacyHud() {

    @Switch(title = "Use Vanilla")
    var useVanilla = false

    @Switch(title = "Show in F3")
    var showInDebug = false

    @Switch(title = "Show in Spectator")
    var showInSpectator = true

    @Switch(title = "Show in 3rd Person")
    var showInThirdPerson = true

    @Include
    var currentCrosshair: String? = null

    @Button(title = "Open Editor")
    private val openEditor = Runnable {
        PolyCrosshairUI.open()
    }

    val id = GL.generateTexture()
    private var texSize = 15f

    override var width: Float
        get() = texSize
        set(value) {}

    override var height: Float
        get() = texSize
        set(value) {}

    override fun category() = Category.COMBAT

    override fun initialize() {
        val currentCrosshair = currentCrosshair
        val stream = when {
            currentCrosshair.isNullOrEmpty() -> getResourceStream("assets/polycrosshair/default.png")
            else -> {
                val p = Paths.get(currentCrosshair)
                if (p.exists()) p.inputStream() else getResourceStream("assets/polycrosshair/default.png")
            }
        }
        val img = ImageIO.read(stream)
        setCrosshair(img.getRGB(0, 0, img.width, img.height, null, 0, img.width), img.width)
    }

    override fun hasBackground() = false

    override fun render(stack: UMatrixStack, x: Float, y: Float, scaleX: Float, scaleY: Float) {
    }


    fun setCrosshair(cdata: IntArray, size: Int) {
        texSize = size.toFloat()
        TextureUtil.allocateTexture(id, size, size)
        TextureUtil.uploadTexture(id, cdata, size, size)
    }

    override fun defaultPosition() = Vec2(1920f / 2f, 1080f / 2f)

    override fun multipleInstancesAllowed() = false

    override fun id() = "polycrosshair.json"

    override fun title() = "PolyCrosshair"

    override fun update() = false
}