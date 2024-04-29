package gripe._90.appliede.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NullInventory;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.module.EMCModulePart;
import gripe._90.appliede.module.TransmutationPattern;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

public class KnowledgeService implements IGridService, IGridServiceProvider {
    private final List<EMCModulePart> modules = new ArrayList<>();
    private final Map<UUID, Supplier<IKnowledgeProvider>> providers = new HashMap<>();
    private final MEStorage storage = new EMCStorage(this);

    private MinecraftServer server;

    public KnowledgeService() {
        MinecraftForge.EVENT_BUS.addListener((PlayerKnowledgeChangeEvent event) -> {
            for (var module : modules) {
                ICraftingProvider.requestUpdate(module.getMainNode());
            }
        });
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        if (server == null) {
            server = gridNode.getLevel().getServer();
        }

        if (gridNode.getOwner() instanceof EMCModulePart module) {
            modules.add(module);
            var uuid = gridNode.getOwningPlayerProfileId();

            if (uuid != null) {
                providers.put(uuid, () -> ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(uuid));
            }
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            modules.remove(module);
            var uuid = gridNode.getOwningPlayerProfileId();

            if (uuid != null) {
                providers.remove(uuid);
            }
        }
    }

    public Set<Supplier<IKnowledgeProvider>> getProviders() {
        return providers.values().stream().collect(Collectors.toUnmodifiableSet());
    }

    public MEStorage getStorage() {
        return storage;
    }

    public MEStorage getStorage(EMCModulePart module) {
        return !modules.isEmpty() && module.equals(modules.get(0)) ? storage : NullInventory.of();
    }

    public List<IPatternDetails> getPatterns() {
        var patterns = new ArrayList<IPatternDetails>();
        var emc = getEmc();
        var highestTier = 1;

        while (emc.divide(AppliedE.TIER_LIMIT).signum() == 1) {
            emc = emc.divide(AppliedE.TIER_LIMIT);
            highestTier++;
        }

        for (var tier = highestTier; tier > 1; tier--) {
            patterns.add(new TransmutationPattern(null, tier));
        }

        var knownItems = getProviders().stream()
                .flatMap(provider -> provider.get().getKnowledge().stream())
                .map(item -> AEItemKey.of(item.getItem()))
                .collect(Collectors.toSet());

        for (var item : knownItems) {
            patterns.add(new TransmutationPattern(item, 1));
        }

        return patterns;
    }

    public BigInteger getEmc() {
        return getProviders().stream()
                .map(provider -> provider.get().getEmc())
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    public boolean knowsItem(AEItemKey item) {
        return getProviders().stream().anyMatch(provider -> provider.get().hasKnowledge(item.toStack()));
    }

    public void syncEmc() {
        if (server != null) {
            providers.forEach((uuid, provider) -> {
                var id = IPlayerRegistry.getMapping(server).getPlayerId(uuid);
                var player = IPlayerRegistry.getConnected(server, id);

                if (player != null) {
                    provider.get().syncEmc(player);
                }
            });
        }
    }
}
