package org.polyfrost.crosshair

import cc.polyfrost.oneconfig.utils.commands.annotations.Command
import cc.polyfrost.oneconfig.utils.commands.annotations.Main
import org.polyfrost.crosshair.config.ModConfig

@Command(value = PolyCrosshair.MODID)
class ModCommand {

    @Main
    fun openGui() {
        ModConfig.openGui()
    }

}