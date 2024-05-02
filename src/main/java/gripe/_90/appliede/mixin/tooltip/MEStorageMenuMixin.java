package gripe._90.appliede.mixin.tooltip;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.ITerminalHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.common.MEStorageMenu;

import gripe._90.appliede.me.reporting.MEInventoryUpdatePacketBuilder;
import gripe._90.appliede.me.service.KnowledgeService;

@Mixin(value = MEStorageMenu.class, remap = false)
public abstract class MEStorageMenuMixin extends AEBaseMenu {
    @Nullable
    @Shadow
    private IGridNode networkNode;

    @Shadow
    @Final
    private ITerminalHost host;

    @Shadow
    private Set<AEKey> previousCraftables;

    @Shadow
    private KeyCounter previousAvailableStacks;

    @Shadow
    @Final
    private IncrementalUpdateHelper updateHelper;

    @Unique
    private Set<AEItemKey> appliede$previousTransmutables = new HashSet<>();

    public MEStorageMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, ITerminalHost host) {
        super(menuType, id, playerInventory, host);
    }

    @Shadow
    protected abstract boolean showsCraftables();

    @Shadow
    public abstract boolean isKeyVisible(AEKey key);

    @Shadow
    protected abstract void updatePowerStatus();

    @Unique
    private Set<AEItemKey> appliede$getTransmutablesFromGrid() {
        var hostNode = networkNode;

        if (hostNode == null && host instanceof IActionHost actionHost) {
            hostNode = actionHost.getActionableNode();
        }

        if (!showsCraftables()) {
            return Collections.emptySet();
        }

        return hostNode != null && hostNode.isActive()
                ? hostNode.getGrid().getService(KnowledgeService.class).getKnownItems()
                : Collections.emptySet();
    }

    // spotless:off
    @SuppressWarnings("Convert2MethodRef")
    @Inject(
            method = "broadcastChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/common/IncrementalUpdateHelper;hasChanges()Z",
                    remap = false),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            remap = true)
    // spotless:on
    private void replacePacket(CallbackInfo ci, Set<AEKey> craftable, KeyCounter availableStacks) {
        ci.cancel();

        var transmutable = appliede$getTransmutablesFromGrid();
        Sets.difference(appliede$previousTransmutables, transmutable).forEach(updateHelper::addChange);
        Sets.difference(transmutable, appliede$previousTransmutables).forEach(updateHelper::addChange);

        if (updateHelper.hasChanges()) {
            var builder = new MEInventoryUpdatePacketBuilder(containerId, updateHelper.isFullUpdate());
            builder.setFilter(key -> isKeyVisible(key));
            builder.addChanges(updateHelper, availableStacks, craftable, new KeyCounter(), transmutable);
            builder.buildAndSend(packet -> sendPacketToClient(packet));
            updateHelper.commitChanges();
        }

        previousCraftables = ImmutableSet.copyOf(craftable);
        previousAvailableStacks = availableStacks;
        appliede$previousTransmutables = ImmutableSet.copyOf(transmutable);

        updatePowerStatus();
        super.broadcastChanges();
    }
}
