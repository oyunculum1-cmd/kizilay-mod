package com.kizilay.entity;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Kızıl Ay Kralı. Faz 1: tırpan (hilal / sıçrayış / girdap). Faz 2 (mavi): çekiç (ezme / süpürme / deprem).
 * Süreler tick cinsinden (20 tick = 1 sn) ve Blockbench animasyon uzunluklarıyla aynıdır.
 */
public class KizilAyKrali extends Monster implements GeoEntity {
    public static final int S_SPAWN = 0, S_IDLE = 1, S_CRESCENT = 2, S_LEAP = 3, S_WHIRL = 4, S_TRANSFORM = 5,
            S_DRAW = 6, S_SMASH = 7, S_SWEEP = 8, S_QUAKE = 9;

    private static final int SPAWN_LEN = 220, CRESCENT_LEN = 64, LEAP_LEN = 80, WHIRL_LEN = 72,
            TRANSFORM_LEN = 210, DRAW_LEN = 48, SMASH_LEN = 72, SWEEP_LEN = 76, QUAKE_LEN = 88, DEATH_LEN = 140;

    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(KizilAyKrali.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(KizilAyKrali.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    private int stateTicks = 0;
    private int cooldown = 40;
    private Vec3 leapFrom = Vec3.ZERO;
    private Vec3 leapTo = Vec3.ZERO;
    private DamageSource pendingLootSource = null;

    public KizilAyKrali(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // ---------------------------------------------------------------- veri
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, S_SPAWN);
        this.entityData.define(PHASE, 1);
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    private void setState(int s) {
        this.entityData.set(STATE, s);
        this.stateTicks = 0;
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(PHASE, Math.max(1, tag.getInt("Phase")));
        this.entityData.set(STATE, S_IDLE);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    // ---------------------------------------------------------------- hedef / hareket
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new ChaseGoal());
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private class ChaseGoal extends Goal {
        ChaseGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return getState() == S_IDLE && getTarget() != null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) {
                return;
            }
            double d = distanceTo(t);
            if (d > (getPhase() >= 2 ? 6.0 : 5.0)) {
                getNavigation().moveTo(t, 1.0D);
            } else {
                getNavigation().stop();
            }
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        int s = getState();
        if ((s == S_SPAWN || s == S_TRANSFORM) && !source.is(DamageTypes.GENERIC_KILL) && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    // ---------------------------------------------------------------- ana mantık (sunucu)
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setColor(getPhase() >= 2 ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.RED);
        tickState();
    }

    private void faceTarget(float step) {
        LivingEntity t = getTarget();
        if (t == null) {
            return;
        }
        double dx = t.getX() - getX();
        double dz = t.getZ() - getZ();
        float want = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float y = Mth.approachDegrees(getYRot(), want, step);
        setYRot(y);
        this.yBodyRot = y;
        this.yHeadRot = y;
    }

    private void tickState() {
        int st = getState();
        stateTicks++;
        LivingEntity t = getTarget();
        float dmg = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);

        switch (st) {
            case S_SPAWN -> {
                getNavigation().stop();
                if (stateTicks == 34) {
                    hitRing(0, 9, dmg * 0.6F, 1.6);
                    burst(ParticleTypes.EXPLOSION, 20);
                }
                if (stateTicks >= SPAWN_LEN) {
                    setState(S_IDLE);
                }
            }
            case S_IDLE -> {
                if (t == null) {
                    return;
                }
                faceTarget(12.0F);
                if (getPhase() == 1 && getHealth() <= getMaxHealth() * 0.5F) {
                    getNavigation().stop();
                    setState(S_TRANSFORM);
                    return;
                }
                if (--cooldown > 0) {
                    return;
                }
                double d = distanceTo(t);
                int r = random.nextInt(100);
                if (getPhase() == 1) {
                    if (d <= 8.0) {
                        startAttack(r < 55 ? S_CRESCENT : S_WHIRL);
                    } else if (d <= 24.0) {
                        startAttack(S_LEAP);
                    }
                } else {
                    if (d <= 10.0) {
                        startAttack(r < 55 ? S_SMASH : S_SWEEP);
                    } else if (d <= 24.0) {
                        startAttack(r < 60 ? S_QUAKE : S_SMASH);
                    }
                }
            }
            case S_CRESCENT -> {
                getNavigation().stop();
                if (stateTicks < 12) {
                    faceTarget(25.0F);
                }
                if (stateTicks == 14) {
                    hitCone(8.0, 80.0, dmg, 1.0);
                    sweepFx();
                } else if (stateTicks == 28) {
                    hitCone(9.0, 90.0, dmg * 1.2F, 1.5);
                    sweepFx();
                }
                if (stateTicks >= CRESCENT_LEN) {
                    endAttack(30, 50);
                }
            }
            case S_LEAP -> tickLeap(dmg, 14, 37, LEAP_LEN, 6.5, 1.3F, 1.5, 20.0);
            case S_WHIRL -> {
                if (stateTicks < 15) {
                    faceTarget(25.0F);
                    getNavigation().stop();
                }
                if (stateTicks >= 17 && stateTicks <= 49) {
                    if (t != null) {
                        getNavigation().moveTo(t, 0.9D);
                    }
                    if ((stateTicks - 17) % 8 == 0) {
                        hitRing(0, 7.0, dmg * 0.6F, 0.9);
                        burst(ParticleTypes.SWEEP_ATTACK, 4);
                    }
                } else {
                    getNavigation().stop();
                }
                if (stateTicks >= WHIRL_LEN) {
                    endAttack(30, 50);
                }
            }
            case S_TRANSFORM -> {
                getNavigation().stop();
                if (stateTicks == 103) {
                    this.entityData.set(PHASE, 2);
                    burst(ParticleTypes.SOUL_FIRE_FLAME, 60);
                }
                if (stateTicks == 115) {
                    hitRing(0, 10.0, dmg * 0.4F, 2.2);
                    burst(ParticleTypes.EXPLOSION_EMITTER, 1);
                }
                if (stateTicks >= TRANSFORM_LEN) {
                    setState(S_DRAW);
                }
            }
            case S_DRAW -> {
                getNavigation().stop();
                if (stateTicks >= DRAW_LEN) {
                    endAttack(20, 40);
                }
            }
            case S_SMASH -> {
                getNavigation().stop();
                if (stateTicks < 18) {
                    faceTarget(25.0F);
                }
                if (stateTicks == 24) {
                    hitPoint(4.5, 5.5, dmg * 1.6F, 1.8);
                    burst(ParticleTypes.EXPLOSION, 6);
                } else if (stateTicks == 27) {
                    hitRing(3.0, 8.0, dmg * 0.5F, 0.9);
                }
                if (stateTicks >= SMASH_LEN) {
                    endAttack(20, 40);
                }
            }
            case S_SWEEP -> {
                getNavigation().stop();
                if (stateTicks < 16) {
                    faceTarget(25.0F);
                }
                if (stateTicks == 19) {
                    hitCone(10.0, 95.0, dmg, 1.4);
                    sweepFx();
                } else if (stateTicks == 39) {
                    hitCone(10.5, 100.0, dmg * 1.1F, 2.0);
                    sweepFx();
                }
                if (stateTicks >= SWEEP_LEN) {
                    endAttack(20, 40);
                }
            }
            case S_QUAKE -> {
                tickLeap(dmg, 14, 39, QUAKE_LEN, 6.0, 1.5F, 1.8, 14.0);
                if (stateTicks == 45) {
                    hitRing(6.0, 10.0, dmg, 1.2);
                } else if (stateTicks == 51) {
                    hitRing(10.0, 14.0, dmg * 0.8F, 1.0);
                }
            }
            default -> setState(S_IDLE);
        }
    }

    private void startAttack(int s) {
        setState(s);
    }

    private void endAttack(int min, int max) {
        setState(S_IDLE);
        cooldown = min + random.nextInt(Math.max(1, max - min));
    }

    /** Sıçrayış: jumpTick'te kalkar, landTick'te iner ve dalga yayar. */
    private void tickLeap(float dmg, int jumpTick, int landTick, int len, double radius, float mult, double kb, double maxJump) {
        LivingEntity t = getTarget();
        if (stateTicks < jumpTick) {
            getNavigation().stop();
            faceTarget(25.0F);
        }
        if (stateTicks == jumpTick) {
            leapFrom = position();
            Vec3 to = t != null ? t.position() : position();
            Vec3 d = to.subtract(leapFrom).multiply(1, 0, 1);
            if (d.length() > maxJump) {
                d = d.normalize().scale(maxJump);
            }
            leapTo = leapFrom.add(d);
            setNoGravity(true);
        }
        if (stateTicks > jumpTick && stateTicks <= landTick) {
            double f = (stateTicks - jumpTick) / (double) (landTick - jumpTick);
            double h = 5.0 * Math.sin(Math.PI * f);
            Vec3 want = new Vec3(Mth.lerp(f, leapFrom.x, leapTo.x), leapFrom.y + h, Mth.lerp(f, leapFrom.z, leapTo.z));
            Vec3 delta = want.subtract(position());
            move(MoverType.SELF, delta);
            if (stateTicks == landTick) {
                setNoGravity(false);
                hitRing(0, radius, dmg * mult, kb);
                burst(ParticleTypes.EXPLOSION, 8);
            }
        }
        if (stateTicks >= len) {
            setNoGravity(false);
            endAttack(30, 50);
        }
    }

    // ---------------------------------------------------------------- hasar yardımcıları
    private List<LivingEntity> near(double r) {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r, 3.0, r),
                e -> e != this && e.isAlive() && !(e instanceof KizilAyKrali));
    }

    private void damage(LivingEntity e, float amount, double kb) {
        e.hurt(damageSources().mobAttack(this), amount);
        Vec3 dir = e.position().subtract(position()).multiply(1, 0, 1);
        dir = dir.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : dir.normalize();
        e.setDeltaMovement(e.getDeltaMovement().add(dir.x * kb, 0.35 + kb * 0.15, dir.z * kb));
        e.hurtMarked = true;
    }

    private void hitRing(double inner, double outer, float amount, double kb) {
        for (LivingEntity e : near(outer)) {
            double d = e.position().subtract(position()).multiply(1, 0, 1).length();
            if (d >= inner && d <= outer) {
                damage(e, amount, kb);
            }
        }
    }

    private void hitCone(double range, double halfAngleDeg, float amount, double kb) {
        Vec3 look = Vec3.directionFromRotation(0, getYRot()).multiply(1, 0, 1).normalize();
        double cos = Math.cos(Math.toRadians(halfAngleDeg));
        for (LivingEntity e : near(range)) {
            Vec3 to = e.position().subtract(position()).multiply(1, 0, 1);
            double d = to.length();
            if (d <= range && (d < 1.5 || to.normalize().dot(look) >= cos)) {
                damage(e, amount, kb);
            }
        }
    }

    private void hitPoint(double forward, double radius, float amount, double kb) {
        Vec3 look = Vec3.directionFromRotation(0, getYRot()).multiply(1, 0, 1).normalize();
        Vec3 p = position().add(look.scale(forward));
        for (LivingEntity e : near(forward + radius)) {
            if (e.position().subtract(p).multiply(1, 0, 1).length() <= radius) {
                damage(e, amount, kb);
            }
        }
    }

    private void burst(net.minecraft.core.particles.ParticleOptions p, int count) {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(p, getX(), getY() + 0.3, getZ(), count, 3.0, 0.3, 3.0, 0.05);
        }
    }

    private void sweepFx() {
        Vec3 look = Vec3.directionFromRotation(0, getYRot()).multiply(1, 0, 1).normalize().scale(3.5);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + look.x, getY() + 1.5, getZ() + look.z, 3, 1.5, 0.5, 1.5, 0.0);
        }
    }

    // ---------------------------------------------------------------- ölüm
    @Override
    protected void dropAllDeathLoot(DamageSource source) {
        // ganimet çekiç yere düştükten sonra, ölüm animasyonunun sonunda düşer
        this.pendingLootSource = source;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        getNavigation().stop();
        this.lastHurtByPlayerTime = Math.max(this.lastHurtByPlayerTime, 100);
        if (this.deathTime >= DEATH_LEN && !level().isClientSide() && !isRemoved()) {
            if (this.pendingLootSource != null) {
                super.dropAllDeathLoot(this.pendingLootSource);
                this.pendingLootSource = null;
            }
            level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    // ---------------------------------------------------------------- GeckoLib
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<KizilAyKrali> c = new AnimationController<>(this, "main", 4, this::predicate);
        c.setSoundKeyframeHandler(ev -> {
            String name = ev.getKeyframeData().getSound();
            if (name == null || name.isEmpty()) {
                return;
            }
            ResourceLocation id = name.contains(":") ? new ResourceLocation(name) : new ResourceLocation("kizilay", name);
            SoundEvent snd = ForgeRegistries.SOUND_EVENTS.getValue(id);
            KizilAyKrali e = ev.getAnimatable();
            if (snd != null && e.level().isClientSide) {
                e.level().playLocalSound(e.getX(), e.getY(), e.getZ(), snd, SoundSource.HOSTILE, 3.0F, 1.0F, false);
            }
        });
        controllers.add(c);
    }

    private PlayState predicate(AnimationState<KizilAyKrali> st) {
        if (isDeadOrDying()) {
            return st.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
        }
        boolean p2 = getPhase() >= 2;
        String anim;
        boolean loop = false;
        boolean hold = false;
        switch (getState()) {
            case S_SPAWN -> { anim = "spawn"; hold = true; }
            case S_CRESCENT -> anim = "attack_crescent";
            case S_LEAP -> anim = "attack_leap";
            case S_WHIRL -> anim = "attack_whirl";
            case S_TRANSFORM -> { anim = "phase2_transform"; hold = true; }
            case S_DRAW -> anim = "draw_hammer";
            case S_SMASH -> anim = "hammer_smash";
            case S_SWEEP -> anim = "hammer_sweep";
            case S_QUAKE -> anim = "hammer_quake";
            default -> {
                loop = true;
                // not: çekiçli yürüme animasyonu henüz yok, faz 2'de boşta animasyonu kullanılır
                anim = p2 ? "idle_hammer" : (st.isMoving() ? "walk" : "idle");
            }
        }
        RawAnimation ra = RawAnimation.begin();
        if (loop) {
            ra = ra.thenLoop(anim);
        } else if (hold) {
            ra = ra.thenPlayAndHold(anim);
        } else {
            ra = ra.thenPlay(anim);
        }
        return st.setAndContinue(ra);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
