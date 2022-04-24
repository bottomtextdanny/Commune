package net.commune.mod.content.entities._base;

import net.bottomtextdanny.braincell.base.vector.DistanceCalc;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.Action;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.pos_finder.MobPosProcessor;
import net.bottomtextdanny.braincell.mod.entity.targeting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;

public class MoveReactionToEntityAction<E extends PathfinderMob> extends Action<E> {
    public static int DEFAULT_PATH_REFRESH_RATE = 4;
    private final MobPosProcessor<LivingEntity> posFinder;
    private final TargetPredicate mobFinder;
    private ToDoubleFunction<LivingEntity> moveSpeedByTarget;
    private SearchNearestPredicate searchPredicate;
    private DoubleSupplier searchRange;
    @Nullable
    private LivingEntity focus;
    @Nullable
    private Path goal;
    private int refreshRate;

    public MoveReactionToEntityAction(E mob, TargetPredicate mobPredicate,
                                      MobPosProcessor<LivingEntity> posFinder) {
        super(mob);
        this.refreshRate = DEFAULT_PATH_REFRESH_RATE;
        this.mobFinder = mobPredicate;
        this.posFinder = posFinder;
        this.moveSpeedByTarget = target -> 1.0;
        this.searchRange = () -> mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        this.searchPredicate = SearchNearestPredicates.nearestLiving();
    }

    public MoveReactionToEntityAction<E> setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
        return this;
    }

    public MoveReactionToEntityAction<E> searchBy(SearchNearestPredicate predicate) {
        this.searchPredicate = predicate;
        return this;
    }

    public MoveReactionToEntityAction<E> speedByTarget(ToDoubleFunction<LivingEntity> moveSpeedByTarget) {
        this.moveSpeedByTarget = moveSpeedByTarget;
        return this;
    }

    public MoveReactionToEntityAction<E> searchRange(DoubleSupplier searchRange) {
        this.searchRange = searchRange;
        return this;
    }

    public boolean canStart() {
        if (this.focus == null) {
            updateFocus();
        }

        LivingEntity focus = this.focus;

        if (this.active() && !this.mob.isVehicle()
                && focus != null) {
            BlockPos goalPos = this.posFinder.compute(this.mob.blockPosition(), this.mob, UNSAFE_RANDOM, focus);

            if (goalPos == null) return false;

            this.goal = this.mob.getNavigation().createPath(goalPos, 0);

            return this.goal != null;
        } else {
            return false;
        }
    }

    protected void start() {
        this.mob.getNavigation().moveTo(this.goal, this.moveSpeedByTarget.applyAsDouble(this.mob.getTarget()));
    }

    protected void update() {
        if (this.ticksPassed % this.refreshRate == 0) {
            if (EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(this.focus)
                    && this.mob.getNavigation().isDone() && !this.goal.isDone()) {

                this.onPathUpdate();
            }
            else {
                    this.goal = null;
                    this.focus = null;
                    this.mob.getNavigation().stop();
            }
        }
    }

    protected void onPathUpdate() {
        LivingEntity livingentity = this.focus;
        this.mob.getNavigation().moveTo(this.goal, this.moveSpeedByTarget.applyAsDouble(livingentity));
    }

    public void onEnd() {
        super.onEnd();
        this.focus = null;
        this.goal = null;
        this.mob.getNavigation().stop();
    }

    public boolean shouldKeepGoing() {
        return this.active() && !this.mob.isVehicle() &&
                (this.goal != null && this.mob.getNavigation().isInProgress())
                && !this.mob.getNavigation().isStuck()
                && this.focus != null && this.focus.isAlive();
    }

    protected void updateFocus() {
        double range = this.searchRange.getAsDouble();

        this.focus = this.searchPredicate.search(this.mob,
                (ServerLevel)this.mob.level,
                RangeTest.awayFrom(this.mob, range, DistanceCalc.MANHATTAN),
                this.getTargetSearchArea(range), this.mobFinder.and(TargetPredicates.noCreativeOrSpectator()));
    }

    protected Lazy<AABB> getTargetSearchArea(double p_26069_) {
        return Lazy.of(() -> {
            return this.mob.getBoundingBox().inflate(p_26069_, 4.0D, p_26069_);
        });
    }
}
