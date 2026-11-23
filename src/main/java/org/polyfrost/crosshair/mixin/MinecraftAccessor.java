package org.polyfrost.crosshair.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
//? if <1.21 {
/*import net.minecraft.client.TickTimer;
import org.spongepowered.asm.mixin.gen.Accessor;
*///?}

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    //? if <1.21 {
    /*@Accessor
    TickTimer getTimer();
    *///?}
}
