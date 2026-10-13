package org.polyfrost.crosshair

import net.fabricmc.loader.api.FabricLoader
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.oneconfig.api.notifications.v1.NotificationType
import org.polyfrost.oneconfig.api.notifications.v1.Notifications
import java.lang.reflect.Field

object Compat {

    private val conflicting = installed("dynamiccrosshair" to "Dynamic Crosshair", "custom-crosshair-mod" to "Custom Crosshair Mod")

    private val replaced = installed("centered-crosshair" to "Centered Crosshair", "crosshairtweaks" to "Crosshair Tweaks")

    private val addonsElytraFlags: List<Field>? =
        if (FabricLoader.getInstance().isModLoaded("crosshairaddons")) {
            runCatching {
                val config = Class.forName("walksy.crosshairaddons.config.Config")
                listOf("modEnabled", "elytraAddonEnabled").map(config::getField)
            }.onFailure { PolyCrosshair.LOGGER.warn("Couldn't read Crosshair Addons' config", it) }.getOrNull()
        } else {
            null
        }

    private var warned = false

    @JvmField
    val forcedOff = conflicting.isNotEmpty()

    val forcedOffReason = "Turned off because ${conflicting.joinToString(" and ")} is installed. Remove it to use ${PolyCrosshair.NAME}."

    val addonsInstalled = addonsElytraFlags != null

    val addonsElytra: Boolean get() = addonsElytraFlags?.all { it.getBoolean(null) } == true

    private var lastAddonsElytra = addonsElytra

    fun tick() {
        warn()
        //? if >=1.21 {
        val elytra = addonsElytra
        if (elytra != lastAddonsElytra) {
            lastAddonsElytra = elytra
            ModConfig.refreshElytraOptions()
        }
        //?}
    }

    private fun warn() {
        if (warned) return
        if (forcedOff) {
            Notifications.send(PolyCrosshair.NAME, forcedOffReason, NotificationType.ERROR)
        } else if (replaced.isNotEmpty() && ModConfig.enabled) {
            Notifications.info(
                PolyCrosshair.NAME,
                "${PolyCrosshair.NAME} replaces ${replaced.joinToString(" and ")}. Their crosshair options are ignored while it's enabled.",
            )
        } else {
            return
        }
        warned = true
    }

    private fun installed(vararg mods: Pair<String, String>): List<String> =
        mods.filter { FabricLoader.getInstance().isModLoaded(it.first) }.map { it.second }
}
