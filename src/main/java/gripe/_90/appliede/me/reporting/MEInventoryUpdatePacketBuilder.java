package gripe._90.appliede.me.reporting;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.RegistryAccess;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IncrementalUpdateHelper;

import gripe._90.appliede.me.key.EMCKey;

public class MEInventoryUpdatePacketBuilder extends MEInventoryUpdatePacket.Builder {
    @Nullable
    private AEKeyFilter filter;

    public MEInventoryUpdatePacketBuilder(int containerId, boolean fullUpdate, RegistryAccess access) {
        super(containerId, fullUpdate, access);
    }

    @Override
    public void setFilter(@Nullable AEKeyFilter filter) {
        this.filter = filter;
    }

    public void addChanges(
            IncrementalUpdateHelper updateHelper,
            KeyCounter networkStorage,
            Set<AEKey> craftables,
            KeyCounter requestables,
            Set<AEItemKey> transmutables) {
        for (AEKey key : updateHelper) {
            if (filter != null && !filter.matches(key)) {
                continue;
            }

            AEKey sendKey;
            var serial = updateHelper.getSerial(key);

            // Try to serialize the item into the buffer
            if (serial == null) {
                // This is a new key, not sent to the client
                sendKey = key;
                serial = updateHelper.getOrAssignSerial(key);
            } else {
                // This is an incremental update referring back to the serial
                sendKey = null;
            }

            // The queued changes are actual differences, but we need to send the real stored properties
            // to the client.
            var storedAmount = networkStorage.get(key);
            var craftable = craftables.contains(key) && !(key instanceof EMCKey);
            var requestable = requestables.get(key);
            var transmutable = key instanceof AEItemKey && transmutables.contains(key);

            GridInventoryEntry entry;

            if (storedAmount <= 0 && requestable <= 0 && !craftable) {
                // This happens when an update is queued but the item is no longer stored
                entry = new GridInventoryEntry(serial, sendKey, 0, 0, false);
                updateHelper.removeSerial(key);
            } else {
                entry = new GridInventoryEntry(serial, sendKey, storedAmount, requestable, craftable);
                ((GridInventoryEMCEntry) entry).appliede$setTransmutable(transmutable);
            }

            super.add(entry);
        }

        updateHelper.commitChanges();
    }
}
