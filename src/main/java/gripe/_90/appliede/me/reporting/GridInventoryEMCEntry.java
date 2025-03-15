package gripe._90.appliede.me.reporting;

import net.minecraft.network.RegistryFriendlyByteBuf;

import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;

public interface GridInventoryEMCEntry {
    boolean appliede$isTransmutable();

    void appliede$setTransmutable(boolean extractable);

    @SuppressWarnings("UnreachableCode")
    static GridInventoryEntry readEntry(RegistryFriendlyByteBuf buffer) {
        var serial = buffer.readVarLong();
        var what = AEKey.readOptionalKey(buffer);
        var storedAmount = buffer.readVarLong();
        var requestableAmount = buffer.readVarLong();
        var craftable = buffer.readBoolean();
        var transmutable = buffer.readBoolean();

        var entry = new GridInventoryEntry(serial, what, storedAmount, requestableAmount, craftable);
        ((GridInventoryEMCEntry) entry).appliede$setTransmutable(transmutable);
        return entry;
    }

    static void writeEntry(RegistryFriendlyByteBuf buffer, GridInventoryEntry entry) {
        buffer.writeVarLong(entry.getSerial());
        AEKey.writeOptionalKey(buffer, entry.getWhat());
        buffer.writeVarLong(entry.getStoredAmount());
        buffer.writeVarLong(entry.getRequestableAmount());
        buffer.writeBoolean(entry.isCraftable());
        buffer.writeBoolean(((GridInventoryEMCEntry) entry).appliede$isTransmutable());
    }
}
