package gripe._90.appliede.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.network.NetworkEvent;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;

@Mixin(KnowledgeSyncEmcPKT.class)
public abstract class KnowledgeSyncEmcPKTMixin {
    // spotless:off
    @Inject(
            method = "handle",
            at = @At(
                    value = "INVOKE",
                    target = "Lmoze_intel/projecte/PECore;debugLog(Ljava/lang/String;[Ljava/lang/Object;)V"),
            cancellable = true,
            remap = false)
    // spotless:on
    private void shutUp(NetworkEvent.Context context, CallbackInfo ci) {
        if (!ProjectEConfig.common.debugLogging.get()) {
            ci.cancel();
        }
    }
}
