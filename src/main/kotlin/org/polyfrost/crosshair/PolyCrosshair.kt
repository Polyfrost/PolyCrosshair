package org.polyfrost.crosshair

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.render.CrosshairRenderer

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


    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        ModConfig
        MinecraftForge.EVENT_BUS.register(CrosshairRenderer)
        CrosshairRenderer.updateTexture()
    }

}