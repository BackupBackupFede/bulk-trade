package net.emeraude.bulktrade.mixin;

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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Le SEUL ajout du mod : recharger les cases de paiement depuis l'inventaire
 * avant chaque echange.
 *
 * <p>Contexte : le vanilla enchaine deja les trades en un seul shift-clic
 * (AbstractContainerMenu.doClick, cas QUICK_MOVE, rappelle quickMoveStack en
 * boucle). Ce que le vanilla ne fait PAS : reapprovisionner les 2 cases de
 * paiement depuis le reste de l'inventaire. Il s'arrete donc des que les
 * ~2 stacks poses a la main sont consommes.
 *
 * <p>On injecte au HEAD de quickMoveStack (slot resultat), on remplit les cases
 * via la methode vanilla {@code moveFromInventoryToPaymentSlot}, et on laisse
 * vanilla faire le trade. Sa propre boucle rappelle quickMoveStack -> notre
 * inject recharge a nouveau -> l'echange se poursuit jusqu'a vider l'inventaire
 * du paiement (ou remplir l'inventaire du resultat). Aucune boucle ecrite a la
 * main, aucune logique de trade dupliquee : robuste par construction.
 */
@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {

    private static final int RESULT_SLOT = 2;

    @Shadow @Final private MerchantContainer tradeContainer;

    @Shadow protected abstract void moveFromInventoryToPaymentSlot(int paymentSlotIndex, ItemCost payment);

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void bulktrade$refillBeforeTrade(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index != RESULT_SLOT || player.level().isClientSide()) return;
        if (!BulkTradeConfig.ENABLED.get()) return;

        // getActiveOffer() n'est non-null que si les cases contiennent deja le
        // paiement -> toujours vrai ici, car le slot resultat n'est cliquable
        // que dans ce cas. Sinon, on ne touche a rien (comportement vanilla).
        MerchantOffer offer = this.tradeContainer.getActiveOffer();
        if (offer == null) return;

        this.moveFromInventoryToPaymentSlot(0, offer.getItemCostA());
        offer.getItemCostB().ifPresent(cost -> this.moveFromInventoryToPaymentSlot(1, cost));
        this.tradeContainer.updateSellItem(); // recalcule le slot resultat
    }
}
