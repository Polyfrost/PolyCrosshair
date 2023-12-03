package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.CustomOption
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.config.configlist.ConfigList
import org.polyfrost.crosshair.config.configlist.Profile
import java.lang.reflect.Field

object ModConfig : Config(Mod(PolyCrosshair.NAME, ModType.HUD), "${PolyCrosshair.MODID}/config.json") {

    @CustomOption
    var profiles = ProfileList()

    var selectedIndex = 0

    init {
        initialize()
    }

    override fun getCustomOption(
        field: Field,
        annotation: CustomOption,
        page: OptionPage,
        mod: Mod,
        migrate: Boolean,
    ): BasicOption? {
        ConfigUtils.getSubCategory(page, "General", "").options.add(Drawer)
        profiles.addOptionTo(this, page, category = "General").select(selectedIndex)
        return null
    }

    class ProfileList : ConfigList<Profile>() {
        override fun onSelected(profile: Profile) {
            selectedIndex = indexOf(profile)
            Drawer.load(profile)
        }

        override fun newConfig() = Profile()
        override fun getName(profile: Profile) = profile.name
    }
}
