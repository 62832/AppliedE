package gripe._90.appliede.mixin.tooltip;

import com.google.common.collect.BiMap;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.me.common.Repo;
import appeng.menu.me.common.GridInventoryEntry;

import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;

@Mixin(Repo.class)
public abstract class RepoMixin {
    @Shadow
    @Final
    private BiMap<Long, GridInventoryEntry> entries;

    // spotless:off
    @Inject(
            method = "handleUpdate(Lappeng/menu/me/common/GridInventoryEntry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/common/GridInventoryEntry;<init>(JLappeng/api/stacks/AEKey;JJZ)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    // spotless:on
    private void setServerEntryTransmutable(
            GridInventoryEntry serverEntry,
            CallbackInfo ci,
            @Local(name = "localEntry") GridInventoryEntry localEntry) {
        ci.cancel();

        var entry = new GridInventoryEntry(
                serverEntry.getSerial(),
                localEntry.getWhat(),
                serverEntry.getStoredAmount(),
                serverEntry.getRequestableAmount(),
                serverEntry.isCraftable());
        ((GridInventoryEMCEntry) entry)
                .appliede$setTransmutable(((GridInventoryEMCEntry) serverEntry).appliede$isTransmutable());
        entries.put(serverEntry.getSerial(), entry);
    }
}
