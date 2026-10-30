package dev.malik.tarkovselffix.mixin;

import dev.malik.tarkovselffix.SelfInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.core.api.EntityInteraction;
import tnt.tarkovcraft.core.client.TarkovCraftCoreClient;

import java.util.List;

/**
 * {@code EntityInteractScreen.init} builds an interaction Context from the local player and the
 * entity the screen was opened for, so opening it for the player themselves crashes the client.
 *
 * TarkovCraft 2.11.2 only opens it from the entity interact handler, which
 * TarkovCraftCoreEventHandlerMixin already guards - this closes the screen's single entry point
 * as well, so a future caller can't reopen the same crash.
 */
@Mixin(TarkovCraftCoreClient.class)
public abstract class TarkovCraftCoreClientMixin {

    @Inject(
            method = "openEntityInteractionScreen(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/List;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void dev$neverForSelf(LivingEntity entity, List<EntityInteraction.Type<?>> interactions, CallbackInfo ci) {
        if (SelfInteraction.isSelf(Minecraft.getInstance().player, entity)) {
            ci.cancel();
        }
    }
}
