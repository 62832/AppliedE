package gripe._90.appliede.me.key;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;

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
