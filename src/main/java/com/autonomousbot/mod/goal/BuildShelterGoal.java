package com.autonomousbot.mod.goal;

import com.autonomousbot.mod.entity.AutonomousBotEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Purely local behavior: when night falls (or health is low) and the bot has
 * gathered enough stone, it walls itself in with a small cobblestone box.
 * The decision "should I build now?" is one boolean check against the
 * world's time and the bot's own health — no external reasoning at all.
 */
public class BuildShelterGoal extends Goal {
    private static final int STONE_COST = 12;

    private final AutonomousBotEntity bot;
    private List<BlockPos> wallPositions;
    private int placeIndex;
    private int cooldown;

    public BuildShelterGoal(AutonomousBotEntity bot) {
        this.bot = bot;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        boolean isNight = bot.getWorld().isNight();
        boolean lowHealth = bot.getHealth() < bot.getMaxHealth() * 0.4F;
        boolean hasMaterials = bot.getStoneCollected() >= STONE_COST;
        return (isNight || lowHealth) && hasMaterials && !alreadySheltered();
    }

    @Override
    public boolean shouldContinue() {
        return wallPositions != null && placeIndex < wallPositions.size();
    }

    @Override
    public void start() {
        wallPositions = computeShelterWalls();
        placeIndex = 0;
    }

    @Override
    public void stop() {
        wallPositions = null;
        placeIndex = 0;
        cooldown = 200; // don't spam-build; wait a bit before considering it again
    }

    @Override
    public void tick() {
        if (wallPositions == null || placeIndex >= wallPositions.size()) return;
        BlockPos pos = wallPositions.get(placeIndex);
        World world = bot.getWorld();
        if (world instanceof ServerWorld && world.isAir(pos) && bot.getStoneCollected() > 0) {
            world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
            bot.spendStone(1);
        }
        placeIndex++;
    }

    private boolean alreadySheltered() {
        BlockPos above = bot.getBlockPos().up();
        return !bot.getWorld().isAir(above);
    }

    private List<BlockPos> computeShelterWalls() {
        BlockPos center = bot.getBlockPos();
        List<BlockPos> walls = new ArrayList<>();
        // A simple 3x3 box (walls + roof) around the bot.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                walls.add(center.add(dx, 0, dz));
                walls.add(center.add(dx, 1, dz));
            }
        }
        walls.add(center.up(2)); // roof
        return walls;
    }
}
