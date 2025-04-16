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

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;

import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.part.EMCModulePart;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;

public class KnowledgeService implements IGridService, IGridServiceProvider, IStorageProvider {
    private static final int TICKS_PER_SYNC = AppliedEConfig.CONFIG.getSyncThrottleInterval();

    final Map<UUID, IKnowledgeProvider> providers = new HashMap<>();
    private final List<IManagedGridNode> moduleNodes = new ArrayList<>();
    private final EMCStorage storage;
    private final Set<IPatternDetails> temporaryPatterns = new HashSet<>();

    private BigInteger cachedEMC;
    private Set<AEItemKey> cachedKnownItems;

    private boolean needsSync;
    private int ticksSinceLastSync;

    private final Object tpeHandler;

    public KnowledgeService(IStorageService storageService, IEnergyService energyService) {
        storage = new EMCStorage(this, energyService);
        storageService.addGlobalStorageProvider(this);

        NeoForge.EVENT_BUS.addListener(EMCRemapEvent.class, event -> updateKnownItems());
        NeoForge.EVENT_BUS.addListener(PlayerKnowledgeChangeEvent.class, event -> updateKnownItems());

        tpeHandler = ModList.get().isLoaded("teamprojecte") ? new TeamProjectEHandler() : null;
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            cachedKnownItems = null;
            moduleNodes.add(module.getMainNode());
            var uuid = gridNode.getOwningPlayerProfileId();

            if (uuid != null) {
                providers.putIfAbsent(uuid, new DelegateKnowledgeProvider(uuid));
            }

            updatePatterns();
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            cachedKnownItems = null;
            moduleNodes.remove(module.getMainNode());
            providers.clear();

            if (tpeHandler != null) {
                ((TeamProjectEHandler) tpeHandler).clear();
            }

            for (var mainNode : moduleNodes) {
                var node = mainNode.getNode();

                if (node != null) {
                    var uuid = node.getOwningPlayerProfileId();

                    if (uuid != null) {
                        providers.putIfAbsent(uuid, new DelegateKnowledgeProvider(uuid));
                    }
                }
            }

            updatePatterns();
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
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(storage);
    }

    @Nullable
    public IKnowledgeProvider getProviderFor(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        var provider = providers.get(uuid);

        if (provider == null && tpeHandler != null) {
            return ((TeamProjectEHandler) tpeHandler).getProviderFor(uuid);
        }

        return provider;
    }

    public Set<AEItemKey> getKnownItems() {
        if (cachedKnownItems == null) {
            cachedKnownItems = new HashSet<>();

            for (var provider : providers.values()) {
                for (var item : provider.getKnowledge()) {
                    if (!IEMCProxy.INSTANCE.hasValue(item)) {
                        continue;
                    }

                    var key = AEItemKey.of(item.createStack());

                    if (key != null) {
                        cachedKnownItems.add(key);
                    }
                }
            }
        }

        return cachedKnownItems;
    }

    private void updateKnownItems() {
        cachedKnownItems = null;
        updatePatterns();
    }

    public List<IPatternDetails> getPatterns(IManagedGridNode node) {
        if (!moduleNodes.isEmpty() && node.equals(moduleNodes.getFirst()) && node.isActive()) {
            var patterns = new ArrayList<IPatternDetails>();

            for (var tier = storage.getAvailableStacks().size(); tier > 1; tier--) {
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
        if (cachedEMC == null) {
            cachedEMC = BigInteger.ZERO;

            for (var uuid : providers.keySet()) {
                var provider = providers.get(uuid);

                if (tpeHandler != null && ((TeamProjectEHandler) tpeHandler).sharingEMC(uuid, provider)) {
                    continue;
                }

                cachedEMC = cachedEMC.add(provider.getEmc());
            }
        }

        return cachedEMC;
    }

    void syncEmc() {
        cachedEMC = null;
        needsSync = true;
    }
}
