package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;

import moze_intel.projecte.api.proxy.IEMCProxy;

public final class TransmutationPattern implements IPatternDetails {
    private final Item output;
    private final long amount;
    private final int tier;
    private final int job;

    private final AEItemKey definition;

    public TransmutationPattern(Item output, long amount, int job) {
        tier = 1;

        var definition = AppliedE.DUMMY_EMC_ITEM.toStack();
        definition.set(
                AppliedE.ENCODED_TRANSMUTATION_PATTERN.get(),
                new Encoded(this.output = output, this.amount = amount, tier, this.job = job));
        this.definition = AEItemKey.of(definition);
    }

    public TransmutationPattern(int tier) {
        output = Items.AIR;
        amount = 1;
        job = 0;

        var definition = AppliedE.DUMMY_EMC_ITEM.toStack();
        definition.set(
                AppliedE.ENCODED_TRANSMUTATION_PATTERN.get(), new Encoded(output, amount, this.tier = tier, job));
        this.definition = AEItemKey.of(definition);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        if (output == Items.AIR) {
            return new IInput[] {new Input(1, tier)};
        }

        var inputs = new ArrayList<IInput>();
        var itemEmc = IEMCProxy.INSTANCE.getValue(output);
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
                output != Items.AIR
                        ? new GenericStack(AEItemKey.of(output), amount)
                        : new GenericStack(EMCKey.of(tier - 1), AppliedE.TIER_LIMIT.longValue()));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TransmutationPattern pattern
                && pattern.output.equals(output)
                && pattern.amount == amount
                && pattern.tier == tier
                && pattern.job == job;
    }

    @Override
    public int hashCode() {
        return Objects.hash(output, amount, tier, job);
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

    public record Encoded(Item output, long amount, int tier, int job) {
        public static final Codec<Encoded> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("output").forGetter(Encoded::output),
                        Codec.LONG.fieldOf("amount").forGetter(Encoded::amount),
                        Codec.INT.fieldOf("tier").forGetter(Encoded::tier),
                        Codec.INT.fieldOf("job").forGetter(Encoded::job))
                .apply(builder, Encoded::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Encoded> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM),
                Encoded::output,
                ByteBufCodecs.VAR_LONG,
                Encoded::amount,
                ByteBufCodecs.VAR_INT,
                Encoded::tier,
                ByteBufCodecs.VAR_INT,
                Encoded::job,
                Encoded::new);
    }
}
