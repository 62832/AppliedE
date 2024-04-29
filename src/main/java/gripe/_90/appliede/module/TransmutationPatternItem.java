package gripe._90.appliede.module;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.EncodedPatternItem;

public final class TransmutationPatternItem extends EncodedPatternItem {
    public TransmutationPatternItem() {
        super(new Item.Properties());
    }

    @Nullable
    @Override
    public IPatternDetails decode(ItemStack stack, Level level, boolean tryRecovery) {
        return decode(AEItemKey.of(stack), level);
    }

    @Nullable
    @Override
    public IPatternDetails decode(AEItemKey what, Level level) {
        if (what == null || !(what.hasTag())) {
            return null;
        }

        try {
            return new TransmutationPattern(what);
        } catch (Exception e) {
            return null;
        }
    }
}
