package gripe._90.appliede.emc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import appeng.api.implementations.items.IFacadeItem;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;

@SuppressWarnings("unused")
@NBTProcessor
public class FacadeProcessor implements INBTProcessor {
    @Override
    public String getName() {
        return "AE2FacadeProcessor";
    }

    @Override
    public String getDescription() {
        return "Calculates EMC value of Applied Energistics 2 cable facades.";
    }

    @Override
    public boolean hasPersistentNBT() {
        return true;
    }

    @Override
    public long recalculateEMC(@NotNull ItemInfo itemInfo, long currentEmc) throws ArithmeticException {
        if (!(itemInfo.getItem() instanceof IFacadeItem facade)) {
            return currentEmc;
        }

        var textureItem = facade.getTextureItem(itemInfo.createStack());
        var textureEmc = IEMCProxy.INSTANCE.getValue(textureItem);
        var anchorEmc = IEMCProxy.INSTANCE.getValue(AEParts.CABLE_ANCHOR.asItem());
        return anchorEmc + textureEmc / 4;
    }

    @Nullable
    @Override
    public CompoundTag getPersistentNBT(@NotNull ItemInfo info) {
        if (AEItems.FACADE.isSameAs(info.createStack())) {
            var tag = info.getNBT();

            if (tag != null && tag.contains("item", Tag.TAG_STRING)) {
                var toReturn = new CompoundTag();
                toReturn.putString("item", tag.getString("item"));
                return toReturn;
            }
        }

        return null;
    }
}
