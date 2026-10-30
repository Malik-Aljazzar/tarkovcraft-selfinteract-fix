package dev.malik.tarkovselffix;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * The one rule every mixin of this mod applies: TarkovCraft's {@code EntityInteraction.Context}
 * rejects {@code player == target} by throwing, so wherever TarkovCraft is about to build one, a
 * target that is the player themselves is treated as "no target".
 *
 * Same identity comparison the Context constructor itself uses ({@code if_acmpne}), so this
 * matches exactly the cases that would have thrown - nothing more.
 */
public final class SelfInteraction {
    private SelfInteraction() {
    }

    public static boolean isSelf(@Nullable Entity player, @Nullable Entity target) {
        return player != null && player == target;
    }
}
