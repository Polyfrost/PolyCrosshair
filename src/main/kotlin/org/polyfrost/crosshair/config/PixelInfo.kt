package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.core.OneColor

class PixelInfo(color: Int) {

    var color = OneColor(255, 255, 255, 255).rgb

    init {
        this.color = color
    }
}