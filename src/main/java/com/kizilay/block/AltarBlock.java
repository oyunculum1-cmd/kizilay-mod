package com.kizilay.block;

import com.kizilay.entity.KizilAyKrali;
import com.kizilay.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Sunağa sağ tıklayınca çekici almaya çalışırsın: sunak kaybolur, Kızıl Ay Kralı doğar. */
public class AltarBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public AltarBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            player.displayClientMessage(Component.translatable("message.kizilay.peaceful"), true);
            return InteractionResult.SUCCESS;
        }
        ServerLevel sl = (ServerLevel) level;
        Vec3 c = Vec3.atBottomCenterOf(pos);
        // çekici kapmaya çalışan oyuncu hafifçe geri savrulur
        Vec3 away = player.position().subtract(c).multiply(1, 0, 1);
        if (away.lengthSqr() < 1.0E-4) {
            away = new Vec3(1, 0, 0);
        }
        away = away.normalize();
        player.setDeltaMovement(away.x * 1.1, 0.45, away.z * 1.1);
        player.hurtMarked = true;

        level.removeBlock(pos, false);
        KizilAyKrali boss = ModRegistry.KRALI.get().create(sl);
        if (boss != null) {
            float yaw = (float) (Math.atan2(-away.x, away.z) * 180.0 / Math.PI) + 180.0F;
            boss.moveTo(c.x, c.y, c.z, yaw, 0.0F);
            boss.finalizeSpawn(sl, sl.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
            sl.addFreshEntity(boss);
        }
        return InteractionResult.CONSUME;
    }
}
