package gripe._90.appliede.module;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.items.IItemHandler;

import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.pattern.TransmutationPattern;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

public class CompositeKnowledgeProvider implements IKnowledgeProvider {
    private static final String UNSUPPORTED_SETTER = "Attempted to call setter method on composite provider";
    private static final String UNSUPPORTED_FUNCTIONALITY = "Composite provider does not support method %s";

    private final Map<UUID, IKnowledgeProvider> cachedProviders = new Object2ObjectLinkedOpenHashMap<>();
    private final Set<IGridNode> moduleNodes = new ObjectLinkedOpenHashSet<>();
    private final List<IPatternDetails> patterns = new ObjectArrayList<>();

    public CompositeKnowledgeProvider() {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent event) -> {
            if (event instanceof PlayerEvent.PlayerLoggedInEvent || event instanceof PlayerEvent.PlayerLoggedOutEvent) {
                cachedProviders.clear();

                for (var node : moduleNodes) {
                    var uuid = node.getOwningPlayerProfileId();

                    if (uuid != null) {
                        cachedProviders.put(uuid, ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(uuid));
                    }
                }
            }
        });
    }

    Set<IKnowledgeProvider> getProviders() {
        return cachedProviders.values().stream().collect(Collectors.toUnmodifiableSet());
    }

    List<IPatternDetails> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    void addNode(IGridNode node) {
        moduleNodes.add(node);
        var uuid = node.getOwningPlayerProfileId();

        if (uuid != null) {
            cachedProviders.put(uuid, ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(uuid));
        }

        recalculatePatterns();
    }

    void removeNode(IGridNode node) {
        moduleNodes.remove(node);
        var uuid = node.getOwningPlayerProfileId();

        if (uuid != null) {
            cachedProviders.remove(uuid);
        }

        recalculatePatterns();
    }

    private void recalculatePatterns() {
        patterns.clear();
        var emc = getEmc();
        var highestTier = 1;

        while (emc.divide(AppliedE.TIER_LIMIT).signum() == 1) {
            emc = emc.divide(AppliedE.TIER_LIMIT);
            highestTier++;
        }

        for (var tier = highestTier; tier > 1; tier--) {
            patterns.add(new TransmutationPattern(null, tier));
        }

        for (var item : getKnowledge()) {
            patterns.add(new TransmutationPattern(AEItemKey.of(item.getItem()), 1));
        }
    }

    void syncAllEmc(MinecraftServer server) {
        cachedProviders.forEach((uuid, provider) -> {
            var id = IPlayerRegistry.getMapping(server).getPlayerId(uuid);
            var player = IPlayerRegistry.getConnected(server, id);

            if (player != null) {
                provider.syncEmc(player);
            }
        });
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemInfo itemInfo) {
        return getProviders().stream().anyMatch(provider -> provider.hasKnowledge(itemInfo));
    }

    @Override
    public boolean hasFullKnowledge() {
        return getProviders().stream().anyMatch(IKnowledgeProvider::hasFullKnowledge);
    }

    @Override
    public @NotNull Set<ItemInfo> getKnowledge() {
        return getProviders().stream()
                .flatMap(provider -> provider.getKnowledge().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public BigInteger getEmc() {
        return getProviders().stream().map(IKnowledgeProvider::getEmc).reduce(BigInteger.ZERO, BigInteger::add);
    }

    @Override
    public void setFullKnowledge(boolean b) {
        throw new UnsupportedOperationException(UNSUPPORTED_SETTER);
    }

    @Override
    public void clearKnowledge() {
        throw new UnsupportedOperationException(UNSUPPORTED_SETTER);
    }

    @Override
    public boolean addKnowledge(@NotNull ItemInfo itemInfo) {
        throw new UnsupportedOperationException(UNSUPPORTED_SETTER);
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemInfo itemInfo) {
        throw new UnsupportedOperationException(UNSUPPORTED_SETTER);
    }

    @Override
    public void setEmc(BigInteger bigInteger) {
        throw new UnsupportedOperationException(UNSUPPORTED_SETTER);
    }

    @Override
    public void sync(@NotNull ServerPlayer serverPlayer) {
        throw new UnsupportedOperationException(UNSUPPORTED_FUNCTIONALITY.formatted("sync"));
    }

    @Override
    public void syncEmc(@NotNull ServerPlayer serverPlayer) {
        throw new UnsupportedOperationException(UNSUPPORTED_FUNCTIONALITY.formatted("syncEmc"));
    }

    @Override
    public void syncKnowledgeChange(@NotNull ServerPlayer serverPlayer, ItemInfo itemInfo, boolean b) {
        throw new UnsupportedOperationException(UNSUPPORTED_FUNCTIONALITY.formatted("syncKnowledgeChange"));
    }

    @NotNull
    @Override
    public IItemHandler getInputAndLocks() {
        throw new UnsupportedOperationException(UNSUPPORTED_FUNCTIONALITY.formatted("getInputAndLocks"));
    }

    @Override
    public void syncInputAndLocks(
            @NotNull ServerPlayer serverPlayer, List<Integer> list, TargetUpdateType targetUpdateType) {
        throw new UnsupportedOperationException(UNSUPPORTED_FUNCTIONALITY.formatted("syncInputAndLocks"));
    }

    @Override
    public void receiveInputsAndLocks(Map<Integer, ItemStack> map) {
        throw new UnsupportedOperationException(UNSUPPORTED_FUNCTIONALITY.formatted("receiveInputAndLocks"));
    }

    @Override
    public CompoundTag serializeNBT() {
        throw new UnsupportedOperationException(UNSUPPORTED_FUNCTIONALITY.formatted("serializeNBT"));
    }

    @Override
    public void deserializeNBT(CompoundTag data) {
        throw new UnsupportedOperationException(UNSUPPORTED_FUNCTIONALITY.formatted("deserializeNBT"));
    }
}
