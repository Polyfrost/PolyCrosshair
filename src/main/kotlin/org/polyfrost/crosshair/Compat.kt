@file:OptIn(ExperimentalOneConfigUiApi::class)

package org.polyfrost.crosshair

import net.fabricmc.loader.api.FabricLoader
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.render.CrosshairRenderer
import org.polyfrost.oneconfig.api.notifications.v1.NotificationType
import org.polyfrost.oneconfig.api.notifications.v1.Notifications
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.ModToggles
import java.lang.reflect.Field

object Compat {

    private val conflicting = installed("dynamiccrosshair" to "Dynamic Crosshair", "custom-crosshair-mod" to "Custom Crosshair Mod")

    private val replaced = installed("centered-crosshair" to "Centered Crosshair", "crosshairtweaks" to "Crosshair Tweaks", "entitycrosshair" to "Entity Crosshair")

    private val addonsConfig: Class<*>? =
        if (FabricLoader.getInstance().isModLoaded("crosshairaddons")) {
            ADDONS_CONFIG_CLASSES.firstNotNullOfOrNull { runCatching { Class.forName(it) }.getOrNull() }
                .also { if (it == null) PolyCrosshair.LOGGER.warn("Couldn't find Crosshair Addons' config in {}", ADDONS_CONFIG_CLASSES) }
        } else {
            null
        }

    private val addonsElytraFlags: List<Field>? = addonsConfig?.let { config ->
        runCatching { listOf("modEnabled", "elytraAddonEnabled").map(config::getField) }
            .onFailure { PolyCrosshair.LOGGER.warn("Couldn't read Crosshair Addons' config", it) }.getOrNull()
    }

    private val addonsThirdPerson: Field? = addonsConfig?.let { runCatching { it.getField("showInThirdPerson") }.getOrNull() }

    @JvmField
    val forcedOff = conflicting.isNotEmpty()

    val forcedOffReason = "Turned off because ${conflicting.joinToString(" and ")} is installed. Remove it to use ${PolyCrosshair.NAME}."

    private val replacedNotice = "${PolyCrosshair.NAME} replaces ${replaced.joinToString(" and ")}. Their crosshair options are ignored while it's enabled."

    @JvmStatic
    fun overriding(): Boolean = !forcedOff && ModConfig.enabled

    init {
        if (FabricLoader.getInstance().isModLoaded("entitycrosshair")) {
            ModToggles.registerReadOnly(
                "entitycrosshair",
                { !overriding() },
                { if (overriding()) "Turned off while ${PolyCrosshair.NAME} is enabled." else null },
            )
        }
    }

    val addonsInstalled = addonsElytraFlags != null

    val addonsElytra: Boolean
        get() = addonsElytraFlags?.all { it.getBoolean(null) } == true &&
            (CrosshairRenderer.firstPerson() || addonsThirdPerson?.getBoolean(null) == true)

    fun tick() {
        if (forcedOff) {
            Notifications.sendOnce(FORCED_OFF_NOTICE, PolyCrosshair.NAME, forcedOffReason, NotificationType.ERROR)
        } else if (replaced.isNotEmpty() && ModConfig.enabled) {
            Notifications.sendOnce(REPLACED_NOTICE, PolyCrosshair.NAME, replacedNotice)
        }
    }

    private fun installed(vararg mods: Pair<String, String>): List<String> =
        mods.filter { FabricLoader.getInstance().isModLoaded(it.first) }.map { it.second }
}

private const val FORCED_OFF_NOTICE = "polycrosshair:forced-off"
private const val REPLACED_NOTICE = "polycrosshair:replaced"
private val ADDONS_CONFIG_CLASSES = listOf("walksy.crosshairaddons.config.CrosshairAddonsConfig", "walksy.crosshairaddons.config.Config")
