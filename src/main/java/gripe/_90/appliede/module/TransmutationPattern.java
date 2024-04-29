package gripe._90.appliede.module;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.key.EMCKey;

import moze_intel.projecte.api.proxy.IEMCProxy;

public final class TransmutationPattern implements IPatternDetails {
    private static final String NBT_ITEM = "item";
    private static final String NBT_TIER = "tier";

    private final AEItemKey definition;

    @Nullable
    private final AEItemKey item;

    private final int tier;

    public TransmutationPattern(AEItemKey definition) {
        this.definition = definition;
        var tag = Objects.requireNonNull(definition.getTag());

        if (tag.contains(NBT_ITEM)) {
            item = AEItemKey.fromTag(tag.getCompound(NBT_ITEM));
            tier = 1;
        } else {
            item = null;
            tier = tag.getInt(NBT_TIER);
        }
    }

    public TransmutationPattern(@Nullable AEItemKey item, int tier) {
        this.item = item;
        this.tier = item != null ? 1 : tier;

        var tag = new CompoundTag();

        if (item != null) {
            tag.put(NBT_ITEM, item.toTag());
        }

        if (tier > 1) {
            tag.putInt(NBT_TIER, tier);
        }

        definition = AEItemKey.of(AppliedE.TRANSMUTATION_PATTERN.get(), tag);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return new IInput[] {new Input(item != null ? IEMCProxy.INSTANCE.getValue(item.getItem()) : 1, tier)};
    }

    @Override
    public GenericStack[] getOutputs() {
        return new GenericStack[] {
            item != null
                    ? new GenericStack(item, 1)
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
