package org.polyfrost.crosshair

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.polyfrost.oneconfig.api.hud.v1.HudManager

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

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        HudManager.register(CrosshairHUD)
    }

}