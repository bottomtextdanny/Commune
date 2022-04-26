package net.commune.mod._client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bottomtextdanny.braincell.mod._mod.client_sided.variant_data.VariantRenderingData;
import net.bottomtextdanny.braincell.mod.entity.modules.variable.VariableModule;
import net.bottomtextdanny.braincell.mod.entity.modules.variable.VariantProvider;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * uses variant model and texture automatically.
 */
public abstract class VariantRenderer<T extends Mob & VariantProvider, M extends EntityModel<T>> extends MobRenderer<T, M> {
	private final M defaultModel;

	public VariantRenderer(EntityRendererProvider.Context renderManagerIn, M entityModelIn, float shadowSizeIn) {
		super(renderManagerIn, entityModelIn, shadowSizeIn);
        this.defaultModel = entityModelIn;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void render(T entity, float entityYaw, float tickOffset, PoseStack pose, MultiBufferSource buffer, int packedLightIn) {
		VariableModule module = entity.variableModule();

		if (module.isUpdated() && module.getForm() != null)
            this.model = (M)((VariantRenderingData<T>)module.getForm().getRendering()).getModel(entity);
		else this.model = this.defaultModel;


		super.render(entity, entityYaw, tickOffset, pose, buffer, packedLightIn);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		VariableModule module = entity.variableModule();

		if (module.isUpdated() && module.getForm() != null)
			return ((VariantRenderingData<T>)module.getForm().getRendering()).getTexture(entity);
		return getDefaultEntityTexture(entity);
	}
	
	public abstract ResourceLocation getDefaultEntityTexture(T entity);
}
