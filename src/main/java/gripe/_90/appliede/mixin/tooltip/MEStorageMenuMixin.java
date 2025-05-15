package gripe._90.appliede.mixin.tooltip;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.ModifyReceiver;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.ITerminalHost;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.common.MEStorageMenu;

import gripe._90.appliede.api.KnowledgeService;
import gripe._90.appliede.me.reporting.TransmutablePacketBuilder;

@Mixin(MEStorageMenu.class)
public abstract class MEStorageMenuMixin extends AEBaseMenu {
    @Shadow
    @Final
    private IncrementalUpdateHelper updateHelper;

    @Unique
    private Set<AEItemKey> appliede$transmutables = new HashSet<>();

    @Unique
    private Set<AEItemKey> appliede$previousTransmutables = new HashSet<>();

    public MEStorageMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, ITerminalHost host) {
        super(menuType, id, playerInventory, host);
    }

    @Shadow
    protected abstract boolean showsCraftables();

    @Shadow
    public abstract @Nullable IGridNode getGridNode();

    @Unique
    private Set<AEItemKey> appliede$getTransmutablesFromGrid() {
        if (!showsCraftables()) {
            return Collections.emptySet();
        }

        return getGridNode() != null && getGridNode().isActive()
                ? getGridNode().getGrid().getService(KnowledgeService.class).getKnownItems()
                : Collections.emptySet();
    }

    @Inject(
            method = "broadcastChanges",
            at = @At(value = "INVOKE", target = "Lappeng/menu/me/common/IncrementalUpdateHelper;hasChanges()Z"))
    private void addTransmutables(CallbackInfo ci) {
        appliede$transmutables = appliede$getTransmutablesFromGrid();
        Sets.difference(appliede$previousTransmutables, appliede$transmutables).forEach(updateHelper::addChange);
        Sets.difference(appliede$transmutables, appliede$previousTransmutables).forEach(updateHelper::addChange);
    }

    // spotless:off
    @ModifyReceiver(
            method = "broadcastChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/network/clientbound/MEInventoryUpdatePacket$Builder;setFilter(Lappeng/api/storage/AEKeyFilter;)V"))
    // spotless:on
    private MEInventoryUpdatePacket.Builder addTransmutables(
            MEInventoryUpdatePacket.Builder builder, AEKeyFilter filter) {
        ((TransmutablePacketBuilder) builder).appliede$addTransmutables(appliede$transmutables);
        return builder;
    }

    @Inject(
            method = "broadcastChanges",
            at = @At(value = "INVOKE", target = "Lappeng/menu/AEBaseMenu;broadcastChanges()V"))
    private void addPreviousTransmutables(CallbackInfo ci) {
        appliede$previousTransmutables = ImmutableSet.copyOf(appliede$transmutables);
    }
}
