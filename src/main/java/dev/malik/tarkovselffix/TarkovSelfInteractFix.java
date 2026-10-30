package dev.malik.tarkovselffix;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TarkovSelfInteractFix.MODID)
public class TarkovSelfInteractFix {
    public static final String MODID = "tarkov_selfinteract_fix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TarkovSelfInteractFix(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("TarkovCraft Self-Interaction Crash Fix loaded (players are never their own interaction target)");
    }
}
