package gripe._90.appliede.key;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import gripe._90.appliede.AppliedE;

public final class EMCKeyType extends AEKeyType {
    public static final EMCKeyType TYPE = new EMCKeyType();
    private static final Component EMC = Component.translatable("key." + AppliedE.MODID + ".emc");

    private EMCKeyType() {
        super(AppliedE.id("emc"), EMCKey.class, EMC);
    }

    @Override
    public AEKey readFromPacket(FriendlyByteBuf input) {
        return EMCKey.tier(input.readVarInt());
    }

    @Override
    public AEKey loadKeyFromTag(CompoundTag tag) {
        return EMCKey.tier(tag.getInt("tier"));
    }

    @Override
    public int getAmountPerByte() {
        return Integer.MAX_VALUE;
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
