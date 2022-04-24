package net.commune.mod._client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bottomtextdanny.braincell.Braincell;
import net.bottomtextdanny.braincell.mod._base.animation.EntityModelAnimator;
import net.bottomtextdanny.braincell.mod._base.animation.interpreter.AnimationInterpreter;
import net.bottomtextdanny.braincell.mod._base.rendering.core_modeling.BCJoint;
import net.bottomtextdanny.braincell.mod.rendering.modeling.BCEntityModel;
import net.commune.Commune;
import net.commune.mod.content.entities.goblin.Goblin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GoblinModel extends BCEntityModel<Goblin> {
    private final BCJoint root;
    private final BCJoint body;
    private final BCJoint head;
    private final BCJoint rightEar;
    private final BCJoint leftEar;
    private final BCJoint rightarm;
    private final BCJoint leftarm;
    private final BCJoint leftleg;
    private final BCJoint rightleg;
    private final EntityModelAnimator animator = new EntityModelAnimator(this, 0.0F);
    private final AnimationInterpreter walk = Braincell.client().getAnimationManager()
            .makeInterpreter(new ResourceLocation(Commune.ID, "goblin/walk"), this);
    private final AnimationInterpreter throwAttack = Braincell.client().getAnimationManager()
            .makeInterpreter(new ResourceLocation(Commune.ID, "goblin/throw"), this);
    private final AnimationInterpreter bumpAttack = Braincell.client().getAnimationManager()
            .makeInterpreter(new ResourceLocation(Commune.ID, "goblin/bump"), this);

    public GoblinModel() {
        texWidth = 32;
        texHeight = 32;

        root = new BCJoint(this, "root");
        root.setPosCore(0.0F, 24.0F, 0.0F);

        body = new BCJoint(this, "body");
        body.setPosCore(0.0F, -4.0F, 0.0F);
        root.addChild(body);
        body.uvOffset(12, 15).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F, 0.0F, false);
        body.uvOffset(0, 11).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 4.0F, 4.0F, 0.025F, false);

        head = new BCJoint(this, "head");
        head.setPosCore(0.0F, -3.0F, 0.0F);
        body.addChild(head);
        head.uvOffset(0, 0).addBox(-4.0F, -6.0F, -2.5F, 8.0F, 6.0F, 5.0F, 0.0F, false);

        rightEar = new BCJoint(this, "rightEar");
        rightEar.setPosCore(0.0F, -3.5F, 0.0F);
        head.addChild(rightEar);
        rightEar.uvOffset(0, 19).addBox(4.0F, -1.5F, 0.0F, 6.0F, 3.0F, 0.0F, 0.0F, false);

        leftEar = new BCJoint(this, "leftEar");
        leftEar.setPosCore(-4.0F, -3.5F, 0.0F);
        head.addChild(leftEar);
        leftEar.uvOffset(12, 11).addBox(-6.0F, -1.5F, 0.0F, 6.0F, 3.0F, 0.0F, 0.0F, false);

        rightarm = new BCJoint(this, "rightarm");
        rightarm.setPosCore(2.0F, -2.5F, 0.0F);
        body.addChild(rightarm);
        setRotationAngle(rightarm, 0.0F, 0.0F, -2.3562F);
        rightarm.uvOffset(12, 22).addBox(-4.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, 0.0F, false);

        leftarm = new BCJoint(this, "leftarm");
        leftarm.setPosCore(-2.0F, -2.5F, 0.0F);
        body.addChild(leftarm);
        setRotationAngle(leftarm, 0.0F, 0.0F, -0.7854F);
        leftarm.uvOffset(12, 22).addBox(-4.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, 0.0F, false);

        leftleg = new BCJoint(this, "leftleg");
        leftleg.setPosCore(-1.5F, -4.0F, -2.0F);
        leftleg.uvOffset(0, 22).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 2.0F, 0.0F, false);
        root.addChild(leftleg);

        rightleg = new BCJoint(this, "rightleg");
        rightleg.setPosCore(1.5F, -4.0F, 0.0F);
        root.addChild(rightleg);
        rightleg.uvOffset(0, 22).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 4.0F, 2.0F, 0.0F, false);
    }

    @Override
    public void handleRotations(Goblin entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
        super.handleRotations(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
        this.head.xRot = Mth.clamp(headPitch, -50, 50) * RAD;
        this.head.yRot = Mth.clamp(headYaw, -70, 50) * RAD;
    }

    @Override
    public void handleKeyframedAnimations(Goblin entity, float limbSwing, float limbSwingAmount, float headYaw, float headPitch) {
        float easedLimbSwing = Mth.clamp(caculateLimbSwingEasing(entity), 0.0F, 0.999F);

        if (easedLimbSwing > 0.0F) {
            float easedlimbSwingAmount = caculateLimbSwingAmountEasing(entity);
            float walkMult = Mth.clamp(easedlimbSwingAmount * 12.0F, 0.0F, 1.0F);

            animator.setTimer(easedLimbSwing * 20.0F);
            animator.multiplier(walkMult);
            walk.run(animator);
        }

        animator.multiplier(1.0F);

        if (entity.mainHandler.isPlayingNull()) return;


        animator.setTimer(entity.mainHandler.dynamicProgress());

        if (entity.mainHandler.isPlaying(Goblin.THROW)) {
            throwAttack.run(animator);
        } else if (entity.mainHandler.isPlaying(Goblin.BUMP)) {
            bumpAttack.run(animator);
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        root.render(matrixStack, buffer, packedLight, packedOverlay);
    }
}
