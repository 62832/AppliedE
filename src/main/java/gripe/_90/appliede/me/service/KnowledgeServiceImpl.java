package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;

import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.part.EMCModulePart;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;

@ApiStatus.Internal
public final class KnowledgeServiceImpl
        implements KnowledgeService, IGridServiceProvider, IStorageProvider, ICraftingProvider {
    private static final String TAG_PATTERN_PRIORITY = "knp";
    private static final int TICKS_PER_SYNC = AppliedEConfig.CONFIG.getSyncThrottleInterval();

    private final Map<UUID, IKnowledgeProvider> providers = new Object2ObjectOpenHashMap<>();
    private final Object2LongMap<AEKey> patternOutputs = new Object2LongOpenHashMap<>();
    private final List<IPatternDetails> temporaryPatterns = new ObjectArrayList<>();

    private final MEStorage storage;
    private final Lazy<BigInteger> cachedEMC = Lazy.of(this::gatherEMC);
    private final Lazy<Set<AEItemKey>> cachedKnownItems = Lazy.of(this::gatherKnownItems);

    private boolean needsSync;
    private int ticksSinceLastSync;
    private int patternPriority;
    private boolean priorityLocked;

    private final IGrid grid;
    private final Object tpeHandler;

    public KnowledgeServiceImpl(
            IGrid grid,
            IStorageService storageService,
            ICraftingService craftingService,
            IEnergyService energyService) {
        storage = new EMCStorage(this, energyService);
        storageService.addGlobalStorageProvider(this);
        craftingService.addGlobalCraftingProvider(this);
        this.grid = grid;

        NeoForge.EVENT_BUS.addListener(EMCRemapEvent.class, event -> updateKnownItems());
        NeoForge.EVENT_BUS.addListener(PlayerKnowledgeChangeEvent.class, event -> {
            if (providers.containsKey(event.getPlayerUUID())) {
                updateKnownItems();
            }
        });

        tpeHandler = ModList.get().isLoaded("teamprojecte") ? new TeamProjectEHandler() : null;
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        if (gridNode.getOwner() instanceof EMCModulePart) {
            cachedEMC.invalidate();
            cachedKnownItems.invalidate();

            var uuid = gridNode.getOwningPlayerProfileId();

            if (uuid != null) {
                providers.putIfAbsent(uuid, new DelegateKnowledgeProvider(uuid));
            }

            if (!priorityLocked && savedData != null && savedData.contains(TAG_PATTERN_PRIORITY, CompoundTag.TAG_INT)) {
                patternPriority = savedData.getInt(TAG_PATTERN_PRIORITY);
                priorityLocked = true;
            }

            grid.getCraftingService().refreshGlobalCraftingProvider(this);
        }
    }

    @Override
    public void saveNodeData(IGridNode node, CompoundTag savedData) {
        if (priorityLocked && node.getOwner() instanceof EMCModulePart) {
            savedData.putInt(TAG_PATTERN_PRIORITY, patternPriority);
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof EMCModulePart) {
            cachedEMC.invalidate();
            cachedKnownItems.invalidate();

            if (tpeHandler != null) {
                ((TeamProjectEHandler) tpeHandler).clear();
            }

            var uuid = gridNode.getOwningPlayerProfileId();

            if (uuid != null) {
                providers.remove(uuid);
            }

            grid.getCraftingService().refreshGlobalCraftingProvider(this);
        }
    }

    @Override
    public void onServerStartTick() {
        if (ticksSinceLastSync < TICKS_PER_SYNC) {
            ticksSinceLastSync++;
        }

        if (needsSync && ticksSinceLastSync == TICKS_PER_SYNC) {
            var server = ServerLifecycleHooks.getCurrentServer();

            if (server != null) {
                for (var uuid : providers.keySet()) {
                    var id = IPlayerRegistry.getMapping(server).getPlayerId(uuid);
                    var player = IPlayerRegistry.getConnected(server, id);

                    if (player != null) {
                        providers.get(uuid).syncEmc(player);
                    }
                }

                if (tpeHandler != null) {
                    ((TeamProjectEHandler) tpeHandler).syncTeamProviders(server);
                }
            }

            needsSync = false;
            ticksSinceLastSync = 0;
        }

        if (!patternOutputs.isEmpty()) {
            for (var it = Object2LongMaps.fastIterator(patternOutputs); it.hasNext(); ) {
                var output = it.next();
                var what = output.getKey();
                var amount = output.getLongValue();
                var inserted = grid.getStorageService()
                        .getInventory()
                        .insert(what, amount, Actionable.MODULATE, IActionSource.empty());

                if (inserted >= amount) {
                    it.remove();
                } else if (inserted > 0) {
                    patternOutputs.put(what, amount - inserted);
                }
            }
        }
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(storage);
    }

    List<IKnowledgeProvider> getAllProviders() {
        return new ObjectArrayList<>(providers.values());
    }

    @Nullable
    @Override
    public IKnowledgeProvider getProviderFor(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }

        var provider = providers.get(uuid);

        if (provider == null && tpeHandler != null) {
            return ((TeamProjectEHandler) tpeHandler).getProviderFor(uuid);
        }

        return provider;
    }

    @Override
    public BigInteger getEMC() {
        return cachedEMC.get();
    }

    private BigInteger gatherEMC() {
        var emc = BigInteger.ZERO;

        for (var uuid : providers.keySet()) {
            var provider = providers.get(uuid);

            if (tpeHandler != null && ((TeamProjectEHandler) tpeHandler).sharingEMC(uuid, provider)) {
                continue;
            }

            emc = emc.add(provider.getEmc());
        }

        return emc;
    }

    @Override
    public Set<AEItemKey> getKnownItems() {
        return cachedKnownItems.get();
    }

    private Set<AEItemKey> gatherKnownItems() {
        var items = new ObjectOpenHashSet<AEItemKey>();

        for (var provider : providers.values()) {
            for (var item : provider.getKnowledge()) {
                if (!IEMCProxy.INSTANCE.hasValue(item)) {
                    continue;
                }

                var key = AEItemKey.of(item.createStack());

                if (key != null) {
                    items.add(key);
                }
            }
        }

        return items;
    }

    private void updateKnownItems() {
        cachedKnownItems.invalidate();
        grid.getCraftingService().refreshGlobalCraftingProvider(this);
    }

    public void addTemporaryPattern(IPatternDetails pattern) {
        temporaryPatterns.add(pattern);
        grid.getCraftingService().refreshGlobalCraftingProvider(this);
    }

    public void removeTemporaryPattern(IPatternDetails pattern) {
        temporaryPatterns.remove(pattern);
        grid.getCraftingService().refreshGlobalCraftingProvider(this);
    }

    void syncEmc() {
        cachedEMC.invalidate();
        needsSync = true;
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        var patterns = new ObjectArrayList<IPatternDetails>();

        for (var tier = storage.getAvailableStacks().size(); tier > 1; tier--) {
            patterns.add(new TransmutationPattern(tier));
        }

        for (var item : getKnownItems()) {
            patterns.add(new TransmutationPattern(item.getItem(), 1, 0));
        }

        patterns.addAll(temporaryPatterns);
        return patterns;
    }

    @Override
    public int getPatternPriority() {
        return patternPriority;
    }

    public void setPatternPriority(int priority, IGridNode node) {
        if (node.getOwner() instanceof EMCModulePart) {
            patternPriority = priority;
            priorityLocked = true;
            grid.getCraftingService().refreshGlobalCraftingProvider(this);
        }
    }

    @Override
    public boolean pushPattern(IPatternDetails details, KeyCounter[] inputHolder) {
        if (details instanceof TransmutationPattern) {
            var output = details.getPrimaryOutput();
            patternOutputs.merge(output.what(), output.amount(), Long::sum);
            return true;
        }

        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }
}
