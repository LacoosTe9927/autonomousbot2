package com.autonomousbot.mod.goal;

import com.autonomousbot.mod.entity.AutonomousBotEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Purely local behavior: scan for stone/ore blocks and mine the nearest one.
 * Same "no external brain" approach as ChopTreeGoal — a fixed rule, not a
 * model call. Only runs once the bot already has some wood (simulating
 * "made a pickaxe"), which is the kind of simple precondition an LLM-free
 * mod uses instead of real tool crafting.
 */
public class MineBlockGoal extends Goal {
    private static final int SEARCH_RADIUS = 10;
    private static final int MINE_TICKS = 50;

    private static final Set<Block> MINEABLE = Set.of(
            Blocks.STONE, Blocks.COBBLESTONE, Blocks.DEEPSLATE,
            Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.DIAMOND_ORE, Blocks.REDSTONE_ORE
    );

    private final AutonomousBotEntity bot;
    private BlockPos targetBlock;
    private int progress;

    public MineBlockGoal(AutonomousBotEntity bot) {
        this.bot = bot;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (bot.getTarget() != null) return false;
        Optional<BlockPos> found = findNearestOre();
        found.ifPresent(pos -> this.targetBlock = pos);
        return found.isPresent();
    }

    @Override
    public boolean shouldContinue() {
        return targetBlock != null
                && bot.getTarget() == null
                && MINEABLE.contains(bot.getWorld().getBlockState(targetBlock).getBlock());
    }

    @Override
    public void start() {
        progress = 0;
    }

    @Override
    public void stop() {
        targetBlock = null;
        progress = 0;
    }

    @Override
    public void tick() {
        if (targetBlock == null) return;
        bot.getNavigation().startMovingTo(
                targetBlock.getX() + 0.5, targetBlock.getY(), targetBlock.getZ() + 0.5, 1.0D);

        double distSq = bot.squaredDistanceTo(
                targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5);
        if (distSq <= 4.5) {
            bot.getNavigation().stop();
            bot.getLookControl().lookAt(
                    targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5);
            progress++;
            if (progress >= MINE_TICKS) {
                World world = bot.getWorld();
                if (world instanceof ServerWorld) {
                    Block block = world.getBlockState(targetBlock).getBlock();
                    if (MINEABLE.contains(block)) {
                        boolean isOre = block != Blocks.STONE && block != Blocks.COBBLESTONE && block != Blocks.DEEPSLATE;
                        world.breakBlock(targetBlock, true, bot);
                        if (isOre) {
                            bot.addOre(1);
                        } else {
                            bot.addStone(1);
                        }
                    }
                }
                stop();
            }
        }
    }

    private Optional<BlockPos> findNearestOre() {
        // Require a little wood first — a simple, local stand-in for "has a tool".
        if (bot.getLevel() < 1) return Optional.empty();

        BlockPos origin = bot.getBlockPos();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.iterate(
                origin.add(-SEARCH_RADIUS, -6, -SEARCH_RADIUS),
                origin.add(SEARCH_RADIUS, 2, SEARCH_RADIUS))) {
            if (MINEABLE.contains(bot.getWorld().getBlockState(pos).getBlock())) {
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
