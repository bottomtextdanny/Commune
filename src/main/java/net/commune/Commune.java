package net.commune;

import com.mojang.logging.LogUtils;
import net.bottomtextdanny.braincell.mod._base.registry.managing.DeferrorType;
import net.bottomtextdanny.braincell.mod._base.registry.managing.ModDeferringManager;
import net.bottomtextdanny.braincell.mod._mod.SortedCreativeTab;
import net.bottomtextdanny.braincell.mod.network.Connection;
import net.commune.mod.tables.CMEntities;
import net.commune.mod.tables.CMItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static net.commune.Commune.ID;

@Mod(ID)
public final class Commune {
    public static final String ID = "commune";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CMCommonSide COMMON = new CMCommonSide();
    /**
     * client proxy
     */
    private static final Object CLIENT = Connection.makeClientSideUnknown(() -> {
        //we use lambda here because method reference will be read by the server in classloading then it will crash.
        return new CMClientSide();
    });

    //main tab
    public static CreativeModeTab TAB = new SortedCreativeTab(ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.APPLE);
        }
    };

    public static final ModDeferringManager BC_REGISTRY_MANAGER = new ModDeferringManager(Commune.ID);

    public Commune() {
        //common setup event hook
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> {
            common().postModLoadingPhaseCallOut();

            common().modLoadingCallOut();
        });

        Connection.doClientSide(() -> {
            //client setup event hook
            FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> {
                client().postModLoadingPhaseCallOut();
            });

            client().modLoadingCallOut();
        });

        BC_REGISTRY_MANAGER.addRegistryDeferror(DeferrorType.ITEM, CMItems.ENTRIES);
        BC_REGISTRY_MANAGER.addRegistryDeferror(DeferrorType.ENTITY_TYPE, CMEntities.ENTRIES);
        BC_REGISTRY_MANAGER.solveAndLockForeverEver();
    }

    public static CMCommonSide common() {
        return COMMON;
    }

    @OnlyIn(Dist.CLIENT)
    public static CMClientSide client() {
        return (CMClientSide) CLIENT;
    }
}
