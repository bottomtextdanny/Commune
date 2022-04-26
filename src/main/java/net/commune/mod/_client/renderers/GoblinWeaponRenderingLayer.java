package net.commune.mod._client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.bottomtextdanny.braincell.mod._base.rendering.core_modeling.BCJoint;
import net.commune.Commune;
import net.commune.mod._client.models.GoblinModel;
import net.commune.mod._client.models.GoblinWeaponModel;
import net.commune.mod.content.entities.goblin.Goblin;
import net.commune.mod.content.entities.goblin.GoblinWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class GoblinWeaponRenderingLayer extends RenderLayer<Goblin, GoblinModel> {
    public static final ResourceLocation TEXTURES_SPEAR =
            new ResourceLocation(Commune.ID, "textures/entity/goblin/goblin_spear.png");
    public static final ResourceLocation TEXTURES_SWORD =
            new ResourceLocation(Commune.ID, "textures/entity/goblin/goblin_sword.png");
    private GoblinWeaponModel model;

    public GoblinWeaponRenderingLayer(RenderLayerParent<Goblin, GoblinModel> parent) {
        super(parent);
        this.model = new GoblinWeaponModel();
    }

    public void render(PoseStack pose, MultiBufferSource buffer, int packedLight,
                       Goblin entity, float limbSwing, float limbSwingAmount,
                       float tickOffset, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isInvisible()) {

            if (entity.hasLeftWeapon()) {
                renderWeapon(pose, buffer, packedLight, entity, true);
            }

            if (entity.hasRightWeapon()) {
                renderWeapon(pose, buffer, packedLight, entity, false);
            }
        }
    }

    private void renderWeapon(PoseStack pose, MultiBufferSource buffer,
                              int packedLight, Goblin entity, boolean leftHand) {
        GoblinWeapon weapon = leftHand ? entity.getLeftWeapon() : entity.getRightWeapon();
        BCJoint joint = leftHand ? getParentModel().leftHandling : getParentModel().rightHandlingRot;

        VertexConsumer vertexBuffer = buffer.getBuffer(this.model.renderType(weapon.getTexture()));

        pose.pushPose();
        joint.translateRotateWithParents(pose);
        model.renderToBuffer(pose, vertexBuffer,
                packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        buffer.getBuffer(RenderType.entitySolid(weapon.getTexture()));

        pose.popPose();
    }
}
