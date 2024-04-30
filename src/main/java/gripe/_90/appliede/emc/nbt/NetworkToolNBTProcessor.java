package gripe._90.appliede.emc.nbt;

import java.math.BigInteger;

import org.jetbrains.annotations.NotNull;

import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.tools.NetworkToolItem;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;

@SuppressWarnings("unused")
@NBTProcessor
public class NetworkToolNBTProcessor implements INBTProcessor {
    @Override
    public String getName() {
        return "AE2NetworkToolProcessor";
    }

    @Override
    public String getDescription() {
        return "Calculates EMC value of the Applied Energistics 2 network tool.";
    }

    @Override
    public long recalculateEMC(@NotNull ItemInfo itemInfo, long currentEmc) throws ArithmeticException {
        if (!(itemInfo.getItem() instanceof NetworkToolItem)) {
            return currentEmc;
        }

        var stack = itemInfo.createStack();
        var bigEmc = BigInteger.valueOf(currentEmc);
        var inventory = new NetworkToolMenuHost(null, -1, stack, null).getInventory();

        for (var item : inventory) {
            var itemEmc = IEMCProxy.INSTANCE.getValue(item);
            bigEmc = bigEmc.add(BigInteger.valueOf(itemEmc).multiply(BigInteger.valueOf(item.getCount())));
        }

        return AppliedE.clampedLong(bigEmc);
    }
}
