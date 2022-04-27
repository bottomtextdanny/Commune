package net.commune.mod.tables;

import net.bottomtextdanny.braincell.mod._base.registry.managing.BCRegistry;
import net.bottomtextdanny.braincell.mod._base.registry.managing.RegistryHelper;
import net.commune.Commune;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public final class CMItems {
    public static final BCRegistry<Item> ENTRIES = new BCRegistry<>();
    public static final RegistryHelper<Item> HELPER = new RegistryHelper<>(Commune.BC_REGISTRY_MANAGER, ENTRIES);

    //

    private CMItems() {}
}
