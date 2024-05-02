package gripe._90.appliede.mixin.tooltip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import appeng.menu.me.common.GridInventoryEntry;

import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;

@Mixin(GridInventoryEntry.class)
public abstract class GridInventoryEntryMixin implements GridInventoryEMCEntry {
    @Unique
    private boolean appliede$transmutable = false;

    @Unique
    @Override
    public boolean appliede$isTransmutable() {
        return appliede$transmutable;
    }

    @Unique
    @Override
    public void appliede$setTransmutable(boolean extractable) {
        appliede$transmutable = extractable;
    }
}
