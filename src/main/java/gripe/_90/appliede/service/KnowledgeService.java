package gripe._90.appliede.service;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NullInventory;

import gripe._90.appliede.module.EMCModulePart;

public class KnowledgeService implements IGridService, IGridServiceProvider {
    private final CompositeKnowledgeProvider knowledge = new CompositeKnowledgeProvider();
    private final MEStorage storage = new EMCStorage(this);
    private final List<EMCModulePart> modules = new ArrayList<>();
    private MinecraftServer server;

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
        return knowledge.getPatterns();
    }

    public void syncEmc() {
        if (server != null) {
            knowledge.syncAllEmc(server);
        }
    }
}
