package com.kizilay.registry;

import com.kizilay.KizilAyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, KizilAyMod.MODID);

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(KizilAyMod.MODID, name)));
    }

    public static final RegistryObject<SoundEvent> BOSS_DESCEND = reg("boss.descend");
    public static final RegistryObject<SoundEvent> BOSS_LAND = reg("boss.land");
    public static final RegistryObject<SoundEvent> BOSS_SHOCKWAVE = reg("boss.shockwave");
    public static final RegistryObject<SoundEvent> HAMMER_CHARGE = reg("hammer.charge");
    public static final RegistryObject<SoundEvent> HAMMER_GRAB = reg("hammer.grab");
    public static final RegistryObject<SoundEvent> HAMMER_YANK = reg("hammer.yank");
    public static final RegistryObject<SoundEvent> CRYSTAL_ABSORB = reg("crystal.absorb");
    public static final RegistryObject<SoundEvent> FLAME_OUT = reg("flame.out");
    public static final RegistryObject<SoundEvent> HAMMER_SWING = reg("hammer.swing");
    public static final RegistryObject<SoundEvent> HAMMER_DOCK = reg("hammer.dock");
    public static final RegistryObject<SoundEvent> BOSS_ROAR = reg("boss.roar");
    public static final RegistryObject<SoundEvent> SCYTHE_SLAM = reg("scythe.slam");
    public static final RegistryObject<SoundEvent> SCYTHE_POINT = reg("scythe.point");
    public static final RegistryObject<SoundEvent> ALTAR_HUM = reg("altar.hum");
    public static final RegistryObject<SoundEvent> BOSS_BREATH = reg("boss.breath");
    public static final RegistryObject<SoundEvent> BOSS_STEP = reg("boss.step");
    public static final RegistryObject<SoundEvent> ATTACK_WINDUP = reg("attack.windup");
    public static final RegistryObject<SoundEvent> ATTACK_SLASH = reg("attack.slash");
    public static final RegistryObject<SoundEvent> ATTACK_SLASH2 = reg("attack.slash2");
    public static final RegistryObject<SoundEvent> ATTACK_CROUCH = reg("attack.crouch");
    public static final RegistryObject<SoundEvent> ATTACK_LEAP = reg("attack.leap");
    public static final RegistryObject<SoundEvent> ATTACK_LEAP_CHARGE = reg("attack.leap_charge");
    public static final RegistryObject<SoundEvent> ATTACK_SLAM = reg("attack.slam");
    public static final RegistryObject<SoundEvent> ATTACK_SPIKES = reg("attack.spikes");
    public static final RegistryObject<SoundEvent> ATTACK_WHIRL = reg("attack.whirl");
    public static final RegistryObject<SoundEvent> ATTACK_WHIRL_END = reg("attack.whirl_end");
    public static final RegistryObject<SoundEvent> PHASE_GROAN = reg("phase.groan");
    public static final RegistryObject<SoundEvent> PHASE_COLLAPSE = reg("phase.collapse");
    public static final RegistryObject<SoundEvent> SCYTHE_DROP = reg("scythe.drop");
    public static final RegistryObject<SoundEvent> RING_DETACH = reg("ring.detach");
    public static final RegistryObject<SoundEvent> RING_RISE = reg("ring.rise");
    public static final RegistryObject<SoundEvent> PHASE_TRANSFORM = reg("phase.transform");
    public static final RegistryObject<SoundEvent> RING_SHATTER = reg("ring.shatter");
    public static final RegistryObject<SoundEvent> PHASE_ROAR = reg("phase.roar");
    public static final RegistryObject<SoundEvent> ALTAR_SINK = reg("altar.sink");
    public static final RegistryObject<SoundEvent> HAMMER_PULL = reg("hammer.pull");
    public static final RegistryObject<SoundEvent> HAMMER_READY = reg("hammer.ready");
    public static final RegistryObject<SoundEvent> HAMMER_WINDUP = reg("hammer.windup");
    public static final RegistryObject<SoundEvent> HAMMER_SWING_H = reg("hammer.swing_h");
    public static final RegistryObject<SoundEvent> HAMMER_SMASH = reg("hammer.smash");
    public static final RegistryObject<SoundEvent> QUAKE_RING = reg("quake.ring");
    public static final RegistryObject<SoundEvent> HAMMER_DROP = reg("hammer.drop");
    public static final RegistryObject<SoundEvent> DEATH_FALL = reg("death.fall");
    public static final RegistryObject<SoundEvent> DEATH_DISSOLVE = reg("death.dissolve");
}
