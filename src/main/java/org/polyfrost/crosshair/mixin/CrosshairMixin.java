package org.polyfrost.crosshair.mixin;

//? if >=1.21 {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
//? if >=1.21.6 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?} else if >=1.21.4 {
/*import java.util.function.Function;
*///?}
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import org.polyfrost.crosshair.render.CrosshairRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    //? if >=26.2 {
    Hud.class
    //?} else {
    /*Gui.class
    *///?}
)
public abstract class CrosshairMixin {

    @Unique
    private static final String CROSSHAIR_METHOD =
        //? if >=26 {
        "extractCrosshair";
        //?} else {
        /*"renderCrosshair";
        *///?}

    @Unique
    private static final String BLIT_SPRITE =
        //? if >=1.21.11 {
        "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";
        //?} else if >=1.21.6 {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V";
        *///?} else if >=1.21.4 {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V";
        *///?} else {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V";
        *///?}

    @Unique
    private static final String BLIT_SPRITE_REGION =
        //? if >=1.21.11 {
        "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V";
        //?} else if >=1.21.6 {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V";
        *///?} else if >=1.21.4 {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V";
        *///?} else {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V";
        *///?}

    @Unique
    private static final int BLIT_Y =
        //? if >=1.21.4 {
        3;
        //?} else {
        /*2;
        *///?}

    @Unique
    private boolean polycrosshair$spriteReached;

    @WrapWithCondition(
        method =
        //? if >=26 {
        "extractCrosshair"
        //?} else {
        /*"renderCrosshair"
        *///?}
        , at = @At(value = "INVOKE", ordinal = 0, target =
        //? if >=1.21.11 {
        "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
        //?} else if >=1.21.6 {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"
        *///?} else if >=1.21.4 {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V"
        *///?} else {
        /*"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
        *///?}
        ))
    private boolean polycrosshair$replaceSprite(
        GuiGraphicsExtractor graphics,
        //? if >=1.21.6 {
        RenderPipeline pipeline,
        //?} else if >=1.21.4 {
        /*Function<?, ?> renderType,
        *///?}
        //? if >=1.21.11 {
        Identifier sprite,
        //?} else {
        /*ResourceLocation sprite,
        *///?}
        int x, int y, int width, int height) {
        polycrosshair$spriteReached = true;
        return !CrosshairRenderer.INSTANCE.render(graphics, true);
    }

    @ModifyArg(method = CROSSHAIR_METHOD, at = @At(value = "INVOKE", target = BLIT_SPRITE, ordinal = 1), index = BLIT_Y)
    private int polycrosshair$moveAttackIndicator(int y) {
        return CrosshairRenderer.INSTANCE.attackIndicatorY(y);
    }

    @ModifyArg(method = CROSSHAIR_METHOD, at = @At(value = "INVOKE", target = BLIT_SPRITE, ordinal = 2), index = BLIT_Y)
    private int polycrosshair$moveAttackIndicatorBackground(int y) {
        return CrosshairRenderer.INSTANCE.attackIndicatorY(y);
    }

    @ModifyArg(method = CROSSHAIR_METHOD, at = @At(value = "INVOKE", target = BLIT_SPRITE_REGION), index = BLIT_Y + 4)
    private int polycrosshair$moveAttackIndicatorProgress(int y) {
        return CrosshairRenderer.INSTANCE.attackIndicatorY(y);
    }

    @Inject(
        method =
        //? if >=26 {
        "extractCrosshair"
        //?} else {
        /*"renderCrosshair"
        *///?}
        , at = @At("RETURN"))
    private void polycrosshair$drawWhenVanillaHides(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!polycrosshair$spriteReached) CrosshairRenderer.INSTANCE.render(graphics, false);
        polycrosshair$spriteReached = false;
    }
}
//?} else {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.render.Window;
import org.polyfrost.crosshair.render.CrosshairRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameGui.class)
public abstract class CrosshairMixin {

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;hasCrosshair()Z"))
    private boolean polycrosshair$replaceCrosshair(boolean original) {
        return !CrosshairRenderer.INSTANCE.render(new Window(Minecraft.getInstance()), original) && original;
    }
}
*///?}
