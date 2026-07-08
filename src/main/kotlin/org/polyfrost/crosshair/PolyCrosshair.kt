package org.polyfrost.crosshair

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.crosshair.config.ModConfig

object PolyCrosshair : ClientModInitializer {
    const val NAME = "PolyCrosshair"

    override fun onInitializeClient() {
        ModConfig.preload()
    }
}
