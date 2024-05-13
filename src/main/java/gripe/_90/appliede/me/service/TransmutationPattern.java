package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

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

    private final AEItemKey definition;

    @Nullable
    private final AEItemKey item;

    private final long amount;
    private final int tier;

    public TransmutationPattern(AEItemKey definition) {
        this.definition = definition;
        var tag = Objects.requireNonNull(definition.getTag());

        if (tag.contains(NBT_ITEM)) {
            item = AEItemKey.fromTag(tag.getCompound(NBT_ITEM));
            amount = tag.getLong(NBT_AMOUNT);
            tier = 1;
        } else {
            item = null;
            amount = 1;
            tier = tag.getInt(NBT_TIER);
        }
    }

    public TransmutationPattern(@Nullable AEItemKey item, long amount, int tier) {
        this.item = item;
        this.amount = item != null ? amount : 1;
        this.tier = item != null ? 1 : tier;

        var tag = new CompoundTag();

        if (item != null) {
            tag.put(NBT_ITEM, item.toTag());
            tag.putLong(NBT_AMOUNT, amount);
        }

        if (tier > 1) {
            tag.putInt(NBT_TIER, tier);
        }

        definition = AEItemKey.of(AppliedE.TRANSMUTATION_PATTERN.get(), tag);
    }

    public TransmutationPattern(int tier) {
        this(null, 1, tier);
    }

    public TransmutationPattern(AEItemKey item, long amount) {
        this(item, amount, 1);
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

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
