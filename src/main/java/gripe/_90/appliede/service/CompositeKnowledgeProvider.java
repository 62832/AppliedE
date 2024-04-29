package gripe._90.appliede.service;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.server.MinecraftServer;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

public class CompositeKnowledgeProvider {
    private final Map<UUID, Supplier<IKnowledgeProvider>> providers = new Object2ObjectLinkedOpenHashMap<>();

    Set<Supplier<IKnowledgeProvider>> getProviders() {
        return providers.values().stream().collect(Collectors.toUnmodifiableSet());
    }

    void addNode(IGridNode node) {
        var uuid = node.getOwningPlayerProfileId();

        if (uuid != null) {
            providers.put(uuid, () -> ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(uuid));
        }
    }

    void removeNode(IGridNode node) {
        var uuid = node.getOwningPlayerProfileId();

        if (uuid != null) {
            providers.remove(uuid);
        }
    }

    void syncAllEmc(MinecraftServer server) {
        providers.forEach((uuid, provider) -> {
            var id = IPlayerRegistry.getMapping(server).getPlayerId(uuid);
            var player = IPlayerRegistry.getConnected(server, id);

            if (player != null) {
                provider.get().syncEmc(player);
            }
        });
    }

    public boolean knowsItem(AEItemKey item) {
        return getProviders().stream().anyMatch(provider -> provider.get().hasKnowledge(item.toStack()));
    }

    public BigInteger getEmc() {
        return getProviders().stream()
                .map(provider -> provider.get().getEmc())
                .reduce(BigInteger.ZERO, BigInteger::add);
    }
}
