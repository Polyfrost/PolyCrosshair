package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.oneconfig.api.config.v1.Node
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.toggle.v1.ModToggle
import org.polyfrost.oneconfig.api.ui.v1.ModToggles
import org.polyfrost.crosshair.Compat
import java.util.function.Predicate
import java.util.function.Supplier

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

    val penColor: Property<Int> = root.hidden("pen_color", -0x1, Int::class.java)

    val favorites: Property<String> = root.hidden("favorites", "", String::class.java)

    fun favoriteList(): List<String> = favorites.get().orEmpty().split(FAVORITE_SEPARATOR).filter { it.isNotEmpty() }

    fun setFavorites(list: List<String>) {
        favorites.set(list.joinToString(FAVORITE_SEPARATOR))
        save()
    }

    //? if >=1.21 {
    private val elytraOptions: List<Property<*>> = options(elytra.key)

    fun refreshElytraOptions() = elytraOptions.forEach { it.revaluateDisplay() }
    //?}

    init {
        //? if >=1.21 {
        val profiles = listOf(normal, friendly, enemy, elytra)
        //?} else {
        /*val profiles = listOf(normal, friendly, enemy)
        *///?}
        for (profile in profiles) {
            options(profile.key).forEach { if (it !== profile.enabled) it.disableWhen(profile.enabled, false) }
            profile.orbitEnabled?.let { orbit ->
                profile.offsetX?.disableWhen(orbit, true)
                profile.offsetY?.disableWhen(orbit, true)
            }
        }

        options("vanilla").forEach { if (it !== vanilla.enabled) it.disableWhen(vanilla.enabled, false) }
        vanilla.color.disableWhen(vanilla.blendingEnabled, true)

        //? if >=1.21 {
        options("attack_indicator").forEach { if (it !== attackIndicator.enabled) it.disableWhen(attackIndicator.enabled, false) }
        attackIndicator.color.disableWhen(attackIndicator.blendingEnabled, true)
        //?}

        for (accordion in root.map.values.filterIsInstance<Tree>()) {
            @Suppress("UNCHECKED_CAST")
            val head = accordion.map["enabled"] as? Property<Boolean> ?: continue
            accordion.props().forEach { if (it !== head) it.disableWhen(head, false) }
        }

        if (Compat.forcedOff) {
            ModToggles.register("polycrosshair", object : ModToggle {
                override fun isEnabled() = false
                override fun setEnabled(enabled: Boolean) {}
            })
            root.getProp("enabled").description = Compat.forcedOffReason
            root.props().forEach { it.lockWhen { true } }
        }
        //? if >=1.21 {
        if (Compat.addonsInstalled) {
            elytra.enabled.description = "Turned off while Crosshair Addons' elytra indicator is on."
            elytraOptions.forEach { it.lockWhen { Compat.addonsElytra } }
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

    private fun Property<*>.disableWhen(toggle: Property<Boolean>, value: Boolean) {
        toggle.addCallback { revaluateDisplay(); false }
        lockWhen { toggle.get() == value }
    }

    private fun Property<*>.lockWhen(locked: () -> Boolean) {
        addDisplayCondition(Supplier { if (locked()) Property.Display.DISABLED else Property.Display.SHOWN })
        revaluateDisplay()
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
