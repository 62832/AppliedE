package gripe._90.appliede.api;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;

/**
 * Defines other associated properties for {@link EMCKey}.
 */
public final class EMCKeyType extends AEKeyType {
    public static final EMCKeyType TYPE = new EMCKeyType();
    private static final Component EMC = Component.translatable("key." + AppliedE.MODID + ".emc");

    private EMCKeyType() {
        super(AppliedE.id("emc"), EMCKey.class, EMC);
    }

    @Override
    public MapCodec<? extends AEKey> codec() {
        return EMCKey.MAP_CODEC;
    }

    @Override
    public AEKey readFromPacket(RegistryFriendlyByteBuf input) {
        return EMCKey.of(input.readVarInt());
    }

    /**
     * Due to the sheer volume of EMC that may (or may not) be involved in some crafting jobs for more "complex" items,
     * the key type for EMC allows the user to configure how much EMC corresponds to 1 byte (1,000,000 EMC by default).
     * This prevents situations where such auto-crafting jobs would require an unreasonable amount of storage blocks for
     * any Crafting CPUs involved.
     */
    @Override
    public int getAmountPerByte() {
        return AppliedEConfig.CONFIG.getEmcPerByte();
    }

    @Override
    public int getAmountPerOperation() {
        return 2000;
    }

    @Override
    public Component getDescription() {
        return EMC;
    }
}
