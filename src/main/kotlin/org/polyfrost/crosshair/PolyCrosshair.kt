package org.polyfrost.crosshair

import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.polyfrost.crosshair.config.ModConfig

object PolyCrosshair : ClientModInitializer {
    const val NAME = "PolyCrosshair"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(NAME)

    override fun onInitializeClient() {
        ModConfig.preload()
    }
}
