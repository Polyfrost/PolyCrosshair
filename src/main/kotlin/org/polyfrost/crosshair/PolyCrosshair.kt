package org.polyfrost.crosshair

import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.oneconfig.api.event.v1.EventManager
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent

object PolyCrosshair : ClientModInitializer {
    const val NAME = "PolyCrosshair"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(NAME)

    override fun onInitializeClient() {
        ModConfig.preload()
        EventManager.register(TickEvent.End::class.java, Runnable(Compat::tick))
    }
}
