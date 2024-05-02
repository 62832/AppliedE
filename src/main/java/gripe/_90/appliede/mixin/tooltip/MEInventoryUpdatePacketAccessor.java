package gripe._90.appliede.mixin.tooltip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import appeng.core.sync.packets.MEInventoryUpdatePacket;

@Mixin(MEInventoryUpdatePacket.class)
public interface MEInventoryUpdatePacketAccessor {
    @Invoker(value = "<init>", remap = false)
    static MEInventoryUpdatePacket create() {
        throw new AssertionError();
    }
}
