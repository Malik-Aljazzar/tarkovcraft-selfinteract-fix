package dev.malik.tarkovselffix.mixin;

import dev.malik.tarkovselffix.SelfInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tnt.tarkovcraft.core.client.hint.EntityInteractHint;

/**
 * The crash this mod was written for - see specification/SPEC.md.
 *
 * {@code EntityInteractHint.tick} runs every client tick and builds an interaction Context from
 * {@code Minecraft.crosshairPickEntity}. That entity is picked from the camera, so whenever the
 * camera is detached from the player's body (Exposure camera on a tripod, freecam-style mods)
 * it can be the local player, and the Context constructor throws - every tick.
 *
 * Fix: the hint sees the player in the crosshair as "no entity". The original then takes its own
 * "not a LivingEntity" early return, leaving the hint hidden exactly like with nothing targeted.
 */
@Mixin(EntityInteractHint.class)
public abstract class EntityInteractHintMixin {

    @Redirect(
            method = "tick(Lnet/minecraft/client/Minecraft;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;crosshairPickEntity:Lnet/minecraft/world/entity/Entity;", opcode = Opcodes.GETFIELD),
            remap = false
    )
    private Entity dev$crosshairEntityExceptSelf(Minecraft minecraft) {
        Entity target = minecraft.crosshairPickEntity;
        return SelfInteraction.isSelf(minecraft.player, target) ? null : target;
    }
}
