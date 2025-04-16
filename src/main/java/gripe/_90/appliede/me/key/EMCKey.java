package gripe._90.appliede.me.key;

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

public final class EMCKey extends AEKey {
    static final MapCodec<EMCKey> MAP_CODEC = Codec.INT.fieldOf("tier").xmap(EMCKey::of, key -> key.tier);
    static final Codec<EMCKey> CODEC = MAP_CODEC.codec();

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
