package net.commune.mod.content.entities._base;

import net.bottomtextdanny.braincell.base.BCMath;
import net.bottomtextdanny.braincell.mod.entity.psyche.pos_finder.MobPosComparators;
import net.bottomtextdanny.braincell.mod.entity.psyche.pos_finder.MobPosPredicates;
import net.bottomtextdanny.braincell.mod.entity.psyche.pos_finder.MobPosProcessor;
import net.bottomtextdanny.braincell.mod.entity.psyche.targeting.TargetPredicate;
import net.bottomtextdanny.braincell.mod.entity.psyche.targeting.TargetRange;
import net.bottomtextdanny.braincell.mod.entity.psyche.targeting.Targeter;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import static net.bottomtextdanny.braincell.mod.entity.psyche.pos_finder.MobPosProcessors.*;

public class PsycheGeneral {
    public static final TargetPredicate DEFAULT_TARGET_PARAMETERS = Targeter.Builder
            .start(TargetRange.followRange())
            .isForCombat()
            .targetRangeForInvisible(TargetRange.fixedRange(2.0D))
            .build();
    /**
     * imitates goal equivalent land stroll.
     */
    public static final MobPosProcessor<?> LAND_STROLL = compose(
            (pos, mob, r, extra) -> stack()
                    .push(sample(10, stack()
                                    .push(randomOffset(7, 4, 7))
                                    .push(moveIf(Direction.DOWN, 16, MobPosPredicates.isSolid().negate()))
                                    .push(offset(0, 1, 0)),
                            MobPosComparators.compareWalkValue(mob)
                    )));

    public static Vec2 rotationsToTarget(double x1, double y1, double z1,
                                         double x2, double y2, double z2) {
        double diffX = x2 - x1;
        double diffY = y2 - y1;
        double diffZ = z2 - z1;
        double horizontalAbsoluteDiff = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));
        return new Vec2((float)(-Mth.atan2(diffY, horizontalAbsoluteDiff) * BCMath.FRAD2DEG), (float)Mth.atan2(diffZ, diffX) * BCMath.FRAD2DEG);
    }

    public static Vec2 rotationsToTarget(Vec3 vec1, Vec3 vec2) {
        return rotationsToTarget(vec1.x, vec1.y, vec1.z, vec2.x, vec2.y, vec2.z);
    }
}
