package gripe._90.appliede.mappers;

import java.math.BigInteger;

import org.jetbrains.annotations.NotNull;

import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.api.upgrades.IUpgradeableItem;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;

@SuppressWarnings("unused")
@NBTProcessor
public class StorageCellNBTProcessor implements INBTProcessor {
    @Override
    public String getName() {
        return "AE2CellProcessor";
    }

    @Override
    public String getDescription() {
        return "Calculates EMC value of Applied Energistics 2 storage cells (and terminals).";
    }

    @Override
    public boolean isAvailable() {
        return AppliedE.useCustomMapper();
    }

    @Override
    public long recalculateEMC(@NotNull ItemInfo itemInfo, long currentEmc) throws ArithmeticException {
        if (!(itemInfo.getItem() instanceof IUpgradeableItem upgradeable)) {
            return currentEmc;
        }

        var stack = itemInfo.createStack();
        var bigEmc = BigInteger.valueOf(currentEmc);

        for (var upgrade : upgradeable.getUpgrades(stack)) {
            bigEmc = bigEmc.add(BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(upgrade)));
        }

        var cell = StorageCells.getCellInventory(stack, null);

        if (cell == null) {
            return AppliedE.clampedLong(bigEmc);
        }

        for (var key : cell.getAvailableStacks()) {
            if (key.getKey() instanceof AEItemKey item) {
                var keyEmc = IEMCProxy.INSTANCE.getValue(item.getItem());
                bigEmc = bigEmc.add(BigInteger.valueOf(keyEmc).multiply(BigInteger.valueOf(key.getLongValue())));
            }
        }

        return AppliedE.clampedLong(bigEmc);
    }
}
