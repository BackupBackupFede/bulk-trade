package net.emeraude.bulktrade;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Development check, off unless the environment variable {@code BULKTRADE_SELFTEST=true} is set
 * (Gradle's runServer forks a JVM that inherits the environment, on both loaders).
 *
 * <p>Once the dedicated server is up, it builds a player with no connection, opens a merchant
 * menu on it, performs the same shift-click vanilla performs (quickMoveStack, re-run while the
 * result slot refills with the same item — the loop of AbstractContainerMenu.doClick), and checks
 * the inventory afterwards. Then it stops the server. "It compiles" proves nothing about a mixin;
 * this proves the behaviour.
 *
 * <p>The witness: with {@code "enabled": false} in {@code config/bulktrade.json} the merchant check
 * must fail (vanilla stops once the payment slot is empty), or it measures nothing.
 */
public final class BulkTradeSelfTest {

    private BulkTradeSelfTest() {}

    public static boolean requested() {
        return "true".equalsIgnoreCase(System.getenv("BULKTRADE_SELFTEST"));
    }

    public static void run(MinecraftServer server) {
        List<String> failures = new ArrayList<>();
        try {
            ServerLevel level = server.overworld();
            check(failures, "merchant", merchant(server, level));
        } catch (Throwable t) {
            BulkTrade.LOGGER.error("[SELFTEST] crashed", t);
            failures.add("crash: " + t);
        }
        BulkTrade.LOGGER.info("[SELFTEST] RESULT {}", failures.isEmpty() ? "PASS" : "FAIL " + failures);
        server.halt(false);
    }

    private static void check(List<String> failures, String name, String error) {
        if (error == null) {
            BulkTrade.LOGGER.info("[SELFTEST] {} OK", name);
        } else {
            BulkTrade.LOGGER.error("[SELFTEST] {} FAILED: {}", name, error);
            failures.add(name + ": " + error);
        }
    }

    // --- checks ---------------------------------------------------------------------------------

    /** 1 emerald -> 1 bread, 1 emerald in the payment slot and 19 in the inventory: 20 trades. */
    private static String merchant(MinecraftServer server, ServerLevel level) {
        ServerPlayer player = player(server, level);
        Inventory inv = player.getInventory();
        inv.setItem(9, stack("minecraft:emerald", 19));

        MerchantOffers offers = new MerchantOffers();
        offers.add(new MerchantOffer(new ItemCost(stack("minecraft:emerald", 1).getItem(), 1), stack("minecraft:bread", 1), 100, 1, 0.05F));
        // Reports itself client-side only so the menu skips its trade sound, which casts the
        // merchant to an Entity. The refill checks the player's side, not the merchant's.
        ClientSideMerchant merchant = new ClientSideMerchant(player) {
            @Override
            public boolean isClientSide() {
                return true;
            }
        };
        merchant.overrideOffers(offers);

        MerchantMenu menu = new MerchantMenu(1, inv, merchant);
        menu.setSelectionHint(0);
        menu.slots.get(0).set(stack("minecraft:emerald", 1));
        if (!menu.slots.get(2).hasItem()) return "no result with 1 emerald in the payment slot";

        shiftClick(menu, player, 2);
        int bread = count(inv, "minecraft:bread");
        int emeralds = count(inv, "minecraft:emerald") + menu.slots.get(0).getItem().getCount();
        return bread == 20 && emeralds == 0 ? null : bread + " bread / " + emeralds + " emeralds left (expected 20 / 0)";
    }

    // --- helpers --------------------------------------------------------------------------------

    /** What AbstractContainerMenu.doClick does for a QUICK_MOVE on a slot. */
    private static void shiftClick(AbstractContainerMenu menu, ServerPlayer player, int slot) {
        ItemStack moved = menu.quickMoveStack(player, slot);
        int guard = 0;
        while (!moved.isEmpty() && ItemStack.isSameItem(menu.slots.get(slot).getItem(), moved) && guard++ < 1000) {
            moved = menu.quickMoveStack(player, slot);
        }
    }

    /**
     * A player with a connection that has no channel: vanilla sends packets on the way (recipe
     * book unlocks when a recipe is used for the first time), and an unopened Connection just
     * queues them.
     */
    private static ServerPlayer player(MinecraftServer server, ServerLevel level) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "selftest");
        ServerPlayer player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(profile, false));
        player.getInventory().clearContent();
        return player;
    }

    /** Items resolved by id, not by Items.* fields: several were renamed or regrouped in 26.x. */
    private static ItemStack stack(String id, int count) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (id(item).equals(id)) return new ItemStack(item, count);
        }
        throw new IllegalArgumentException("unknown item " + id);
    }

    private static String id(Item item) {
        return String.valueOf(BuiltInRegistries.ITEM.getKey(item));
    }

    private static int count(Inventory inv, String id) {
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && id(s.getItem()).equals(id)) n += s.getCount();
        }
        return n;
    }
}
