package org.polyfrost.crosshair

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.events.event.ShutdownEvent
import cc.polyfrost.oneconfig.events.event.Stage
import cc.polyfrost.oneconfig.events.event.TickEvent
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
import cc.polyfrost.oneconfig.libs.universal.UResolution
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import org.polyfrost.crosshair.config.Drawer.loadImage
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.render.CrosshairRenderer
import org.polyfrost.crosshair.utils.toBufferedImage
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

    private var lastGuiScale = 1f

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        clearCaches()
        dir.mkdirs()
        ModConfig
        MinecraftForge.EVENT_BUS.register(CrosshairRenderer)
        EventManager.INSTANCE.register(this)
    }

    @Mod.EventHandler
    fun onFMLPostInitialization(event: FMLPostInitializationEvent) {
        CrosshairRenderer.updateVanilla()
    }

    @Subscribe
    fun onTick(event: TickEvent) {
        if (event.stage != Stage.END) return
        if (UResolution.scaleFactor.toFloat() != lastGuiScale) {
            lastGuiScale = UResolution.scaleFactor.toFloat()
            toBufferedImage(ModConfig.newCurrentCrosshair.img)?.let { it ->
                loadImage(it, false, ModConfig.newCurrentCrosshair)?.let {
                    CrosshairRenderer.updateTexture(it)
                }
            }
        }
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