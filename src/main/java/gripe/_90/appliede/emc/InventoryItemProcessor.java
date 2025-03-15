package gripe._90.appliede.emc;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;

import appeng.api.storage.StorageCells;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.locator.MenuLocators;

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
            var inventory = new NetworkToolMenuHost<>(null, null, MenuLocators.forStack(stack), null).getInventory();

            for (var item : inventory) {
                currentEmc = addEmc(currentEmc, item);
            }

            return currentEmc;
        }

        if (!(stack.getItem() instanceof IUpgradeableItem upgradeable)) {
            return currentEmc;
        }

        for (var upgrade : upgradeable.getUpgrades(stack)) {
            currentEmc = addEmc(currentEmc, upgrade);
        }

        var cell = StorageCells.getCellInventory(stack, null);

        if (cell != null && !cell.getAvailableStacks().isEmpty()) {
            return 0;
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
