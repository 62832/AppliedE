package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NullInventory;

import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.part.EMCModulePart;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

public class KnowledgeService implements IGridService, IGridServiceProvider {
    private static final int TICKS_PER_SYNC = AppliedEConfig.CONFIG.getSyncThrottleInterval();

    private final List<IManagedGridNode> moduleNodes = new ArrayList<>();
    private final Map<UUID, Supplier<IKnowledgeProvider>> providers = new HashMap<>();
    private final EMCStorage storage = new EMCStorage(this);
    private final Set<IPatternDetails> temporaryPatterns = new HashSet<>();
    private final TeamProjectEHandler.Proxy tpeHandler = new TeamProjectEHandler.Proxy();

    final IGrid grid;
    private Set<AEItemKey> knownItemCache;
    private boolean needsSync;
    private int ticksSinceLastSync;

    public KnowledgeService(IGrid grid) {
        this.grid = grid;
        NeoForge.EVENT_BUS.addListener(EMCRemapEvent.class, event -> updateKnownItems());
        NeoForge.EVENT_BUS.addListener(PlayerKnowledgeChangeEvent.class, event -> updateKnownItems());
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            knownItemCache = null;
            moduleNodes.add(module.getMainNode());
            var uuid = gridNode.getOwningPlayerProfileId();

            if (uuid != null) {
                addProvider(uuid);
            }

            updatePatterns();
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            knownItemCache = null;
            moduleNodes.remove(module.getMainNode());
            providers.clear();
            tpeHandler.clear();

            for (var mainNode : moduleNodes) {
                var node = mainNode.getNode();

                if (node != null) {
                    var uuid = node.getOwningPlayerProfileId();

                    if (uuid != null) {
                        addProvider(uuid);
                    }
                }
            }

            moduleNodes.forEach(IStorageProvider::requestUpdate);
            updatePatterns();
        }
    }

    @Override
    public void onServerStartTick() {
        if (ticksSinceLastSync < TICKS_PER_SYNC) {
            ticksSinceLastSync++;
        }

        if (needsSync && ticksSinceLastSync == TICKS_PER_SYNC) {
            tpeHandler.syncTeamProviders(providers);
            needsSync = false;
            ticksSinceLastSync = 0;
        }
    }

    private void addProvider(UUID playerUUID) {
        providers.putIfAbsent(playerUUID, retrieveProvider(playerUUID));
    }

    static Supplier<IKnowledgeProvider> retrieveProvider(UUID playerUUID) {
        return () -> ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(playerUUID);
    }

    List<IKnowledgeProvider> getProviders() {
        return providers.values().stream().map(Supplier::get).toList();
    }

    public Supplier<IKnowledgeProvider> getProviderFor(UUID uuid) {
        return providers.getOrDefault(uuid, tpeHandler.getProviderFor(uuid));
    }

    Supplier<IKnowledgeProvider> getProviderFor(Player player) {
        return getProviderFor(player.getUUID());
    }

    Supplier<IKnowledgeProvider> getProviderFor(IActionHost host) {
        var node = host.getActionableNode();

        if (node != null) {
            var uuid = node.getOwningPlayerProfileId();
            return uuid != null ? getProviderFor(uuid) : null;
        }

        return null;
    }

    public EMCStorage getStorage() {
        return storage;
    }

    public MEStorage getStorage(IManagedGridNode node) {
        return !moduleNodes.isEmpty() && node.equals(moduleNodes.getFirst()) && node.isActive()
                ? storage
                : NullInventory.of();
    }

    public Set<AEItemKey> getKnownItems() {
        if (knownItemCache == null) {
            knownItemCache = new HashSet<>();

            for (var provider : getProviders()) {
                for (var item : provider.getKnowledge()) {
                    if (!IEMCProxy.INSTANCE.hasValue(item)) {
                        continue;
                    }

                    var key = AEItemKey.of(item.createStack());

                    if (key != null) {
                        knownItemCache.add(key);
                    }
                }
            }
        }

        return knownItemCache;
    }

    private void updateKnownItems() {
        knownItemCache = null;
        updatePatterns();
    }

    public List<IPatternDetails> getPatterns(IManagedGridNode node) {
        if (!moduleNodes.isEmpty() && node.equals(moduleNodes.getFirst()) && node.isActive()) {
            var patterns = new ArrayList<IPatternDetails>();

            for (var tier = storage.getHighestTier(); tier > 1; tier--) {
                patterns.add(new TransmutationPattern(tier));
            }

            for (var item : getKnownItems()) {
                patterns.add(new TransmutationPattern(item, 1, 0));
            }

            patterns.addAll(temporaryPatterns);
            return patterns;
        }

        return Collections.emptyList();
    }

    public void addTemporaryPattern(IPatternDetails pattern) {
        temporaryPatterns.add(pattern);
        updatePatterns();
    }

    public void removeTemporaryPattern(IPatternDetails pattern) {
        temporaryPatterns.remove(pattern);
        updatePatterns();
    }

    void updatePatterns() {
        moduleNodes.forEach(ICraftingProvider::requestUpdate);
    }

    BigInteger getEmc() {
        var emc = BigInteger.ZERO;

        for (var entry : providers.entrySet()) {
            if (tpeHandler.notSharingEmc(entry)) {
                emc = emc.add(entry.getValue().get().getEmc());
            }
        }

        return emc;
    }

    public boolean isTrackingPlayer(Player player) {
        var uuid = player.getUUID();
        return providers.containsKey(uuid) || tpeHandler.isPlayerInTrackedTeam(uuid);
    }

    void syncEmc() {
        needsSync = true;
    }
}
