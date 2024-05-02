package gripe._90.appliede.mixin.tooltip;

import com.google.common.collect.BiMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import appeng.client.gui.me.common.Repo;
import appeng.menu.me.common.GridInventoryEntry;

import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;

@Mixin(value = Repo.class, remap = false)
public abstract class RepoMixin {
    @Shadow
    @Final
    private BiMap<Long, GridInventoryEntry> entries;

    // spotless:off
    @SuppressWarnings("UnreachableCode")
    @Inject(
            method = "handleUpdate(Lappeng/menu/me/common/GridInventoryEntry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/common/GridInventoryEntry;<init>(JLappeng/api/stacks/AEKey;JJZ)V",
                    shift = At.Shift.AFTER),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    // spotless:on
    private void setServerEntryTransmutable(
            GridInventoryEntry serverEntry, CallbackInfo ci, GridInventoryEntry localEntry) {
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
