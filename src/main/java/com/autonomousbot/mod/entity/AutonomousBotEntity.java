package com.autonomousbot.mod.entity;

import com.autonomousbot.mod.goal.BuildShelterGoal;
import com.autonomousbot.mod.goal.ChopTreeGoal;
import com.autonomousbot.mod.goal.MineBlockGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.IronGolemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * A fully local, offline autonomous companion.
 *
 * There is no language model, no API key and no network call involved.
 * Every decision comes from plain Java rules that read the world around the
 * bot (nearby blocks / entities, its own stats) — see the `goal` package.
 * The bot "evolves" by leveling up as it gathers resources: it becomes
 * tougher, builds bigger shelters, and reacts differently over time, all
 * computed locally on your machine.
 */
public class AutonomousBotEntity extends IronGolemEntity {

    // --- Local, offline "brain" state -------------------------------------------------
    // These counters are the whole "memory" of the bot. No file, no server,
    // no external call reads or writes them — they only live in this entity
    // and are saved/loaded with the Minecraft world save (readCustomData /
    // writeCustomData below), exactly like vanilla mob data.
    private int woodCollected = 0;
    private int stoneCollected = 0;
    private int oreCollected = 0;
    private int level = 1;

    public AutonomousBotEntity(EntityType<? extends IronGolemEntity> entityType, World world) {
        super(entityType, world);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0);
    }

    @Override
    protected void initGoals() {
        // Priority is the first number: lower = more urgent.
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new BuildShelterGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.add(3, new ChopTreeGoal(this));
        this.goalSelector.add(4, new MineBlockGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, HostileEntity.class, 10, true, false,
                entity -> entity instanceof HostileEntity));
    }

    @Override
    public void tick() {
        super.tick();
        // Every so often, check whether the bot has gathered enough to level up.
        // This is the "evolves on its own" part: no external decision-maker,
        // just a local threshold check running on the bot's own tick.
        if (!this.getWorld().isClient && this.age % 100 == 0) {
            maybeLevelUp();
        }
    }

    private void maybeLevelUp() {
        int totalResources = woodCollected + stoneCollected + oreCollected;
        int requiredForNextLevel = level * 32;
        if (totalResources >= requiredForNextLevel && level < 10) {
            level++;
            // Getting tougher and faster each level, entirely locally computed.
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .setBaseValue(30.0 + (level - 1) * 5.0);
            this.setHealth((float) this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH));
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    .setBaseValue(4.0 + (level - 1) * 1.0);

            if (this.getWorld() instanceof ServerWorld) {
                this.getWorld().sendEntityStatus(this, (byte) 18); // vanilla "happy villager" particles
            }
            this.sendMessageNearby(Text.literal("[Bot] Niveau " + level + " atteint ! (bois:" + woodCollected
                    + " pierre:" + stoneCollected + " minerai:" + oreCollected + ")"));
        }
    }

    private void sendMessageNearby(Text text) {
        for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (player.squaredDistanceTo(this) < 64 * 64) {
                player.sendMessage(text, false);
            }
        }
    }

    // --- Accessors used by the goal classes --------------------------------------------
    public void addWood(int amount) {
        this.woodCollected += amount;
    }

    public void addStone(int amount) {
        this.stoneCollected += amount;
    }

    public void addOre(int amount) {
        this.oreCollected += amount;
    }

    public int getStoneCollected() {
        return stoneCollected;
    }

    public void spendStone(int amount) {
        this.stoneCollected = Math.max(0, this.stoneCollected - amount);
    }

    public int getLevel() {
        return level;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("WoodCollected", woodCollected);
        nbt.putInt("StoneCollected", stoneCollected);
        nbt.putInt("OreCollected", oreCollected);
        nbt.putInt("BotLevel", level);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.woodCollected = nbt.getInt("WoodCollected");
        this.stoneCollected = nbt.getInt("StoneCollected");
        this.oreCollected = nbt.getInt("OreCollected");
        this.level = Math.max(1, nbt.getInt("BotLevel"));
    }
}
