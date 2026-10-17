package net.emeraude.bulktrade.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.emeraude.bulktrade.BulkTradeConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Refill the payment slots after a successful quick-move, before vanilla decides whether to
 * continue the shift-click loop.
 *
 * <p>Vanilla already chains trades on a shift-click (AbstractContainerMenu.doClick re-runs
 * quickMoveStack in a loop), but only consumes what's in the 2 payment slots and never refills
 * them. Refilling only at the HEAD of quickMoveStack is too late when the previous trade emptied
 * the result slot: vanilla stops its loop before calling quickMoveStack again. This happens with
 * unstackable payments such as boats, and with prices greater than half a stack.
 *
 * <p>The merchant API is identical on 1.21.1 and 26.2 (verified against decompiled sources), so
 * this single mixin compiles and applies on both versions and both loaders (Mojmap names).
 */
@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {

    private static final int RESULT_SLOT = 2;

    @Shadow @Final private MerchantContainer tradeContainer;

    @Shadow protected abstract void moveFromInventoryToPaymentSlot(int paymentSlotIndex, ItemCost payment);

    @WrapMethod(method = "quickMoveStack")
    private ItemStack bulktrade$refillAfterTrade(Player player, int index, Operation<ItemStack> original) {
        if (index != RESULT_SLOT || player.level().isClientSide() || !BulkTradeConfig.enabled()) {
            return original.call(player, index);
        }

        // Spending the last payment can clear the active offer, so retain it before the trade.
        MerchantOffer offer = this.tradeContainer.getActiveOffer();
        ItemStack moved = original.call(player, index);
        // An empty return means nothing could be moved (e.g. the inventory is full).
        if (offer == null || moved.isEmpty() || offer.isOutOfStock()) return moved;

        this.moveFromInventoryToPaymentSlot(0, offer.getItemCostA());
        offer.getItemCostB().ifPresent(cost -> this.moveFromInventoryToPaymentSlot(1, cost));
        this.tradeContainer.updateSellItem(); // recompute the result slot
        return moved;
    }
}
