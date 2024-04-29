package gripe._90.appliede.key;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.RegisterEvent;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;

import gripe._90.appliede.AppliedE;

public final class EMCKeyType extends AEKeyType {
    static final Component EMC = Component.translatable("key." + AppliedE.MODID + ".emc");
    public static final EMCKeyType TYPE = new EMCKeyType();

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            AEKeyTypes.register(TYPE);
        }
    }

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
}
