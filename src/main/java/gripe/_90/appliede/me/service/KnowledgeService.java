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
    private final List<IPatternDetails> publishedPatterns = new ArrayList<>();
    private final Map<AEItemKey, TransmutationPattern> itemPatterns = new HashMap<>();
    private final List<TransmutationPattern> tierPatterns = new ArrayList<>();
    private final TeamProjectEHandler.Proxy tpeHandler = new TeamProjectEHandler.Proxy();

    final IGrid grid;
    private Set<AEItemKey> knownItemCache;
    private int publishedHighestTier = 1;
    private boolean patternsReady;
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
        if (uuid == null) {
            return null;
        }

        var provider = providers.get(uuid);

        if (provider != null) {
            return provider;
        }

        var teamProvider = tpeHandler.getProviderFor(uuid);
        return teamProvider != null ? teamProvider : retrieveProvider(uuid);
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
        syncPatterns();
    }

    public List<IPatternDetails> getPatterns(IManagedGridNode node) {
        if (!isActiveModule(node)) {
            return List.of();
        }

        if (!patternsReady) {
            rebuildPatternCache();
        }

        return Collections.unmodifiableList(publishedPatterns);
    }

    public void addTemporaryPattern(IPatternDetails pattern) {
        if (!temporaryPatterns.add(pattern)) {
            return;
        }

        if (!patternsReady) {
            rebuildPatternCache();
            return;
        }

        publishedPatterns.add(pattern);
        pushPatternChange(List.of(), List.of(pattern));
    }

    public void removeTemporaryPattern(IPatternDetails pattern) {
        if (!temporaryPatterns.remove(pattern)) {
            return;
        }

        if (!patternsReady) {
            rebuildPatternCache();
            return;
        }

        publishedPatterns.remove(pattern);
        pushPatternChange(List.of(pattern), List.of());
    }

    void updatePatterns() {
        patternsReady = false;
        moduleNodes.forEach(ICraftingProvider::requestUpdate);
    }

    void syncPatterns() {
        if (!patternsReady) {
            rebuildPatternCache();
            return;
        }

        var removed = new ArrayList<IPatternDetails>();
        var added = new ArrayList<IPatternDetails>();
        syncItems(removed, added);
        syncTiers(removed, added);

        if (removed.isEmpty() && added.isEmpty()) {
            return;
        }

        rebuildPublishedList();
        pushPatternChange(removed, added);
    }

    private void syncItems(List<IPatternDetails> removed, List<IPatternDetails> added) {
        var next = getKnownItems();
        var iterator = itemPatterns.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();

            if (!next.contains(entry.getKey())) {
                removed.add(entry.getValue());
                iterator.remove();
            }
        }

        for (var item : next) {
            if (itemPatterns.containsKey(item)) {
                continue;
            }

            var pattern = new TransmutationPattern(item, 1, 0);
            itemPatterns.put(item, pattern);
            added.add(pattern);
        }
    }

    private void syncTiers(List<IPatternDetails> removed, List<IPatternDetails> added) {
        var highest = storage.getHighestTier();

        if (highest == publishedHighestTier) {
            return;
        }

        removed.addAll(tierPatterns);
        tierPatterns.clear();

        for (var tier = highest; tier > 1; tier--) {
            var pattern = new TransmutationPattern(tier);
            tierPatterns.add(pattern);
            added.add(pattern);
        }

        publishedHighestTier = highest;
    }

    private void rebuildPatternCache() {
        itemPatterns.clear();
        tierPatterns.clear();
        publishedHighestTier = storage.getHighestTier();

        for (var tier = publishedHighestTier; tier > 1; tier--) {
            tierPatterns.add(new TransmutationPattern(tier));
        }

        for (var item : getKnownItems()) {
            itemPatterns.put(item, new TransmutationPattern(item, 1, 0));
        }

        rebuildPublishedList();
        patternsReady = true;
    }

    private void rebuildPublishedList() {
        publishedPatterns.clear();
        publishedPatterns.addAll(tierPatterns);
        publishedPatterns.addAll(itemPatterns.values());
        publishedPatterns.addAll(temporaryPatterns);
    }

    private void pushPatternChange(List<IPatternDetails> removed, List<IPatternDetails> added) {
        var node = activeCraftingNode();

        if (node == null) {
            return;
        }

        var index = PatternIndex.of(grid);

        if (index == null) {
            updatePatterns();
            return;
        }

        if (index.appliede$hasProvider(node)) {
            index.appliede$replacePatterns(node, removed, added);
        }
    }

    private boolean isActiveModule(IManagedGridNode node) {
        return !moduleNodes.isEmpty() && node.equals(moduleNodes.getFirst()) && node.isActive();
    }

    private IGridNode activeCraftingNode() {
        if (moduleNodes.isEmpty()) {
            return null;
        }

        var module = moduleNodes.getFirst();
        return module.isActive() ? module.getNode() : null;
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

    public void refreshKnownItems() {
        updateKnownItems();
    }

    public boolean isTrackingPlayer(Player player) {
        var uuid = player.getUUID();
        return providers.containsKey(uuid) || tpeHandler.isPlayerInTrackedTeam(uuid);
    }

    void syncEmc() {
        needsSync = true;
    }
}
