package net.commune.mod._client.renderers;

import net.commune.Commune;
import net.commune.mod._client.models.GoblinModel;
import net.commune.mod.content.entities.goblin.Goblin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoblinRenderer extends VariantRenderer<Goblin, GoblinModel> {
    public static final ResourceLocation TEXTURES_PURPLE =
            new ResourceLocation(Commune.ID, "textures/entity/goblin/purple_goblin.png");
    public static final ResourceLocation TEXTURES_GREEN =
            new ResourceLocation(Commune.ID, "textures/entity/goblin/green_goblin.png");
    public static final ResourceLocation TEXTURES_RED =
            new ResourceLocation(Commune.ID, "textures/entity/goblin/red_goblin.png");
    public static final ResourceLocation TEXTURES_BLUE =
            new ResourceLocation(Commune.ID, "textures/entity/goblin/blue_goblin.png");
    public static final GoblinModel MODEL = new GoblinModel();

    public GoblinRenderer(Object manager) {
        this((EntityRendererProvider.Context) manager);
    }

    public GoblinRenderer(EntityRendererProvider.Context manager) {
        super(manager, new GoblinModel(), 0.25F);
        addLayer(new GoblinWeaponRenderingLayer(this));
    }

    @Override
    public ResourceLocation getDefaultEntityTexture(Goblin entity) {
        return TEXTURES_GREEN;
    }
}
