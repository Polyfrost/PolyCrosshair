package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.KtConfig
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree

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

    val penColor: Property<Int> = root.hidden("pen_color", -0x1, Int::class.java)

    val favorites: Property<String> = root.hidden("favorites", "", String::class.java)

    fun favoriteList(): List<String> = favorites.get().orEmpty().split(FAVORITE_SEPARATOR).filter { it.isNotEmpty() }

    fun setFavorites(list: List<String>) {
        favorites.set(list.joinToString(FAVORITE_SEPARATOR))
        save()
    }

    fun profile(kind: TargetKind): CrosshairProfile = when (kind) {
        TargetKind.NORMAL -> normal
        TargetKind.FRIENDLY -> friendly.takeIf { it.enabled?.get() == true } ?: normal
        TargetKind.ENEMY -> enemy.takeIf { it.enabled?.get() == true } ?: normal
    }
}

private const val GENERAL = "General"
private const val FAVORITE_SEPARATOR = ","
