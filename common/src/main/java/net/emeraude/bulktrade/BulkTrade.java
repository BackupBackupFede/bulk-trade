package net.emeraude.bulktrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bulk Villager Trading — refills the payment slots from the player's inventory during a
 * shift-click on a merchant, so one click trades the whole stock instead of stopping after the
 * two payment slots run dry.
 *
 * <p>This class is loader-agnostic; the Fabric and NeoForge entry points just call {@link #init()}.
 * All the actual behaviour lives in {@code mixin.MerchantMenuMixin}.
 */
public final class BulkTrade {

    public static final String MOD_ID = "bulktrade";
    public static final Logger LOGGER = LoggerFactory.getLogger("Bulk Villager Trading");

    private static boolean initialized = false;

    private BulkTrade() {}

    /** Called once from each loader's entry point, as early as possible. */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        BulkTradeConfig.load();
        LOGGER.info("Bulk Villager Trading loaded — toggle in config/{}.json", MOD_ID);
    }
}
