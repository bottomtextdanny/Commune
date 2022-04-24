package net.commune.mod.content.entities._base;

import net.bottomtextdanny.braincell.mod._base.entity.psyche.pos_finder.MobPosProcessor;
import net.bottomtextdanny.braincell.mod.entity.psyche.pos_finder.MobPosComparators;
import net.bottomtextdanny.braincell.mod.entity.psyche.pos_finder.MobPosPredicates;
import net.bottomtextdanny.braincell.mod.entity.targeting.TargetPredicate;
import net.bottomtextdanny.braincell.mod.entity.targeting.TargetRange;
import net.bottomtextdanny.braincell.mod.entity.targeting.Targeter;
import net.minecraft.core.Direction;

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
    public static final MobPosProcessor LAND_STROLL = compose(
            (pos, mob, r) -> stack()
                    .push(sample(10, stack()
                                    .push(randomOffset(7, 4, 7))
                                    .push(moveIf(Direction.DOWN, 16, MobPosPredicates.isSolid().negate()))
                                    .push(offset(0, 1, 0)),
                            MobPosComparators.compareWalkValue(mob)
                    )));
}
