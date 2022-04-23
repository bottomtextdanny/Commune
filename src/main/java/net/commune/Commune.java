package net.commune;

import com.mojang.logging.LogUtils;
import net.bottomtextdanny.braincell.mod._base.network.Connection;
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

    }

    public static CMCommonSide common() {
        return COMMON;
    }

    @OnlyIn(Dist.CLIENT)
    public static CMClientSide client() {
        return (CMClientSide) CLIENT;
    }
}
