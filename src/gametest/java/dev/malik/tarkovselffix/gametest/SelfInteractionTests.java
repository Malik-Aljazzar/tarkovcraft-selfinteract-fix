package dev.malik.tarkovselffix.gametest;

import com.mojang.authlib.GameProfile;
import dev.malik.tarkovselffix.TarkovSelfInteractFix;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.core.network.message.C2S_RequestInteractionState;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * The server-side self-interaction paths of TarkovCraft Core, run against the real TarkovCraft jar.
 * Without this mod's mixins each self-target test throws "Player and target cannot be the same
 * entity" (verified by running them with the mixins removed).
 *
 * The client-side paths (on-screen hint, interaction screen) need a client and are covered by the
 * manual test in specification/SPEC.md.
 */
@GameTestHolder(TarkovSelfInteractFix.MODID)
@PrefixGameTestTemplate(false)
public class SelfInteractionTests {

    // A FakePlayer instead of GameTestHelper's mock player: the mock player's embedded connection
    // rejects NeoForge's attachment sync payload that TarkovCraft's player data triggers.
    private static FakePlayer player(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "selfinteract_test"));
        player.moveTo(helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        return player;
    }

    private static ServerPlayer sneakingPlayer(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setPose(Pose.CROUCHING);
        if (!player.isCrouching()) helper.fail("Mock player isn't crouching - TarkovCraft's handler would return early");
        return player;
    }

    /** A payload context that only knows its player - the only thing the guarded handler path reads. */
    private static IPayloadContext contextOf(Player player) {
        return (IPayloadContext) Proxy.newProxyInstance(
                IPayloadContext.class.getClassLoader(),
                new Class<?>[]{IPayloadContext.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("player")) return player;
                    throw new UnsupportedOperationException("Test payload context: " + method.getName());
                });
    }

    @GameTest(template = "empty")
    public static void sneakInteractWithSelfDoesNotThrow(GameTestHelper helper) {
        ServerPlayer player = sneakingPlayer(helper);
        PlayerInteractEvent.EntityInteractSpecific event =
                new PlayerInteractEvent.EntityInteractSpecific(player, InteractionHand.MAIN_HAND, player, Vec3.ZERO);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) helper.fail("Self interaction must be left to vanilla, but the event was canceled");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sneakInteractWithOtherEntityStillHandled(GameTestHelper helper) {
        ServerPlayer player = sneakingPlayer(helper);
        var villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, 1, 1, 1);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteractSpecific(player, InteractionHand.MAIN_HAND, villager, Vec3.ZERO));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void interactionRequestForSelfIsDropped(GameTestHelper helper) {
        FakePlayer player = player(helper);
        // The handler resolves the target by entity id, so the player has to be in the level.
        helper.getLevel().addFreshEntity(player);
        try {
            if (helper.getLevel().getEntity(player.getId()) != player) helper.fail("Test player isn't resolvable by its entity id");
            C2S_RequestInteractionState.start(null, player, helper.getLevel().getGameTime()).handleMessage(contextOf(player));
        } finally {
            helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }
}
