package com.autonomousbot.mod;

import com.autonomousbot.mod.entity.AutonomousBotEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Autonomous Bot — a fully local, offline Minecraft companion.
 *
 * There is NO external AI, NO API key and NO network call anywhere in this mod.
 * All decision-making happens inside {@link AutonomousBotEntity} and the goal
 * classes in the `goal` package, using plain Java logic that reads the
 * surrounding Minecraft world (nearby blocks, entities, its own inventory,
 * health, time of day) and picks the next action from a fixed priority list.
 * "Evolving" here means its behavior naturally changes as its situation
 * changes (better tools found -> mines faster, night falls -> takes shelter,
 * a monster appears -> fights or flees, hunger drops -> eats), not that it
 * calls out to a language model.
 */
public class AutonomousBotMod implements ModInitializer {
    public static final String MOD_ID = "autonomousbot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final EntityType<AutonomousBotEntity> AUTONOMOUS_BOT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "autonomous_bot"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, AutonomousBotEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .trackRangeChunks(10)
                    .build()
    );

    public static final Item AUTONOMOUS_BOT_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "autonomous_bot_spawn_egg"),
            new SpawnEggItem(AUTONOMOUS_BOT, new Item.Settings())
    );

    @Override
    public void onInitialize() {
        LOGGER.info("[AutonomousBot] Initializing — fully offline AI, no API key required.");

        FabricDefaultAttributeRegistry.register(AUTONOMOUS_BOT, AutonomousBotEntity.createAttributes());
    }
}
