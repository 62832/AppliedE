package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

record DelegateKnowledgeProvider(UUID playerUUID) implements IKnowledgeProvider {
    private IKnowledgeProvider get() {
        return ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(playerUUID);
    }

    @Override
    public boolean hasFullKnowledge() {
        return get().hasFullKnowledge();
    }

    @Override
    public void setFullKnowledge(boolean fullKnowledge) {
        get().setFullKnowledge(fullKnowledge);
    }

    @Override
    public void clearKnowledge() {
        get().clearKnowledge();
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemInfo info) {
        return get().hasKnowledge(info);
    }

    @Override
    public boolean addKnowledge(@NotNull ItemInfo info) {
        return get().addKnowledge(info);
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemInfo info) {
        return get().removeKnowledge(info);
    }

    @NotNull
    @Override
    public Set<ItemInfo> getKnowledge() {
        return get().getKnowledge();
    }

    @NotNull
    @Override
    public IItemHandler getInputAndLocks() {
        return get().getInputAndLocks();
    }

    @Override
    public BigInteger getEmc() {
        return get().getEmc();
    }

    @Override
    public void setEmc(BigInteger emc) {
        get().setEmc(emc);
    }

    @Override
    public void sync(@NotNull ServerPlayer player) {
        get().sync(player);
    }

    @Override
    public void syncEmc(@NotNull ServerPlayer player) {
        get().syncEmc(player);
    }

    @Override
    public void syncKnowledgeChange(@NotNull ServerPlayer player, ItemInfo change, boolean learned) {
        get().syncKnowledgeChange(player, change, learned);
    }

    @Override
    public void syncInputAndLocks(@NotNull ServerPlayer player, IntList slotsChanged, TargetUpdateType updateTargets) {
        get().syncInputAndLocks(player, slotsChanged, updateTargets);
    }

    @Override
    public void receiveInputsAndLocks(Int2ObjectMap<ItemStack> changes) {
        get().receiveInputsAndLocks(changes);
    }
}
