package net.commune;

import net.bottomtextdanny.braincell.mod._base.AbstractModSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * client proxy*
 */
@OnlyIn(Dist.CLIENT)
public final class CMClientSide extends AbstractModSide {
    public final Logger logger;

    public CMClientSide() {
        super(Commune.ID);
        this.logger = LogManager.getLogger(String.join(", ", this.modId, "(client content)"));
    }

    @Override
    public void modLoadingCallOut() {
    }

    @Override
    public void postModLoadingPhaseCallOut() {
    }
}
