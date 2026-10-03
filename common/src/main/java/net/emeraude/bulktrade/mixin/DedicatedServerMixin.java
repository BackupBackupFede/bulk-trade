package net.emeraude.bulktrade.mixin;

import net.emeraude.bulktrade.BulkTradeSelfTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Runs {@link BulkTradeSelfTest} once the dedicated server's levels exist. No-op unless requested. */
@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    @Inject(method = "initServer", at = @At("RETURN"))
    private void bulktrade$selfTest(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && BulkTradeSelfTest.requested()) {
            BulkTradeSelfTest.run((MinecraftServer) (Object) this);
        }
    }
}
