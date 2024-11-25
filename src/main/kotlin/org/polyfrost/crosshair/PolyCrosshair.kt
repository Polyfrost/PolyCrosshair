package org.polyfrost.crosshair

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import org.polyfrost.crosshair.config.PolyCrosshairConfig
import org.polyfrost.crosshair.render.CrosshairRenderer
import org.polyfrost.oneconfig.api.event.v1.EventManager
import org.polyfrost.oneconfig.api.event.v1.events.ShutdownEvent
import org.polyfrost.oneconfig.api.event.v1.invoke.impl.Subscribe
import java.io.File

@Mod(
    modid = PolyCrosshair.MODID,
    name = PolyCrosshair.NAME,
    version = PolyCrosshair.VERSION,
    modLanguageAdapter = "org.polyfrost.oneconfig.utils.v1.forge.KotlinLanguageAdapter"
)
object PolyCrosshair {
    const val MODID = "@MOD_ID@"
    const val NAME = "@MOD_NAME@"
    const val VERSION = "@MOD_VERSION@"

//    val path = "${ConfigUtils.getProfileDir().absolutePath}/${MODID}/caches/"
    val path = "config/${MODID}/caches/"

    val dir = File(path)

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        clearCaches()
        dir.mkdirs()
        PolyCrosshairConfig
        MinecraftForge.EVENT_BUS.register(CrosshairRenderer)
        EventManager.INSTANCE.register(this)
    }

    @Mod.EventHandler
    fun onFMLPostInitialization(event: FMLPostInitializationEvent) {
        CrosshairRenderer.updateVanilla()
    }

    @Subscribe
    fun onShutDown(e: ShutdownEvent) {
        clearCaches()
    }

    fun clearCaches() {
        if (dir.listFiles()?.isNotEmpty() == true) {
            for (file in dir.listFiles()!!) {
                file.delete()
            }
        }
        dir.delete()
    }

}