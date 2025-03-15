package gripe._90.appliede.me.misc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;

import moze_intel.projecte.api.proxy.IEMCProxy;

public final class TransmutationPattern implements IPatternDetails {
    private final AEItemKey item;
    private final long amount;
    private final int tier;

    private final AEItemKey definition;

    public TransmutationPattern(AEItemKey item, long amount) {
        tier = 1;

        var definition = new ItemStack(AppliedE.DUMMY_EMC_ITEM.get());
        definition.set(
                AppliedE.ENCODED_TRANSMUTATION_PATTERN.get(),
                new Encoded((this.item = item).toStack(), this.amount = amount, tier));
        this.definition = AEItemKey.of(definition);
    }

    public TransmutationPattern(int tier) {
        item = null;
        amount = 1;

        var definition = new ItemStack(AppliedE.DUMMY_EMC_ITEM.get());
        definition.set(
                AppliedE.ENCODED_TRANSMUTATION_PATTERN.get(), new Encoded(ItemStack.EMPTY, amount, this.tier = tier));
        this.definition = AEItemKey.of(definition);
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
    public List<GenericStack> getOutputs() {
        return Collections.singletonList(
                item != null
                        ? new GenericStack(item, amount)
                        : new GenericStack(EMCKey.of(tier - 1), AppliedE.TIER_LIMIT.longValue()));
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
            return new GenericStack[] {new GenericStack(EMCKey.of(tier), amount)};
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

    public record Encoded(ItemStack item, long amount, int tier) {
        public static final Codec<Encoded> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                        ItemStack.CODEC.fieldOf("item").forGetter(Encoded::item),
                        Codec.LONG.fieldOf("amount").forGetter(Encoded::amount),
                        Codec.INT.fieldOf("tier").forGetter(Encoded::tier))
                .apply(builder, Encoded::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Encoded> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC,
                Encoded::item,
                ByteBufCodecs.VAR_LONG,
                Encoded::amount,
                ByteBufCodecs.VAR_INT,
                Encoded::tier,
                Encoded::new);
    }
}
