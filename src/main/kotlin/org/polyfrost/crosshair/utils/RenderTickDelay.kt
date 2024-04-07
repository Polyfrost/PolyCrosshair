package org.polyfrost.crosshair.utils

import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.events.event.RenderEvent
import cc.polyfrost.oneconfig.events.event.Stage
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe

class RenderTickDelay(function: Runnable, ticks: Int) {
    private val function: Runnable
    private var delay = 0

    init {
        if (ticks < 1) {
            function.run()
        } else {
            EventManager.INSTANCE.register(this)
            delay = ticks
        }
        this.function = function
    }

    @Subscribe
    protected fun onTick(event: RenderEvent) {
        if (event.stage == Stage.START) {
            if (delay < 1) {
                function.run()
                EventManager.INSTANCE.unregister(this)
            }
            delay--
        }
    }
}