package net.emeraude.bulktrade;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Config serveur : c'est le serveur qui execute les trades. */
public final class BulkTradeConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        ENABLED = b
                .comment(
                        "Recharge les cases de paiement depuis l'inventaire pendant un",
                        "shift-clic, pour ecouler tout le stock en un clic (au lieu des",
                        "~2 stacks poses a la main). false = comportement vanilla.")
                .define("enabled", true);

        SPEC = b.build();
    }

    private BulkTradeConfig() {}
}
