package gripe._90.appliede.api;

import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import gripe._90.appliede.AppliedE;

/**
 * Represents <i>ProjectE</i>'s "EMC" as a key resource within ME networks.
 *
 * <p>Accounting for the fact that EMC is typically stored as a {@link java.math.BigInteger BigInteger} while ME
 * networks may only support {@code long} amounts of a given resource/stack, this key uses a "tier" approach to
 * represent larger amounts of EMC, which a network may otherwise be unable to cope with. The "base" EMC key corresponds
 * to some amount of a single (1 EMC) unit as the first tier, up to a maximum of <i>10 trillion</i> ({@code 10^12}) for
 * its corresponding stack/entry.</p>
 *
 * <p>Successive tier entries each represent higher powers of 10 trillion, so an "EMC^2" entry, for example, will have
 * its reported amount correspond to how many lots of {@code 10^12} EMC the system is currently tracking. Similarly, an
 * "EMC^3" entry would represent amounts of {@code (10^12)^2} EMC, with the general progression of "EMC^<b>n</b>"
 * representing amounts of {@code (10^12)^(n - 1)} EMC currently in the system.</p>
 *
 * <p>See also: {@link EMCKeyType}</p>
 */
public final class EMCKey extends AEKey {
    static final MapCodec<EMCKey> MAP_CODEC = Codec.INT.fieldOf("tier").xmap(EMCKey::of, key -> key.tier);
    private static final Codec<EMCKey> CODEC = MAP_CODEC.codec();

    public static final EMCKey BASE = new EMCKey(1);

    private final int tier;

    private EMCKey(int tier) {
        if (tier <= 0) {
            throw new IllegalArgumentException("Tier must be non-negative");
        }

        this.tier = tier;
    }

    public static EMCKey of(int tier) {
        return tier == 1 ? BASE : new EMCKey(tier);
    }

    public int getTier() {
        return tier;
    }

    @Override
    public AEKeyType getType() {
        return EMCKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public CompoundTag toTag(HolderLookup.Provider registries) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return (CompoundTag) CODEC.encodeStart(ops, this).getOrThrow();
    }

    @Override
    public Object getPrimaryKey() {
        return tier;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("projecte", "emc_" + tier);
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        data.writeVarInt(tier);
    }

    @Override
    protected Component computeDisplayName() {
        return Component.translatable("key." + AppliedE.MODID + ".emc" + (tier == 1 ? "" : "_tiered"), tier);
    }

    @Override
    public boolean hasComponents() {
        return true;
    }

    @Override
    public void addDrops(long l, List<ItemStack> list, Level level, BlockPos blockPos) {}

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass() && ((EMCKey) o).tier == tier;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tier);
    }
}
