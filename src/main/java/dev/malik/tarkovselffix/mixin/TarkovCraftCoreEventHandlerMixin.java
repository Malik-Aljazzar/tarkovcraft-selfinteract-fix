package dev.malik.tarkovselffix.mixin;

import dev.malik.tarkovselffix.SelfInteraction;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.core.common.TarkovCraftCoreEventHandler;

/**
 * {@code onEntityInteract} (sneak + right-click on a living entity) builds an interaction Context
 * from the event's player and target, on the client and on the server. With a detached camera the
 * player can right-click their own body; vanilla's ServerboundInteractPacket handling doesn't
 * reject a self target either, so the server-side handler throws too.
 *
 * Fix: skip TarkovCraft's handler for a self target. The event is left as-is (not canceled), so
 * vanilla's own interaction handling continues exactly as it does for entities TarkovCraft has no
 * interactions for.
 */
@Mixin(TarkovCraftCoreEventHandler.class)
public abstract class TarkovCraftCoreEventHandlerMixin {

    @Inject(
            method = "onEntityInteract(Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$EntityInteractSpecific;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void dev$ignoreSelfTarget(PlayerInteractEvent.EntityInteractSpecific event, CallbackInfo ci) {
        if (SelfInteraction.isSelf(event.getEntity(), event.getTarget())) {
            ci.cancel();
        }
    }
}
