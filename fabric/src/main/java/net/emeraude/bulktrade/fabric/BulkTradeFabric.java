package net.emeraude.bulktrade.fabric;

import net.emeraude.bulktrade.BulkTrade;
import net.fabricmc.api.ModInitializer;

/** Fabric entry point. All logic is loader-agnostic in {@link BulkTrade}. */
public final class BulkTradeFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        BulkTrade.init();
    }
}
