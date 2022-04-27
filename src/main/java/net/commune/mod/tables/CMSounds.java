package net.commune.mod.tables;

import net.bottomtextdanny.braincell.mod._base.registry.managing.BCRegistry;
import net.bottomtextdanny.braincell.mod._base.registry.managing.RegistryHelper;
import net.bottomtextdanny.braincell.mod._base.registry.managing.Wrap;
import net.commune.Commune;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

public final class CMSounds {
    public static final BCRegistry<SoundEvent> ENTRIES = new BCRegistry<>();
    public static final RegistryHelper<SoundEvent> HELPER = new RegistryHelper<>(Commune.BC_REGISTRY_MANAGER, ENTRIES);

    //*\\*//*\\*//*\\ENTITIES START/*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//

    public static final Wrap<SoundEvent> GOBLIN_ATTACK = defer("entity.goblin_attack");
    public static final Wrap<SoundEvent> GOBLIN_COMBAT = defer("entity.goblin_combat");
    public static final Wrap<SoundEvent> GOBLIN_DEATH = defer("entity.goblin_death");
    public static final Wrap<SoundEvent> GOBLIN_HURT = defer("entity.goblin_hurt");
    public static final Wrap<SoundEvent> GOBLIN_IDLE = defer("entity.goblin_idle");

    //*\\*//*\\*//*\\ENTITIES END*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//*\\*//

    public static Wrap<SoundEvent> defer(String path) {
        return HELPER.defer(path.substring(path.lastIndexOf('.') + 1), () -> new SoundEvent(new ResourceLocation(Commune.ID, path)));
    }

    private CMSounds() {}
}
