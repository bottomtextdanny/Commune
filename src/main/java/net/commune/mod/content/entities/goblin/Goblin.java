package net.commune.mod.content.entities.goblin;

import net.bottomtextdanny.braincell.mod._base.entity.modules.looped_walk.LoopedWalkModule;
import net.bottomtextdanny.braincell.mod._base.entity.modules.variable.*;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.Psyche;
import net.bottomtextdanny.braincell.mod._mod.client_sided.variant_data.SimpleVariantRenderingData;
import net.bottomtextdanny.braincell.mod._mod.client_sided.variant_data.VariantRenderingData;
import net.commune.mod._client.renderers.GoblinRenderer;
import net.commune.mod.content.entities._base.CMPsycheMob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class Goblin extends CMPsycheMob {
    public static final Float ANGRY_SPEED_MULTIPLIER = 1.3F;
    public static final Form<Goblin> GREEN = new Form<>() {
        @OnlyIn(Dist.CLIENT)
        @Override
        protected VariantRenderingData<Goblin> createRenderingHandler() {
            return new SimpleVariantRenderingData<>(
                    GoblinRenderer.TEXTURES_GREEN,
                    GoblinRenderer.MODEL
            );
        }
    };
    public static final Form<Goblin> PURPLE = new Form<>() {
        @OnlyIn(Dist.CLIENT)
        @Override
        protected VariantRenderingData<Goblin> createRenderingHandler() {
            return new SimpleVariantRenderingData<>(
                    GoblinRenderer.TEXTURES_PURPLE,
                    GoblinRenderer.MODEL
            );
        }
    };
    public static final IndexedFormManager FORMS =
            IndexedFormManager.builder()
                    .add(GREEN)
                    .add(PURPLE)
                    .create();

    public Goblin(EntityType<? extends PathfinderMob> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    public static boolean spawningParameters(EntityType<? extends LivingEntity> entityType, LevelAccessor worldIn, MobSpawnType reason, BlockPos pos, Random rand) {
        return worldIn.getDifficulty() != Difficulty.PEACEFUL && worldIn.getLightEmission(pos) < 7;
    }

    @Override
    protected void commonInit() {
        super.commonInit();
        this.loopedWalkModule = new LoopedWalkModule(this);
        this.variableModule = new IndexedVariableModule(this, FORMS);
    }

    @Override
    public Psyche<?> makePsyche() {
        return new GoblinPsyche(this);
    }

    @Override
    public Form<?> chooseVariant() {
        return random.nextBoolean() ? GREEN : PURPLE;
    }

    @Override
    public float getLoopWalkMultiplier() {
        return 0.8F;
    }

    @Override
    public boolean removeWhenFarAway(double v) {
        return true;
    }
}
