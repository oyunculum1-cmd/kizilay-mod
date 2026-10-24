package com.kizilay.registry;

import com.kizilay.KizilAyMod;
import com.kizilay.block.AltarBlock;
import com.kizilay.entity.KizilAyKrali;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, KizilAyMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KizilAyMod.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KizilAyMod.MODID);

    public static final RegistryObject<Block> MOON_ALTAR = BLOCKS.register("moon_altar",
            () -> new AltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .strength(8.0F, 1200.0F).lightLevel(s -> 9).noOcclusion()));

    public static final RegistryObject<Item> MOON_ALTAR_ITEM = ITEMS.register("moon_altar",
            () -> new BlockItem(MOON_ALTAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> MOON_SHARD = ITEMS.register("moon_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<EntityType<KizilAyKrali>> KRALI = ENTITIES.register("kizil_ay_krali",
            () -> EntityType.Builder.of(KizilAyKrali::new, MobCategory.MONSTER)
                    .sized(2.0F, 4.6F).clientTrackingRange(12).fireImmune().build("kizil_ay_krali"));

    public static final RegistryObject<Item> KRALI_EGG = ITEMS.register("kizil_ay_krali_spawn_egg",
            () -> new ForgeSpawnEggItem(KRALI, 0x1A0A14, 0xD4AA28, new Item.Properties()));
}
