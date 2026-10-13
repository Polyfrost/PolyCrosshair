package org.polyfrost.crosshair.render

//? if >=1.21 {
import net.minecraft.client.AttackIndicatorStatus
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.component.DataComponents
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.BundleItem
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.ShulkerBoxBlock
import org.polyfrost.crosshair.Compat
import org.polyfrost.crosshair.config.ModConfig
//? if >=1.21.11 {
import net.minecraft.resources.Identifier as ResourceLocation
//?} else {
/*import net.minecraft.resources.ResourceLocation
*///?}
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines
//?} else if >=1.21.4 {
/*import net.minecraft.client.renderer.RenderType
*///?} else {
/*import com.mojang.blaze3d.systems.RenderSystem
*///?}
import kotlin.math.roundToInt

object AttackIndicatorRenderer {

    private val FULL = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_full")
    private val BACKGROUND = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_background")
    private val PROGRESS = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_progress")

    private const val SHULKER_SLOTS = 27

    private val mc: Minecraft get() = Minecraft.getInstance()

    fun replacesVanilla(): Boolean = !Compat.forcedOff && ModConfig.attackIndicator.enabled.get() == true

    fun render(graphics: GuiGraphicsExtractor) {
        if (!replacesVanilla()) return
        val player = mc.player ?: return
        val status = mc.options.attackIndicator().get()
        if (status == AttackIndicatorStatus.OFF) return
        val showAttack = status == AttackIndicatorStatus.CROSSHAIR &&
            ModConfig.attackIndicator.attackEnabled.get() == true && attackApplies(player.mainHandItem)

        val x = graphics.guiWidth() / 2 - 8 + (ModConfig.attackIndicator.offsetX.get() ?: 0f).roundToInt()
        val baseY = graphics.guiHeight() / 2 - 7 + 16 + (ModConfig.attackIndicator.offsetY.get() ?: 0f).roundToInt()
        val y = CrosshairRenderer.attackIndicatorY(baseY, x)

        val progress = customProgress(player)
        if (progress == null && !showAttack) return

        //? if <1.21.4 {
        /*RenderSystem.enableBlend()
        CrosshairRenderer.invertingBlend()
        *///?}
        if (progress != null) {
            bar(graphics, x, y, progress)
        } else {
            val strength = player.getAttackStrengthScale(0f)
            if (fullyCharged(player, strength)) {
                sprite(graphics, FULL, x, y, 16, 16)
            } else if (strength < 1f) {
                bar(graphics, x, y, strength)
            }
        }
        //? if <1.21.4 {
        /*RenderSystem.defaultBlendFunc()
        RenderSystem.disableBlend()
        *///?}
    }

    private fun customProgress(player: LocalPlayer): Float? = with(ModConfig.attackIndicator) {
        if (sleep.get() == true && player.isSleeping) {
            return (player.sleepTimer / Player.SLEEP_DURATION.toFloat()).coerceIn(0f, 1f)
        }
        if (blockBreaking.get() == true && mc.gameMode?.isDestroying == true) {
            val stage = mc.gameMode?.destroyStage ?: -1
            if (stage >= 0) return (stage + 1) / 10f
        }
        if (player.isUsingItem) {
            val stack = player.useItem
            val item = stack.item
            if (bowCharge.get() == true) {
                if (item is BowItem) return (player.ticksUsingItem / 20f).coerceAtMost(1f)
                if (item is CrossbowItem) {
                    return (player.ticksUsingItem / CrossbowItem.getChargeDuration(stack, player).toFloat()).coerceAtMost(1f)
                }
            }
            if (consumables.get() == true && stack.useAnimation.name.let { it == "EAT" || it == "DRINK" }) {
                val duration = stack.getUseDuration(player)
                if (duration > 0) return (player.ticksUsingItem / duration.toFloat()).coerceAtMost(1f)
            }
        }
        for (hand in InteractionHand.entries) {
            val stack = player.getItemInHand(hand)
            if (stack.isEmpty) continue
            val enabled = if (stack.`is`(Items.SHIELD)) shieldCooldown else itemCooldown
            if (enabled.get() != true) continue
            val remaining = cooldown(player, stack)
            if (remaining > 0f) return 1f - remaining
        }
        if (storage.get() == true) {
            storageFullness(player.mainHandItem)?.let { return it }
        }
        return null
    }

    private fun cooldown(player: LocalPlayer, stack: ItemStack): Float =
        //? if >=1.21.4 {
        player.cooldowns.getCooldownPercent(stack, 0f)
        //?} else {
        /*player.cooldowns.getCooldownPercent(stack.item, 0f)
        *///?}

    private fun storageFullness(stack: ItemStack): Float? {
        if (stack.item is BundleItem) return BundleItem.getFullnessDisplay(stack)
        if ((stack.item as? BlockItem)?.block !is ShulkerBoxBlock) return null
        val used = stack.get(DataComponents.CONTAINER)?.nonEmptyItems()?.count() ?: 0
        return used / SHULKER_SLOTS.toFloat()
    }

    private fun attackApplies(stack: ItemStack): Boolean = with(ModConfig.attackIndicator) {
        val toggle = when {
            stack.`is`(ItemTags.SWORDS) -> attackSwords
            stack.`is`(ItemTags.AXES) -> attackAxes
            stack.`is`(ItemTags.PICKAXES) || stack.`is`(ItemTags.SHOVELS) || stack.`is`(ItemTags.HOES) -> attackTools
            //? if >=1.21.11 {
            stack.`is`(ItemTags.SPEARS) -> attackHeavy
            //?}
            stack.`is`(Items.TRIDENT) || stack.`is`(Items.MACE) -> attackHeavy
            else -> attackOther
        }
        toggle.get() == true
    }

    private fun fullyCharged(player: LocalPlayer, strength: Float): Boolean {
        val target = mc.crosshairPickEntity as? LivingEntity ?: return false
        return strength >= 1f && player.currentItemAttackStrengthDelay > 5f && target.isAlive
    }

    private fun bar(graphics: GuiGraphicsExtractor, x: Int, y: Int, progress: Float) {
        sprite(graphics, BACKGROUND, x, y, 16, 4)
        val width = (progress.coerceIn(0f, 1f) * 17f).toInt()
        if (width > 0) spriteRegion(graphics, PROGRESS, 16, 4, 0, 0, x, y, width, 4)
    }

    private fun sprite(graphics: GuiGraphicsExtractor, id: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        //? if >=1.21.6 {
        graphics.blitSprite(RenderPipelines.CROSSHAIR, id, x, y, width, height)
        //?} else if >=1.21.4 {
        /*graphics.blitSprite(RenderType::crosshair, id, x, y, width, height)
        *///?} else {
        /*graphics.blitSprite(id, x, y, width, height)
        *///?}
    }

    @Suppress("LongParameterList")
    private fun spriteRegion(
        graphics: GuiGraphicsExtractor,
        id: ResourceLocation,
        spriteWidth: Int,
        spriteHeight: Int,
        u: Int,
        v: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        //? if >=1.21.6 {
        graphics.blitSprite(RenderPipelines.CROSSHAIR, id, spriteWidth, spriteHeight, u, v, x, y, width, height)
        //?} else if >=1.21.4 {
        /*graphics.blitSprite(RenderType::crosshair, id, spriteWidth, spriteHeight, u, v, x, y, width, height)
        *///?} else {
        /*graphics.blitSprite(id, spriteWidth, spriteHeight, u, v, x, y, width, height)
        *///?}
    }
}
//?}
