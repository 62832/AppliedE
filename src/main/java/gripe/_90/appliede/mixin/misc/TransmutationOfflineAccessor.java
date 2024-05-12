package gripe._90.appliede.mixin.misc;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.impl.TransmutationOffline;

@Mixin(value = TransmutationOffline.class, remap = false)
public interface TransmutationOfflineAccessor {
    @Invoker
    static IKnowledgeProvider invokeForPlayer(UUID uuid) {
        throw new AssertionError();
    }
}
