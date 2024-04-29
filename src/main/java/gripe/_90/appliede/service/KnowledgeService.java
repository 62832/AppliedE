package gripe._90.appliede.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import appeng.api.crafting.IPatternDetails;
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

import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;

public class KnowledgeService implements IGridService, IGridServiceProvider {
    private final CompositeKnowledgeProvider knowledge = new CompositeKnowledgeProvider();
    private final MEStorage storage = new EMCStorage(this);
    private final List<EMCModulePart> modules = new ArrayList<>();
    private MinecraftServer server;

    public KnowledgeService() {
        MinecraftForge.EVENT_BUS.addListener((PlayerKnowledgeChangeEvent event) ->
                modules.forEach(module -> ICraftingProvider.requestUpdate(module.getMainNode())));
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        if (server == null) {
            server = gridNode.getLevel().getServer();
        }

        if (gridNode.getOwner() instanceof EMCModulePart module) {
            modules.add(module);
            knowledge.addNode(gridNode);
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof EMCModulePart module) {
            modules.remove(module);
            knowledge.removeNode(gridNode);
        }
    }

    public CompositeKnowledgeProvider getKnowledge() {
        return knowledge;
    }

    public MEStorage getStorage() {
        return storage;
    }

    public MEStorage getStorage(EMCModulePart module) {
        return !modules.isEmpty() && module.equals(modules.get(0)) ? storage : NullInventory.of();
    }

    public List<IPatternDetails> getPatterns() {
        var patterns = new ArrayList<IPatternDetails>();
        var emc = knowledge.getEmc();
        var highestTier = 1;

        while (emc.divide(AppliedE.TIER_LIMIT).signum() == 1) {
            emc = emc.divide(AppliedE.TIER_LIMIT);
            highestTier++;
        }

        for (var tier = highestTier; tier > 1; tier--) {
            patterns.add(new TransmutationPattern(null, tier));
        }

        var knownItems = knowledge.getProviders().stream()
                .flatMap(provider -> provider.get().getKnowledge().stream())
                .map(item -> AEItemKey.of(item.getItem()))
                .collect(Collectors.toSet());

        for (var item : knownItems) {
            patterns.add(new TransmutationPattern(item, 1));
        }

        return patterns;
    }

    public void syncEmc() {
        if (server != null) {
            knowledge.syncAllEmc(server);
        }
    }
}
