package gripe._90.appliede.emc;

import java.math.BigInteger;

import org.jetbrains.annotations.NotNull;

import appeng.api.upgrades.IUpgradeableItem;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;

@SuppressWarnings("unused")
@NBTProcessor
public class UpgradedItemProcessor implements INBTProcessor {
    @Override
    public String getName() {
        return "AE2UpgradeProcessor";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Calculates EMC value of Applied Energistics 2 items containing upgrades.";
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

        return AppliedE.clampedLong(bigEmc);
    }
}
