package com.autonomousbot.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.IronGolemEntityRenderer;

/**
 * Client-only setup. We deliberately reuse the vanilla Iron Golem model and
 * texture so the mod needs zero custom art assets to work out of the box.
 * If you want your own look, replace this renderer with a custom one and
 * add your texture under assets/autonomousbot/textures/entity/.
 */
public class AutonomousBotModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(AutonomousBotMod.AUTONOMOUS_BOT, IronGolemEntityRenderer::new);
    }
}
