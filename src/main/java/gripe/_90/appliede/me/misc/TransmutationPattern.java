package gripe._90.appliede.me.misc;

import java.math.BigInteger;
import java.util.ArrayList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;

import moze_intel.projecte.api.proxy.IEMCProxy;

public final class TransmutationPattern implements IPatternDetails {
    private static final String NBT_ITEM = "item";
    private static final String NBT_AMOUNT = "amount";
    private static final String NBT_TIER = "tier";

    private final AEItemKey item;
    private final long amount;
    private final int tier;

    private final AEItemKey definition;

    public TransmutationPattern(AEItemKey item, long amount) {
        tier = 1;

        var tag = new CompoundTag();
        tag.put(NBT_ITEM, (this.item = item).toTag());
        tag.putLong(NBT_AMOUNT, this.amount = amount);
        definition = AEItemKey.of(AppliedE.DUMMY_EMC_ITEM.get(), tag);
    }

    public TransmutationPattern(int tier) {
        item = null;
        amount = 1;

        var tag = new CompoundTag();
        tag.putInt(NBT_TIER, this.tier = tier);
        definition = AEItemKey.of(AppliedE.DUMMY_EMC_ITEM.get(), tag);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        if (item == null) {
            return new IInput[] {new Input(1, tier)};
        }

        var inputs = new ArrayList<IInput>();
        var itemEmc = IEMCProxy.INSTANCE.getValue(item.toStack());
        var totalEmc = BigInteger.valueOf(itemEmc).multiply(BigInteger.valueOf(amount));
        var currentTier = 1;

        while (totalEmc.divide(AppliedE.TIER_LIMIT).signum() == 1) {
            inputs.add(new Input(totalEmc.remainder(AppliedE.TIER_LIMIT).longValue(), currentTier));
            totalEmc = totalEmc.divide(AppliedE.TIER_LIMIT);
            currentTier++;
        }

        inputs.add(new Input(totalEmc.longValue(), currentTier));

        return inputs.toArray(new IInput[0]);
    }

    @Override
    public GenericStack[] getOutputs() {
        return new GenericStack[] {
            item != null
                    ? new GenericStack(item, amount)
                    : new GenericStack(EMCKey.tier(tier - 1), AppliedE.TIER_LIMIT.longValue())
        };
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TransmutationPattern pattern && pattern.definition.equals(definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    private record Input(long amount, int tier) implements IInput {
        @Override
        public GenericStack[] getPossibleInputs() {
            return new GenericStack[] {new GenericStack(EMCKey.tier(tier), amount)};
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input.matches(getPossibleInputs()[0]);
        }

        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
