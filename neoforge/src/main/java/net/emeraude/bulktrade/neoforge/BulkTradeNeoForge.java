package net.emeraude.bulktrade.neoforge;

import net.emeraude.bulktrade.BulkTrade;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/** NeoForge entry point. All logic is loader-agnostic in {@link BulkTrade}. */
@Mod(BulkTrade.MOD_ID)
public final class BulkTradeNeoForge {

    public BulkTradeNeoForge(IEventBus modBus, ModContainer container) {
        BulkTrade.init();
    }
}
