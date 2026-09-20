package com.autonomousbot.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-only setup. Registers our own custom renderer (see
 * AutonomousBotEntityRenderer), which reuses the vanilla zombie model and
 * texture so the mod needs zero custom art assets to work out of the box.
 */
public class AutonomousBotModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(AutonomousBotMod.AUTONOMOUS_BOT, AutonomousBotEntityRenderer::new);
    }
}
