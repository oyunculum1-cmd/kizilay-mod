package com.kizilay;

import com.kizilay.registry.ModRegistry;
import com.kizilay.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(KizilAyMod.MODID)
public class KizilAyMod {
    public static final String MODID = "kizilay";

    public KizilAyMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.BLOCKS.register(bus);
        ModRegistry.ITEMS.register(bus);
        ModRegistry.ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);
    }
}
