package org.polyfrost.crosshair.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.polyfrost.crosshair.ui.CanvasHover;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "org.polyfrost.oneconfig.internal.ui.keybind.KeybindRecordingBus", remap = false)
public abstract class KeybindRecordingBusMixin {

    @ModifyReturnValue(method = "isRecording", at = @At("RETURN"), require = 0)
    private static boolean polycrosshair$keepRightClickForCanvas(boolean original) {
        return original || CanvasHover.owner != null;
    }
}
