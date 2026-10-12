package org.polyfrost.crosshair.render

import net.minecraft.client.Minecraft
//? if >=1.21 {
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.EntityHitResult
//?} else {
/*import net.minecraft.entity.Entity
import net.minecraft.entity.living.LivingEntity
import net.minecraft.entity.living.mob.MobCategory
import net.minecraft.entity.living.player.PlayerEntity as Player
*///?}
import org.polyfrost.crosshair.config.TargetKind

object TargetClassifier {

    fun current(): TargetKind {
        val mc = Minecraft.getInstance()
        val self = mc.player ?: return TargetKind.NORMAL
        //? if >=1.21 {
        val target = (mc.hitResult as? EntityHitResult)?.entity ?: return TargetKind.NORMAL
        //?} else {
        /*val target = mc.crosshairTarget?.entity ?: return TargetKind.NORMAL
        *///?}
        if (target === self || target.isInvisible) return TargetKind.NORMAL
        return classify(self, target)
    }

    private fun classify(self: Player, target: Entity): TargetKind {
        //? if >=1.21 {
        val myTeam = self.team
        val theirTeam = target.team
        val category = target.type.category
        //?} else {
        /*val myTeam = self.scoreboardTeam
        val theirTeam = (target as? LivingEntity)?.scoreboardTeam
        val category = MobCategory.values().firstOrNull { it.type.isInstance(target) }
        *///?}
        if (myTeam != null && theirTeam != null) {
            return if (myTeam == theirTeam) TargetKind.FRIENDLY else TargetKind.ENEMY
        }

        return when (category) {
            MobCategory.MONSTER -> TargetKind.ENEMY
            MobCategory.CREATURE,
            MobCategory.WATER_CREATURE,
            //? if >=1.21
            MobCategory.WATER_AMBIENT,
            MobCategory.AMBIENT,
            -> TargetKind.FRIENDLY

            else -> if (target is Player) TargetKind.ENEMY else TargetKind.NORMAL
        }
    }
}
