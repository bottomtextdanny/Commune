package net.commune.mod._client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bottomtextdanny.braincell.mod._base.rendering.core_modeling.BCJoint;
import net.bottomtextdanny.braincell.mod.rendering.BCRenderTypes;
import net.bottomtextdanny.braincell.mod.rendering.modeling.BCSimpleModel;
import net.minecraft.client.renderer.RenderType;

public class GoblinWeaponModel extends BCSimpleModel {
    private final BCJoint root;

    public GoblinWeaponModel() {
        super(BCRenderTypes::getFlatShading);
        texWidth = 64;
        texHeight = 64;

        root = new BCJoint(this);
        root.setPosCore(0.0F, 0.0F, 0.0F);
        root.uvOffset(0, 0).addBox(0.0F, -15.5F, -16.0F, 0.0F, 32.0F, 32.0F, 0.0F, false);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        root.render(matrixStack, buffer, packedLight, packedOverlay);
    }
}
