package com.autonomousbot.mod;

import com.autonomousbot.mod.entity.AutonomousBotEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/**
 * Custom renderer for AutonomousBotEntity. Reuses the vanilla humanoid
 * (zombie) model and texture so the mod needs zero custom art assets.
 */
public class AutonomousBotEntityRenderer extends MobEntityRenderer<AutonomousBotEntity, BipedEntityModel<AutonomousBotEntity>> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/entity/zombie/zombie.png");

    public AutonomousBotEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(AutonomousBotEntity entity) {
        return TEXTURE;
    }
}
