package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.toggle.v1.ModToggle
import org.polyfrost.oneconfig.api.ui.v1.ModToggles
import org.polyfrost.crosshair.Compat
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

    val normal = CrosshairProfile(root, "normal", "Normal")

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
    private val elytraOptions: List<Property<*>> = buildList {
        for (node in root.map.values) {
            if (node.id?.startsWith("${elytra.key}_") != true) continue
            when (node) {
                is Property<*> -> add(node)
                is Tree -> node.onAllProps { _, prop -> add(prop) }
            }
        }
    }

    fun refreshElytraOptions() = elytraOptions.forEach { it.revaluateDisplay() }
    //?}

    init {
        if (Compat.forcedOff) {
            ModToggles.register("polycrosshair", object : ModToggle {
                override fun isEnabled() = false
                override fun setEnabled(enabled: Boolean) {}
            })
            root.getProp("enabled").description = Compat.forcedOffReason
            root.onAllProps { _, prop -> prop.lockWhen { true } }
        }
        //? if >=1.21 {
        if (Compat.addonsInstalled) {
            elytra.enabled?.description = "Turned off while Crosshair Addons' elytra indicator is on."
            elytraOptions.forEach { it.lockWhen { Compat.addonsElytra } }
        }
        //?}
    }

    private fun Property<*>.lockWhen(locked: () -> Boolean) {
        addDisplayCondition(Supplier { if (locked()) Property.Display.DISABLED else Property.Display.SHOWN })
        revaluateDisplay()
    }

    fun profile(kind: TargetKind): CrosshairProfile = when (kind) {
        TargetKind.NORMAL -> normal
        TargetKind.FRIENDLY -> friendly.takeIf { it.enabled?.get() == true } ?: normal
        TargetKind.ENEMY -> enemy.takeIf { it.enabled?.get() == true } ?: normal
    }
}

private const val GENERAL = "General"
private const val FAVORITE_SEPARATOR = ","
