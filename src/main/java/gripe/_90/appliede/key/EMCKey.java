package gripe._90.appliede.key;

import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import gripe._90.appliede.AppliedE;

public final class EMCKey extends AEKey {
    private final int tier;

    private EMCKey(int tier) {
        if (tier <= 0) {
            throw new IllegalArgumentException("Tier must be positive");
        }

        this.tier = tier;
    }

    public static EMCKey tier(int tier) {
        return new EMCKey(tier);
    }

    public static EMCKey base() {
        return tier(1);
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
    public CompoundTag toTag() {
        var tag = new CompoundTag();
        tag.putInt("tier", tier);
        return tag;
    }

    @Override
    public Object getPrimaryKey() {
        return tier;
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation("projecte", "emc_" + tier);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
        data.writeVarInt(tier);
    }

    @Override
    protected Component computeDisplayName() {
        return Component.translatable("key." + AppliedE.MODID + ".emc" + (tier == 1 ? "" : "_tiered"), tier);
    }

    @Override
    public void addDrops(long l, List<ItemStack> list, Level level, BlockPos blockPos) {}

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EMCKey key && tier == key.tier;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tier);
    }
}
