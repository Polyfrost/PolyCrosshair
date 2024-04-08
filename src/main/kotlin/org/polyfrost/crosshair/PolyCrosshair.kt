package org.polyfrost.crosshair

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.events.EventManager
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.render.CrosshairRenderer
import java.io.File

@Mod(
    modid = PolyCrosshair.MODID,
    name = PolyCrosshair.NAME,
    version = PolyCrosshair.VERSION,
    modLanguageAdapter = "cc.polyfrost.oneconfig.utils.KotlinLanguageAdapter"
)
object PolyCrosshair {
    const val MODID = "@ID@"
    const val NAME = "@NAME@"
    const val VERSION = "@VER@"

    val path = "${ConfigUtils.getProfileDir().absolutePath}/${MODID}/caches/"

    val dir = File(path)

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        if (!dir.exists()) {
            dir.mkdirs()
        }
        ModConfig
        MinecraftForge.EVENT_BUS.register(CrosshairRenderer)
        EventManager.INSTANCE.register(this)
        CrosshairRenderer.updateTexture()
    }

}