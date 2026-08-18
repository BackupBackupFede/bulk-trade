package net.emeraude.bulktrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tiny loader-agnostic config, read once at startup from {@code config/bulktrade.json} (relative to
 * the game directory, which is the working directory on both loaders). Dependency-free on purpose:
 * no NeoForge {@code ModConfigSpec}, no config library — so the mod stays a single dependency on
 * every loader.
 */
public final class BulkTradeConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean enabled = true;

    private BulkTradeConfig() {}

    public static void load() {
        Path file = Path.of("config", BulkTrade.MOD_ID + ".json");
        try {
            if (Files.exists(file)) {
                JsonObject root = GSON.fromJson(Files.readString(file), JsonObject.class);
                if (root != null && root.has("enabled")) {
                    enabled = root.get("enabled").getAsBoolean();
                }
            } else {
                Files.createDirectories(file.getParent());
                Files.writeString(file, GSON.toJson(defaults()));
                BulkTrade.LOGGER.info("Wrote a default config to {}", file);
            }
        } catch (IOException | RuntimeException e) {
            BulkTrade.LOGGER.warn("Couldn't read {}, using defaults.", file, e);
        }
    }

    private static JsonObject defaults() {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", true);
        return root;
    }

    /** When false, the mod does nothing and merchants behave exactly like vanilla. */
    public static boolean enabled() {
        return enabled;
    }
}
