package net.commune.mod.content.entities.goblin;

import net.bottomtextdanny.braincell.base.scheduler.IntScheduler;
import net.bottomtextdanny.braincell.mod._base.entity.modules.animatable.AnimationArray;
import net.bottomtextdanny.braincell.mod._base.entity.modules.animatable.AnimationGetter;
import net.bottomtextdanny.braincell.mod._base.entity.modules.data_manager.BCDataManager;
import net.bottomtextdanny.braincell.mod._base.entity.modules.looped_walk.LoopedWalkModule;
import net.bottomtextdanny.braincell.mod._base.entity.modules.variable.*;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.Psyche;
import net.bottomtextdanny.braincell.mod._base.serialization.builtin.BuiltinSerializers;
import net.bottomtextdanny.braincell.mod._mod.client_sided.variant_data.SimpleVariantRenderingData;
import net.bottomtextdanny.braincell.mod._mod.client_sided.variant_data.VariantRenderingData;
import net.bottomtextdanny.braincell.mod.entity.modules.animatable.SimpleAnimation;
import net.bottomtextdanny.braincell.mod.entity.serialization.EntityData;
import net.bottomtextdanny.braincell.mod.entity.serialization.EntityDataReference;
import net.bottomtextdanny.braincell.mod.entity.serialization.RawEntityDataReference;
import net.bottomtextdanny.braincell.mod.serialization.BCSerializers;
import net.bottomtextdanny.braincell.mod.world.builtin_items.BCSpawnEggItem;
import net.commune.mod._client.renderers.GoblinRenderer;
import net.commune.mod.content.entities._base.CMPsycheMob;
import net.commune.mod.tables.CMEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;
import java.util.Random;

public class Goblin extends CMPsycheMob {
    public static final float ATTACK_REACH_SQUARE = 1.1F;
    public static final float ANGRY_SPEED_MULTIPLIER = 1.3F;
    public static final float RUN_AWAY_SPEED_MULTIPLIER = 1.6F;
    public static final float AVOID_RANGE = 2.5F;
    public static final float THROW_RANGE = 6.5F;
    public static final int MAX_STONES = 5;
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
    public static final EntityDataReference<Integer> STONES_REF =
            BCDataManager.attribute(Goblin.class,
                    RawEntityDataReference.of(
                            BuiltinSerializers.INTEGER,
                            () -> 4,
                            "stones")
            );
    public static final EntityDataReference<IntScheduler.Simple> THROW_DELAY_REF =
            BCDataManager.attribute(Goblin.class,
                    RawEntityDataReference.of(
                            BCSerializers.INT_SCHEDULER,
                            () -> IntScheduler.simple(60),
                            "throw_delay")
            );
    public static final EntityDataReference<IntScheduler.Simple> STONE_READDITION_DELAY_REF =
            BCDataManager.attribute(Goblin.class,
                    RawEntityDataReference.of(
                            BCSerializers.INT_SCHEDULER,
                            () -> IntScheduler.simple(100),
                            "stone_delay")
            );
    public static final SimpleAnimation THROW = new SimpleAnimation(18);
    public static final SimpleAnimation BUMP = new SimpleAnimation(17);
    public static final AnimationArray ANIMATIONS = new AnimationArray(THROW, BUMP);
    public final EntityData<Integer> stones;
    public final EntityData<IntScheduler.Simple> throwDelay;
    public final EntityData<IntScheduler.Simple> stoneDelay;
    private final IntScheduler waitForThrow;
    private final IntScheduler meleeAtackDelay;

    public Goblin(EntityType<? extends PathfinderMob> type, Level worldIn) {
        super(type, worldIn);
        this.stones = bcDataManager().addNonSyncedData(EntityData.of(STONES_REF));
        this.throwDelay = bcDataManager().addNonSyncedData(EntityData.of(THROW_DELAY_REF));
        this.stoneDelay = bcDataManager().addNonSyncedData(EntityData.of(STONE_READDITION_DELAY_REF));
        this.waitForThrow = IntScheduler.ranged(4, 6);
        this.meleeAtackDelay = IntScheduler.ranged(3, 15);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 1.5D)
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
    public AnimationGetter getAnimations() {
        return ANIMATIONS;
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
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() != null && source.getEntity() instanceof LivingEntity livingTarget) {
            this.waitForThrow.incrementFreely(40);
        }
        return super.hurt(source, amount);
    }

    @Override
    public float getLoopWalkMultiplier() {
        return 0.8F;
    }

    public IntScheduler getWaitForThrow() {
        return waitForThrow;
    }

    public IntScheduler getMeleeAtackDelay() {
        return meleeAtackDelay;
    }

    @Override
    public boolean removeWhenFarAway(double v) {
        return true;
    }
}
