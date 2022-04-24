package net.commune.mod.content.entities.goblin;

import net.bottomtextdanny.braincell.base.scheduler.IntScheduler;
import net.bottomtextdanny.braincell.base.value_mapper.FloatMappers;
import net.bottomtextdanny.braincell.base.value_mapper.RandomIntegerMapper;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.Psyche;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.input.ActionInputKey;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.input.UnbuiltActionInputs;
import net.bottomtextdanny.braincell.mod._base.entity.psyche.pos_finder.MobPosProcessor;
import net.bottomtextdanny.braincell.mod.entity.psyche.MarkedTimer;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.ConstantThoughtAction;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.FollowTargetAction;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.LookRandomlyAction;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.RandomStrollAction;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.target.LookForAttackTargetAction;
import net.bottomtextdanny.braincell.mod.entity.psyche.actions.target.TargetBullyAction;
import net.bottomtextdanny.braincell.mod.entity.targeting.SearchNearestPredicates;
import net.commune.mod.content.entities._base.PsycheGeneral;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

public class GoblinPsyche extends Psyche<Goblin> {
    public static final int
            MAIN_MODULE = 1,
            ANIMATION_ACTIONS_MODULE = 2,
            IDLE_ACTIONS_MODULE = 3;
    private final MarkedTimer unseenTimer;
    private LookRandomlyAction lookRandomlyAction;
    private RandomStrollAction randomStrollAction;
    private FollowTargetAction<Goblin> followTargetAction;

    public GoblinPsyche(Goblin mob) {
        super(mob);
        this.unseenTimer = new MarkedTimer(IntScheduler.simple(100));

        //allocate all the indices you will need for your AI, we call each "module".
        allocateModules(3);
    }

    @Override
    protected void populateInputs(UnbuiltActionInputs inputs) {
        //this input means the time the mob will take before forgetting its last-seen target.
        inputs.put(ActionInputKey.MARKED_UNSEEN, () -> this.unseenTimer);
    }

    @Override
    protected void initialize() {

        this.followTargetAction = new FollowTargetAction<>(getMob(), target -> Goblin.ANGRY_SPEED_MULTIPLIER).setRefreshRate(12);
        //when goblin is following a target, every action running on IDLE_ACTIONS_MODULE will be ignored.
        this.followTargetAction.addBlockedModule(IDLE_ACTIONS_MODULE);

        //goblin stroll position finder.
        MobPosProcessor strollPositionCalculator = PsycheGeneral.LAND_STROLL;
        this.randomStrollAction = new RandomStrollAction(getMob(), strollPositionCalculator, RandomIntegerMapper.of(60, 120));
        this.randomStrollAction.addModule(IDLE_ACTIONS_MODULE);

        this.lookRandomlyAction = new LookRandomlyAction(getMob(), RandomIntegerMapper.of(120, 160)).vertical(FloatMappers.of(-0.68F, 0.2F));
        this.lookRandomlyAction.addModule(IDLE_ACTIONS_MODULE);

        //this action is processed before everything else, it handles all the transient action tasks.
        ConstantThoughtAction<Goblin> globalCheck = ConstantThoughtAction.withUpdateCallback(getMob(), mobo -> {
            if (mobo.getTarget() != null) {
                onTargetFound();
            } else {
                idle();
            }
        });

        //these are the permanent-running actions, note that globalCheck is the first to be added, and therefore, is the first to be processed each tick.
        tryAddRunningAction(CHECKS_MODULE, globalCheck);

        tryAddRunningAction(CHECKS_MODULE, new TargetBullyAction(getMob(), PsycheGeneral.DEFAULT_TARGET_PARAMETERS));

        tryAddRunningAction(CHECKS_MODULE, new LookForAttackTargetAction<>(
                getMob(), IntScheduler.simple(4),
                PsycheGeneral.DEFAULT_TARGET_PARAMETERS, SearchNearestPredicates.nearestPlayer()));


    }

    private void onTargetFound() {
        tryAddRunningAction(MAIN_MODULE, this.followTargetAction);
    }

    private void idle() {
        tryAddRunningAction(IDLE_ACTIONS_MODULE, this.randomStrollAction);
        tryAddRunningAction(IDLE_ACTIONS_MODULE, this.lookRandomlyAction);
    }
}
