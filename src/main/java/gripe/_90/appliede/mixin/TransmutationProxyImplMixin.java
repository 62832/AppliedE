package gripe._90.appliede.mixin;

import java.util.UUID;

import com.google.common.base.Preconditions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.server.ServerLifecycleHooks;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.impl.TransmutationProxyImpl;

@Mixin(value = TransmutationProxyImpl.class, remap = false)
public abstract class TransmutationProxyImplMixin {
    @Shadow
    protected abstract Player findOnlinePlayer(UUID playerUUID);

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "getKnowledgeProviderFor", at = @At("HEAD"), cancellable = true)
    private void dontFuckingThrow(UUID playerUUID, CallbackInfoReturnable<IKnowledgeProvider> cir) {
        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
            cir.setReturnValue(DistExecutor.unsafeRunForDist(
                    () -> () -> {
                        var player = Minecraft.getInstance().player;
                        Preconditions.checkState(player != null, "Client player doesn't exist!");
                        var offline = TransmutationOfflineAccessor.invokeForPlayer(player.getUUID());
                        return player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY)
                                .orElse(offline);
                    },
                    () -> () -> {
                        throw new RuntimeException("unreachable");
                    }));
        } else {
            Preconditions.checkNotNull(playerUUID);
            Preconditions.checkNotNull(
                    ServerLifecycleHooks.getCurrentServer(), "Server must be running to query knowledge!");
            var player = findOnlinePlayer(playerUUID);
            var offline = TransmutationOfflineAccessor.invokeForPlayer(playerUUID);
            cir.setReturnValue(
                    player != null
                            ? player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY)
                                    .orElse(offline)
                            : offline);
        }
    }
}
