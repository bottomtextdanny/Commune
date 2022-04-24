package net.commune.mod.content.entities.goblin;

import net.bottomtextdanny.braincell.base.BCMath;
import net.bottomtextdanny.braincell.base.FloatRandomPicker;
import net.bottomtextdanny.braincell.base.scheduler.IntScheduler;
import net.bottomtextdanny.braincell.base.value_mapper.FloatMapper;
import net.bottomtextdanny.braincell.base.value_mapper.FloatMappers;
import net.bottomtextdanny.braincell.base.value_mapper.RandomIntegerMapper;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.Psyche;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.input.ActionInputKey;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.input.UnbuiltActionInputs;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.pos_finder.MobPosProcessor;
import net.bottomtextdanny.braincell.mod.entity.modules.animatable.SimpleAnimation;
import net.bottomtextdanny.braincell.mod.entity.psyche.MarkedTimer;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.*;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.target.LookForAttackTargetAction;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.target.TargetBullyAction;
import net.bottomtextdanny.braincell.mod.entity.targeting.SearchNearestPredicate;
import net.bottomtextdanny.braincell.mod.entity.targeting.SearchNearestPredicates;
import net.bottomtextdanny.braincell.mod.entity.targeting.TargetPredicate;
import net.bottomtextdanny.braincell.mod.entity.targeting.TargetRange;
import net.bottomtextdanny.braincell.mod.world.helpers.CombatHelper;
import net.bottomtextdanny.braincell.mod.world.helpers.ReachHelper;
import net.commune.mod.content.entities._base.MoveReactionToEntityAction;
import net.commune.mod.content.entities._base.PsycheGeneral;
import net.commune.mod.tables.CMEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.function.Function;

import static net.bottomtextdanny.braincell.mod.entity.psyche.pos_finder.MobPosProcessors.*;

public class GoblinPsyche extends Psyche<Goblin> {
    public static final MobPosProcessor<LivingEntity> AVOID_POS = compose(
            (blockPos, mob, randomGenerator, target) -> {
                Vec3 posDiff = mob.position().subtract(target.position());
                float oppositeFromTargetRad = (float) Mth.atan2(posDiff.z, posDiff.x) + BCMath.FPI;

                FloatMapper angleAwayFromTarget = FloatMapper.from(
                        oppositeFromTargetRad - 20 * BCMath.FRAD,
                        oppositeFromTargetRad + 20 * BCMath.FRAD,
                        FloatRandomPicker.normal());

                FloatMapper stepAway = FloatMapper.of(14);

                return stack().generic(LivingEntity.class)
                        .push(advanceHorizontal(angleAwayFromTarget, stepAway))
                        .push(sample(10, randomOffset(3, 4, 3), evaluateAvoidingPos(mob)));
            });
    private static final TargetPredicate OPPOSITE_GOBLIN = (mob, target) -> {
        return target instanceof Goblin targetGoblin
                && targetGoblin.variableModule().getForm() != ((Goblin)mob).variableModule().getForm()
                && PsycheGeneral.DEFAULT_TARGET_PARAMETERS.test(mob, target);
    };
    private static final TargetPredicate NOT_SAME_GOBLIN = (mob, target) -> {
        return !(target instanceof Goblin targetGoblin && targetGoblin.variableModule().getForm() == ((Goblin)mob).variableModule().getForm())
                && PsycheGeneral.DEFAULT_TARGET_PARAMETERS.test(mob, target);
    };
    public static final int
            ANIMATION_ACTIONS_MODULE = 1,
            AVOID_MODULE = 2,
            MELEE_MODULE = 3,
            IDLE_ACTIONS_MODULE = 4;
    private final MarkedTimer unseenTimer;
    private LookRandomlyAction lookRandomlyAction;
    private RandomStrollAction randomStrollAction;
    private MoveReactionToEntityAction<Goblin> avoidTarget;
    private FollowTargetAction<Goblin> followTargetAction;
    private ThrowAttackAction throwAction;
    private BumpAttackAction bumpAction;

    public GoblinPsyche(Goblin mob) {
        super(mob);
        this.unseenTimer = new MarkedTimer(IntScheduler.simple(100));

        //allocate all the indices you will need for your AI, we call each "module".
        allocateModules(5);
    }

    @Override
    protected void populateInputs(UnbuiltActionInputs inputs) {
        //this input means the time the mob will take before forgetting its last-seen target.
        inputs.put(ActionInputKey.MARKED_UNSEEN, () -> this.unseenTimer);

        //this input is what will be run each time a new target is set by running actions.
        inputs.put(ActionInputKey.SET_TARGET_CALL, () -> {
            getMob().getMeleeAtackDelay().end();
        });
    }

    @Override
    protected void initialize() {

        this.throwAction = new ThrowAttackAction(this.getMob());
        this.bumpAction = new BumpAttackAction(this.getMob());

        this.avoidTarget = new MoveReactionToEntityAction<>(getMob(), TargetRange.followRangeMultiplied(0.3F), AVOID_POS)
                .speedByTarget(target -> Goblin.RUN_AWAY_SPEED_MULTIPLIER)
                .setRefreshRate(15)
                //we are not really searching for any entity around, just checking if
                //actual combat target is close enough.
                .searchRange(() -> 0.0F)
                .searchBy(searchTargetSpecifically());
        this.avoidTarget.addBlockedModule(IDLE_ACTIONS_MODULE);

        this.followTargetAction = new FollowTargetAction<>(getMob(), target -> Goblin.ANGRY_SPEED_MULTIPLIER).setRefreshRate(12);
        //when goblin is following a target, every action running on IDLE_ACTIONS_MODULE will be ignored.
        this.followTargetAction.addBlockedModule(IDLE_ACTIONS_MODULE);

        //goblin stroll position finder.
        MobPosProcessor<?> strollPositionCalculator = PsycheGeneral.LAND_STROLL;
        this.randomStrollAction = new RandomStrollAction(getMob(), strollPositionCalculator, RandomIntegerMapper.of(60, 120));
        this.randomStrollAction.addModule(IDLE_ACTIONS_MODULE);

        this.lookRandomlyAction = new LookRandomlyAction(getMob(), RandomIntegerMapper.of(120, 160))
                .vertical(FloatMappers.of(-0.68F, 0.2F));
        this.lookRandomlyAction.addModule(IDLE_ACTIONS_MODULE);

        //this action is processed before everything else, it handles all the transient action tasks.
        ConstantThoughtAction<Goblin> globalCheck = ConstantThoughtAction.withUpdateCallback(getMob(), mobo -> {
            checkStoneReaddition(mobo);
            LivingEntity target = mobo.getTarget();
            if (target != null) {
                onTargetFound(mobo, target);
            } else {
                idle();
            }
        });

        //these are the permanent-running actions, note that globalCheck is the first to be added, and therefore, is the first to be processed each tick.
        tryAddRunningAction(CHECKS_MODULE, globalCheck);

        tryAddRunningAction(CHECKS_MODULE, new FloatAction(getMob(), 0.2F));

        tryAddRunningAction(CHECKS_MODULE, new TargetBullyAction(getMob(), NOT_SAME_GOBLIN));

        tryAddRunningAction(CHECKS_MODULE, new LookForAttackTargetAction<>(
                getMob(), IntScheduler.simple(4),
                PsycheGeneral.DEFAULT_TARGET_PARAMETERS, SearchNearestPredicates.nearestPlayer()));

        tryAddRunningAction(CHECKS_MODULE, new LookForAttackTargetAction<>(
                getMob(), IntScheduler.simple(4),
                OPPOSITE_GOBLIN, SearchNearestPredicates.nearestLiving()));
    }

    private void checkStoneReaddition(Goblin mob) {
        int stones = mob.stones.get();

        if (stones > Goblin.MAX_STONES) return;

        IntScheduler stoneDelay = mob.stoneDelay.get();
        stoneDelay.incrementFreely(1);

        if (stoneDelay.hasEnded()) {
            mob.stones.set(stones + 1);
            stoneDelay.reset();
        }
    }

    private void onTargetFound(Goblin mob, @Nonnull LivingEntity target) {
        IntScheduler throwDelay = mob.throwDelay.get();
        IntScheduler meleeAttackDelay = mob.getMeleeAtackDelay();

        //block idling activities instantly while targeting.
        blockModule(IDLE_ACTIONS_MODULE);

        throwDelay.incrementFreely(1);
        meleeAttackDelay.incrementFreely(1);

        //if goblin should throw stone and stone is thrown successfully, skip melee behaviour.
        if (!(mob.stones.get() > 0 && throwDelay.hasEnded() && !tryThrowStoneToTarget(mob, target))) {
            goMeleeOnTarget(mob, target);
        }
    }

    private boolean tryThrowStoneToTarget(Goblin mob, @Nonnull LivingEntity target) {
        double distance = mob.position().distanceTo(target.position());

        if (distance > Goblin.THROW_RANGE) return true;

        blockModule(MELEE_MODULE);

        //if goblin is in distance to throw stone, cancel avoiding behaviour.
        if (distance > Goblin.AVOID_RANGE && mob.getSensing().hasLineOfSight(target)) {
            blockModule(AVOID_MODULE);
            IntScheduler wait = mob.getWaitForThrow();

            wait.incrementFreely(1);

            if (wait.hasEnded()) {
                tryAddRunningAction(ANIMATION_ACTIONS_MODULE, this.throwAction);
            }

            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            mob.setYRot(mob.yHeadRot);
            return true;
        }

        tryAddRunningAction(AVOID_MODULE, this.avoidTarget);
        return false;
    }

    private void goMeleeOnTarget(Goblin mob, @Nonnull LivingEntity target) {
        tryAddRunningAction(MELEE_MODULE, this.followTargetAction);

        if (mob.getMeleeAtackDelay().hasEnded() && ReachHelper.reachSqr(mob, target) < Goblin.ATTACK_REACH_SQUARE) {
            tryAddRunningAction(ANIMATION_ACTIONS_MODULE, this.bumpAction);
        }
    }

    private static SearchNearestPredicate searchTargetSpecifically() {
        return (mob, serverLevel, rangeTest, lazy, targetPredicate) -> {
            LivingEntity target = mob.getTarget();
            if (target == null || !target.isAlive()
                    || !targetPredicate.test(mob, target)) return null;
            return target;
        };
    }

    private static Function<LivingEntity, Comparator<BlockPos>> evaluateAvoidingPos(Mob mob) {
        return (target) -> mob instanceof PathfinderMob pf ? Comparator.comparingDouble(pos -> {
            double value = Math.min(1.0, pf.getWalkTargetValue(pos));
            value -= mob.position().distanceTo(target.position()) / 8;

            return value;
        }) : null;
    }

    private void idle() {
        tryAddRunningAction(IDLE_ACTIONS_MODULE, this.randomStrollAction);
        tryAddRunningAction(IDLE_ACTIONS_MODULE, this.lookRandomlyAction);
    }

    protected static final class ThrowAttackAction extends AnimationAction<Goblin, SimpleAnimation> {

        public ThrowAttackAction(Goblin mob) {
            super(mob, Goblin.THROW, mob.mainHandler);
        }

        @Override
        protected void update() {
            super.update();
            int animationTick = this.animationHandler.getTick();

            if (CombatHelper.hasValidAttackTarget(this.mob)) {
                LivingEntity target = this.mob.getTarget();

                if (animationTick == 7) {
                   this.mob.throwDelay.get().reset();
                   this.mob.stones.set(this.mob.stones.get() - 1);
                   this.mob.getWaitForThrow().reset();

                    StoneProjectile projectile = new StoneProjectile(CMEntities.STONE_PROJECTILE.get(), this.mob.level);
                    projectile.setOwner(this.mob);
                    projectile.setPos(this.mob.position().add(0.0F, 0.5F, 0.0F));

                    Vec2 rotations = PsycheGeneral.rotationsToTarget(
                            projectile.position(),
                            target.position().add(0.0F, target.getBbHeight() / 2.0F, 0.0F)
                    );

                    projectile.shootFromRotation(this.mob, rotations.x, rotations.y - 90, -1.5F,  2.0F, 0.6F);
                    this.mob.level.addFreshEntity(projectile);
                }
            }
        }

        @Override
        public boolean canStart() {
            return super.canStart() && this.animationHandler.isPlayingNull();
        }
    }

    protected static final class BumpAttackAction extends AnimationAction<Goblin, SimpleAnimation> {

        public BumpAttackAction(Goblin mob) {
            super(mob, Goblin.BUMP, mob.mainHandler);
        }

        @Override
        protected void update() {
            super.update();
            int animationTick = this.animationHandler.getTick();

            if (CombatHelper.hasValidAttackTarget(this.mob)) {
                LivingEntity target = this.mob.getTarget();

                if (animationTick == 8 && ReachHelper.reachSqr(this.mob, target) < Goblin.ATTACK_REACH_SQUARE * 1.2F) {
                    CombatHelper.attackWithMultiplier(this.mob, target, 1.0F + RANDOM.nextFloat() * 0.15F);
                    this.mob.getMeleeAtackDelay().reset();
                }

                mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                mob.setYRot(mob.yHeadRot);
            }
        }

        @Override
        public boolean canStart() {
            return super.canStart() && this.animationHandler.isPlayingNull();
        }
    }
}
