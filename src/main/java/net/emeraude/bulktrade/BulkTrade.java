package net.emeraude.bulktrade;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

/**
 * Point d'entree du mod. Ne fait qu'enregistrer la config ; toute la logique
 * de trade en masse vit dans {@link net.emeraude.bulktrade.mixin.MerchantMenuMixin}.
 */
@Mod(BulkTrade.MOD_ID)
public final class BulkTrade {
    public static final String MOD_ID = "bulktrade";

    public BulkTrade(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, BulkTradeConfig.SPEC);
    }
}
