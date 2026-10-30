package dev.malik.tarkovselffix.mixin;

import dev.malik.tarkovselffix.SelfInteraction;
import dev.malik.tarkovselffix.TarkovSelfInteractFix;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.core.network.message.C2S_RequestInteractionState;

/**
 * The server-side handler of TarkovCraft's interaction request packet looks up the target by the
 * entity id the client sent and builds an interaction Context with the sending player. A client
 * sending its own entity id (a modified client, or any future client path that lets the player
 * target themselves) makes the handler throw on the server.
 *
 * Fix: drop such a request before TarkovCraft looks at it - the same thing the handler already
 * does for ids that don't resolve to a living entity.
 */
@Mixin(C2S_RequestInteractionState.class)
public abstract class RequestInteractionStateMixin {

    @Shadow(remap = false)
    @Final
    private int entityId;

    @Inject(
            method = "handleMessage(Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void dev$dropSelfTarget(IPayloadContext context, CallbackInfo ci) {
        Player player = context.player();
        if (player != null && SelfInteraction.isSelf(player, player.level().getEntity(entityId))) {
            TarkovSelfInteractFix.LOGGER.debug("Dropped TarkovCraft interaction request of {} targeting themselves", player.getGameProfile().getName());
            ci.cancel();
        }
    }
}
