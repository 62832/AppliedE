package gripe._90.appliede.emc;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import appeng.api.storage.StorageCells;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.items.tools.NetworkToolItem;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;

@SuppressWarnings("unused")
@DataComponentProcessor
public class InventoryItemProcessor implements IDataComponentProcessor {
    @Override
    public String getTranslationKey() {
        return "config." + AppliedE.MODID + ".mapper.inventory";
    }

    @Override
    public String getName() {
        return "Inventory Item Processor";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Calculates EMC value of Applied Energistics 2 inventory items such as cells and wireless terminals.";
    }

    @Override
    public long recalculateEMC(@NotNull ItemInfo itemInfo, long currentEmc) throws ArithmeticException {
        var stack = itemInfo.createStack();

        if (stack.getItem() instanceof NetworkToolItem) {
            var inventory = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

            for (var item : inventory.nonEmptyItems()) {
                currentEmc = addEmc(currentEmc, item);
            }

            return currentEmc;
        } else {
            var cell = StorageCells.getCellInventory(stack, null);

            if (cell != null && !cell.getAvailableStacks().isEmpty()) {
                return 0;
            }

            if (!(stack.getItem() instanceof IUpgradeableItem upgradeable)) {
                return currentEmc;
            }

            for (var upgrade : upgradeable.getUpgrades(stack)) {
                currentEmc = addEmc(currentEmc, upgrade);
            }
        }

        return currentEmc;
    }

    private long addEmc(long currentEmc, ItemStack stack) throws ArithmeticException {
        var itemEmc = IEMCProxy.INSTANCE.getValue(stack);

        if (itemEmc > 0) {
            var stackEmc = Math.multiplyExact(itemEmc, stack.getCount());
            currentEmc = Math.addExact(currentEmc, stackEmc);
        }

        return currentEmc;
    }
}
