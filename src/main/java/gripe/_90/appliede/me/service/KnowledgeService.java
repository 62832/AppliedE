package gripe._90.appliede.me.service;

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
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NullInventory;

import gripe._90.appliede.part.EMCModulePart;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

public class KnowledgeService implements IGridService, IGridServiceProvider {
    private final List<IManagedGridNode> moduleNodes = new ArrayList<>();
    private final Map<UUID, Supplier<IKnowledgeProvider>> providers = new HashMap<>();
    private final EMCStorage storage = new EMCStorage(this);
    private final TeamProjectEHandler.Proxy tpeHandler = new TeamProjectEHandler.Proxy();

    private MinecraftServer server;
    private IGrid grid;

    public KnowledgeService() {
        MinecraftForge.EVENT_BUS.addListener((PlayerKnowledgeChangeEvent event) -> updatePatterns());
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        if (server == null) {
            server = gridNode.getLevel().getServer();
        }

        if (grid == null) {
            grid = gridNode.getGrid();
        }

        if (gridNode.getOwner() instanceof EMCModulePart module) {
            moduleNodes.add(module.getMainNode());
            var uuid = gridNode.getOwningPlayerProfileId();

            if (uuid != null) {
                providers.put(uuid, () -> ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(uuid));
            }

            updatePatterns();
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            moduleNodes.remove(module.getMainNode());
            providers.clear();
            tpeHandler.clear();

            for (var mainNode : moduleNodes) {
                var node = mainNode.getNode();
                if (node == null) continue;

                var uuid = node.getOwningPlayerProfileId();
                if (uuid == null) continue;

                providers.put(uuid, () -> ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(uuid));
            }

            moduleNodes.forEach(IStorageProvider::requestUpdate);
            updatePatterns();
        }
    }

    Set<IKnowledgeProvider> getProviders() {
        return providers.values().stream().map(Supplier::get).collect(Collectors.toUnmodifiableSet());
    }

    public EMCStorage getStorage() {
        return storage;
    }

    public MEStorage getStorage(IManagedGridNode node) {
        return !moduleNodes.isEmpty() && node.equals(moduleNodes.get(0)) ? storage : NullInventory.of();
    }

    public Set<AEItemKey> getKnownItems() {
        return getProviders().stream()
                .flatMap(provider -> provider.getKnowledge().stream())
                .map(item -> AEItemKey.of(item.createStack()))
                .collect(Collectors.toSet());
    }

    public List<IPatternDetails> getPatterns() {
        var patterns = new ArrayList<IPatternDetails>();

        for (var tier = storage.getHighestTier(); tier > 1; tier--) {
            patterns.add(new TransmutationPattern(null, tier));
        }

        for (var item : getKnownItems()) {
            patterns.add(new TransmutationPattern(item, 1));
        }

        return patterns;
    }

    void updatePatterns() {
        moduleNodes.forEach(ICraftingProvider::requestUpdate);
    }

    IGrid getGrid() {
        return grid;
    }

    BigInteger getEmc() {
        return providers.entrySet().stream()
                .filter(tpeHandler::notSharingEmc)
                .map(provider -> provider.getValue().get())
                .distinct()
                .map(IKnowledgeProvider::getEmc)
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    public boolean knowsItem(AEItemKey item) {
        return getProviders().stream().anyMatch(provider -> provider.hasKnowledge(item.toStack()));
    }

    public boolean isTrackingPlayer(UUID uuid) {
        return providers.containsKey(uuid) || tpeHandler.isPlayerInTrackedTeam(uuid);
    }

    void sync() {
        if (server != null) {
            providers.forEach((uuid, provider) -> {
                var id = IPlayerRegistry.getMapping(server).getPlayerId(uuid);
                var player = IPlayerRegistry.getConnected(server, id);

                if (player != null) {
                    provider.get().sync(player);
                    provider.get().syncEmc(player);
                }
            });
        }
    }
}
