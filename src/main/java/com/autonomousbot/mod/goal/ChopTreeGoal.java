package com.autonomousbot.mod.goal;

import com.autonomousbot.mod.entity.AutonomousBotEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Purely local behavior: scan a radius around the bot for a log block,
 * walk to it, break it. No AI model involved — just a nearest-block search.
 */
public class ChopTreeGoal extends Goal {
    private static final int SEARCH_RADIUS = 20;
    private static final int BREAK_TICKS = 40; // simulated "chopping time"

    private final AutonomousBotEntity bot;
    private BlockPos targetLog;
    private int progress;

    public ChopTreeGoal(AutonomousBotEntity bot) {
        this.bot = bot;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (bot.getTarget() != null) return false; // fighting takes priority
        Optional<BlockPos> found = findNearestLog();
        found.ifPresent(pos -> this.targetLog = pos);
        return found.isPresent();
    }

    @Override
    public boolean shouldContinue() {
        return targetLog != null
                && bot.getTarget() == null
                && bot.getWorld().getBlockState(targetLog).isIn(BlockTags.LOGS);
    }

    @Override
    public void start() {
        progress = 0;
    }

    @Override
    public void stop() {
        targetLog = null;
        progress = 0;
    }

    @Override
    public void tick() {
        if (targetLog == null) return;
        bot.getNavigation().startMovingTo(
                targetLog.getX() + 0.5, targetLog.getY(), targetLog.getZ() + 0.5, 1.0D);

        double distSq = bot.squaredDistanceTo(
                targetLog.getX() + 0.5, targetLog.getY() + 0.5, targetLog.getZ() + 0.5);
        if (distSq <= 4.5) {
            bot.getNavigation().stop();
            bot.getLookControl().lookAt(
                    targetLog.getX() + 0.5, targetLog.getY() + 0.5, targetLog.getZ() + 0.5);
            progress++;
            if (progress >= BREAK_TICKS) {
                World world = bot.getWorld();
                if (world instanceof ServerWorld serverWorld) {
                    BlockState state = world.getBlockState(targetLog);
                    if (state.isIn(BlockTags.LOGS)) {
                        world.breakBlock(targetLog, true, bot); // drops the log as a normal item
                        bot.addWood(1);
                    }
                }
                stop();
            }
        }
    }

    private Optional<BlockPos> findNearestLog() {
        BlockPos origin = bot.getBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.iterate(
                origin.add(-SEARCH_RADIUS, -4, -SEARCH_RADIUS),
                origin.add(SEARCH_RADIUS, 4, SEARCH_RADIUS))) {
            Block block = bot.getWorld().getBlockState(pos).getBlock();
            if (bot.getWorld().getBlockState(pos).isIn(BlockTags.LOGS)) {
                double d = pos.getSquaredDistance(origin);
                if (d < closestDist) {
                    closestDist = d;
                    closest = pos.toImmutable();
                }
            }
        }
        return Optional.ofNullable(closest);
    }
}
