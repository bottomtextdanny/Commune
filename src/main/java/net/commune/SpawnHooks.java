package net.commune;

import net.commune.mod.content.entities.goblin.Goblin;
import net.commune.mod.tables.CMEntities;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class SpawnHooks {

    public static void applySpawnToBiomes(BiomeLoadingEvent event) {
        Biome biome = ForgeRegistries.BIOMES.getValue(event.getName());

        if (biome != null && event.getName() != null) {
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
            List<MobSpawnSettings.SpawnerData> monsterSpawns = event.getSpawns().getSpawner(MobCategory.MONSTER);
            Set<BiomeDictionary.Type> forgeDictionary = BiomeDictionary.getTypes(biomeKey);

            if (forgeDictionary.contains(BiomeDictionary.Type.OVERWORLD)
                    && forgeDictionary.contains(BiomeDictionary.Type.FOREST)) {
                monsterSpawns.add(spawn(CMEntities.GOBLIN.get(), 15, 7, 9));
            }
        }
    }

    private static MobSpawnSettings.SpawnerData spawn(EntityType<?> entityType, int weight, int minGroup, int maxGroup) {
        return new MobSpawnSettings.SpawnerData(entityType, weight, minGroup, maxGroup);
    }

    private SpawnHooks() {}
}
