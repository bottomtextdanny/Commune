package net.commune;

import net.bottomtextdanny.braincell.mod._base.AbstractModSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CMCommonSide extends AbstractModSide {
    public final Logger logger;

    public CMCommonSide() {
        super(Commune.ID);
        this.logger = LogManager.getLogger(String.join(", ", this.modId, "(common content)"));
    }

    @Override
    public void modLoadingCallOut() {
    }

    @Override
    public void postModLoadingPhaseCallOut() {
    }
}
