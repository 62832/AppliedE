package gripe._90.appliede.mappers;

import java.math.BigInteger;

import org.jetbrains.annotations.NotNull;

import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.me.cells.BasicCellInventory;

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
        return "Calculates EMC value of Applied Energistics 2 storage cells.";
    }

    @Override
    public boolean isAvailable() {
        return AppliedE.useCustomMapper();
    }

    @Override
    public long recalculateEMC(@NotNull ItemInfo itemInfo, long currentEmc) throws ArithmeticException {
        var cell = StorageCells.getCellInventory(itemInfo.createStack(), null);
        if (cell == null) return currentEmc;

        var bigEmc = BigInteger.valueOf(currentEmc);

        for (var key : cell.getAvailableStacks()) {
            if (key.getKey() instanceof AEItemKey item) {
                var keyEmc = IEMCProxy.INSTANCE.getValue(item.getItem());
                bigEmc = bigEmc.add(BigInteger.valueOf(keyEmc).multiply(BigInteger.valueOf(key.getLongValue())));
            }
        }

        // TODO: See about adding upgrade inventories to the StorageCell interface
        if (cell instanceof BasicCellInventory basicCell) {
            for (var upgrade : basicCell.getUpgradesInventory()) {
                bigEmc = bigEmc.add(BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(upgrade)));
            }
        }

        return AppliedE.clampedLong(bigEmc);
    }
}
