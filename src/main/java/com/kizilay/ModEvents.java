package com.kizilay;

import com.kizilay.entity.KizilAyKrali;
import com.kizilay.registry.ModRegistry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KizilAyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModRegistry.KRALI.get(), KizilAyKrali.createAttributes().build());
    }

    @SubscribeEvent
    public static void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            e.accept(ModRegistry.KRALI_EGG);
        } else if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            e.accept(ModRegistry.MOON_ALTAR_ITEM);
        } else if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            e.accept(ModRegistry.MOON_SHARD);
        }
    }
}
