package gripe._90.appliede.mixin.tooltip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.network.FriendlyByteBuf;

import appeng.core.sync.BasePacket;

@Mixin(value = BasePacket.class, remap = false)
public interface BasePacketAccessor {
    @Invoker
    void invokeConfigureWrite(FriendlyByteBuf data);
}
