@file:OptIn(ExperimentalOneConfigUiApi::class)

package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.oneconfig.api.config.v1.Node
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.dependsOn
import org.polyfrost.oneconfig.api.config.v1.disableAllUnless
import org.polyfrost.oneconfig.api.config.v1.disabledWhen
import org.polyfrost.oneconfig.api.config.v1.enableToggle
import org.polyfrost.oneconfig.api.config.v1.hidden
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.ModToggles
import org.polyfrost.crosshair.Compat

object ModConfig : KtConfig(
    "polycrosshair.json",
    "PolyCrosshair",
    Category.HUD,
    "/assets/polycrosshair/polycrosshair.svg",
) {

    var enabled by switch(
        def = false,
        name = "Enabled",
        description = "Replaces the vanilla crosshair with your own.",
        category = GENERAL,
    )

    private val root: Tree = makeTree()

    val normal = CrosshairProfile(
        root, "normal", "Normal",
        enabledDescription = "Shows this crosshair when you aren't looking at a friendly or enemy target. Turn this off to use the vanilla crosshair instead.",
        enabledDefault = true,
    )

    val friendly = CrosshairProfile(root, "friendly", "Friendly") { normal }

    val enemy = CrosshairProfile(root, "enemy", "Enemy") { normal }

    //? if >=1.21 {
    val elytra = CrosshairProfile(
        root, "elytra", "Elytra",
        defaultImage = CrosshairPixels.ELYTRA,
        enabledDescription = "Shows this below your crosshair while you're wearing an elytra.",
        enabledDefault = true,
        indicator = true,
    ) { normal }
    //?}

    val vanilla = VanillaCrosshair(root)

    //? if >=1.21 {
    val attackIndicator = AttackIndicator(root)
    //?}

    val debugCrosshair = DebugCrosshair(root)

    val penColor: Property<Int> = root.hidden("pen_color", -0x1)

    val favorites: Property<String> = root.hidden("favorites", "")

    fun favoriteList(): List<String> = favorites.get().orEmpty().split(FAVORITE_SEPARATOR).filter { it.isNotEmpty() }

    fun setFavorites(list: List<String>) {
        favorites.set(list.joinToString(FAVORITE_SEPARATOR))
        save()
    }

    init {
        addAliases(*arrayOf("crosshair"))

        //? if >=1.21 {
        val profiles = listOf(normal, friendly, enemy, elytra)
        //?} else {
        /*val profiles = listOf(normal, friendly, enemy)
        *///?}
        for (profile in profiles) {
            options(profile.key).forEach { if (it !== profile.enabled) it.dependsOn(profile.enabled) }
            profile.orbitEnabled?.let { orbit ->
                profile.offsetX?.dependsOn(orbit, expected = false)
                profile.offsetY?.dependsOn(orbit, expected = false)
            }
        }

        options("vanilla").forEach { if (it !== vanilla.enabled) it.dependsOn(vanilla.enabled) }
        vanilla.color.dependsOn(vanilla.blendingEnabled, expected = false)

        //? if >=1.21 {
        options("attack_indicator").forEach { if (it !== attackIndicator.enabled) it.dependsOn(attackIndicator.enabled) }
        attackIndicator.color.dependsOn(attackIndicator.blendingEnabled, expected = false)
        attackIndicator.color.dependsOn(attackIndicator.chargeColorsEnabled, expected = false)
        //?}

        options("debug_crosshair").forEach { if (it !== debugCrosshair.enabled) it.dependsOn(debugCrosshair.enabled) }
        for (axis in listOf(debugCrosshair.xAxis, debugCrosshair.yAxis, debugCrosshair.zAxis)) {
            axis.outlineColor.dependsOn(axis.outline)
        }

        for (group in root.map.values.filterIsInstance<Tree>()) {
            if (group.enableToggle != null) group.disableAllUnless("enabled")
        }

        if (Compat.forcedOff) {
            ModToggles.registerReadOnly("polycrosshair", { false }, { Compat.forcedOffReason })
            root.getProp("enabled").description = Compat.forcedOffReason
            root.props().forEach { it.disabledWhen { true } }
        }
        //? if >=1.21 {
        if (Compat.addonsInstalled) {
            elytra.enabled.description = "Turned off while Crosshair Addons' elytra indicator is on."
            options(elytra.key).forEach { it.disabledWhen(pollEveryTicks = ADDONS_POLL_TICKS) { Compat.addonsElytra } }
        }
        //?}
    }

    private fun options(key: String): List<Property<*>> =
        root.map.values.filter { it.id?.startsWith("${key}_") == true }.flatMap { it.props() }

    private fun Node.props(): List<Property<*>> = when (this) {
        is Property<*> -> listOf(this)
        is Tree -> map.values.flatMap { it.props() }
        else -> emptyList()
    }

    fun profile(kind: TargetKind): CrosshairProfile? {
        val target = when (kind) {
            TargetKind.NORMAL -> normal
            TargetKind.FRIENDLY -> friendly
            TargetKind.ENEMY -> enemy
        }
        return target.takeIf { it.enabled.get() == true } ?: normal.takeIf { it.enabled.get() == true }
    }
}

private const val GENERAL = "General"
private const val FAVORITE_SEPARATOR = ","
private const val ADDONS_POLL_TICKS = 10
