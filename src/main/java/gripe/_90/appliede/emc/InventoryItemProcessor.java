package gripe._90.appliede.emc;

import java.math.BigInteger;

import org.jetbrains.annotations.NotNull;

import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.tools.NetworkToolItem;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;

@SuppressWarnings("unused")
@NBTProcessor
public class InventoryItemProcessor implements INBTProcessor {
    @Override
    public String getName() {
        return "AE2InventoryItemProcessor";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Calculates EMC value of Applied Energistics 2 inventory items such as cells and wireless terminals.";
    }

    @Override
    public long recalculateEMC(@NotNull ItemInfo itemInfo, long currentEmc) throws ArithmeticException {
        if (itemInfo.getItem() instanceof NetworkToolItem) {
            var stack = itemInfo.createStack();
            var bigEmc = BigInteger.valueOf(currentEmc);
            var inventory = new NetworkToolMenuHost(null, -1, stack, null).getInventory();

            for (var item : inventory) {
                var itemEmc = IEMCProxy.INSTANCE.getValue(item);
                bigEmc = bigEmc.add(BigInteger.valueOf(itemEmc).multiply(BigInteger.valueOf(item.getCount())));
            }

            return AppliedE.clampedLong(bigEmc);
        }

        if (!(itemInfo.getItem() instanceof IUpgradeableItem upgradeable)) {
            return currentEmc;
        }

        var stack = itemInfo.createStack();
        var bigEmc = BigInteger.valueOf(currentEmc);

        for (var upgrade : upgradeable.getUpgrades(stack)) {
            bigEmc = bigEmc.add(BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(upgrade)));
        }

        var cell = StorageCells.getCellInventory(itemInfo.createStack(), null);

        if (cell == null) {
            return AppliedE.clampedLong(bigEmc);
        }

        for (var key : cell.getAvailableStacks()) {
            if (key.getKey() instanceof AEItemKey item) {
                var keyEmc = IEMCProxy.INSTANCE.getValue(item.toStack());
                bigEmc = bigEmc.add(BigInteger.valueOf(keyEmc).multiply(BigInteger.valueOf(key.getLongValue())));
            }
        }

        return AppliedE.clampedLong(bigEmc);
    }
}
