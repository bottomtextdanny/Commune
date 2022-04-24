package net.commune.mod.tables;

import net.bottomtextdanny.braincell.mod._base.registry.BCEntityBuilder;
import net.bottomtextdanny.braincell.mod._base.registry.BCLivingEntityBuilder;
import net.bottomtextdanny.braincell.mod._base.registry.BCMobBuilder;
import net.bottomtextdanny.braincell.mod._base.registry.EntityWrap;
import net.bottomtextdanny.braincell.mod._base.registry.managing.BCRegistry;
import net.bottomtextdanny.braincell.mod.world.builtin_items.BCSpawnEggItem;
import net.commune.Commune;
import net.commune.mod._client.renderers.GoblinRenderer;
import net.commune.mod.content.entities.goblin.Goblin;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;

public class CMEntities {
    public static final BCRegistry<EntityType<?>> ENTRIES = new BCRegistry<>();

    //*\\*//*\\*//*\\MOBS START\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//

    public static final EntityWrap<EntityType<Goblin>> GOBLIN =
            startBraincellMob("goblin", Goblin::new)
                    .classification(MobCategory.CREATURE)
                    .dimensions(0.6F, 0.7F)
                    .spawn(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Goblin::spawningParameters)
                    .attributes(Goblin::attributes)
                    .renderer(() -> GoblinRenderer::new)
                    .egg(braincellEggBuilder(0x8bc657, 0x573e2c))
                    .build();

    //*\\*//*\\*//*\\MOBS END*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//

    private static <E extends Entity> BCEntityBuilder<E> startBraincellEntity(String entityId, EntityType.EntityFactory<E> factory) {
        BCEntityBuilder<E> builder = new BCEntityBuilder<>(ENTRIES, Commune.BC_REGISTRY_MANAGER);
        builder.declare(entityId, factory);
        return builder;
    }

    private static <E extends LivingEntity> BCLivingEntityBuilder<E> startBraincellLivingEntity(String entityId, EntityType.EntityFactory<E> factory) {
        BCLivingEntityBuilder<E> builder = new BCLivingEntityBuilder<>(ENTRIES, Commune.BC_REGISTRY_MANAGER);
        builder.declare(entityId, factory);
        return builder;
    }

    private static <E extends Mob> BCMobBuilder<E> startBraincellMob(String entityId, EntityType.EntityFactory<E> factory) {
        BCMobBuilder<E> builder = new BCMobBuilder<E>(ENTRIES, Commune.BC_REGISTRY_MANAGER);
        builder.declare(entityId, factory);
        return builder;
    }

    public static BCSpawnEggItem.Builder braincellEggBuilder(int primaryTint, int secondaryTint) {
        return BCSpawnEggItem.createBuilder(primaryTint, secondaryTint).properties(new Item.Properties().tab(Commune.TAB));
    }
}
