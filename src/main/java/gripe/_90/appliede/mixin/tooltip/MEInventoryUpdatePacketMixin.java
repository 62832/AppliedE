package gripe._90.appliede.mixin.tooltip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.FriendlyByteBuf;

import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.menu.me.common.GridInventoryEntry;

import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;

@Mixin(value = MEInventoryUpdatePacket.class, remap = false)
public abstract class MEInventoryUpdatePacketMixin {
    // spotless:off
    @Redirect(
            method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/sync/packets/MEInventoryUpdatePacket;readEntry(Lnet/minecraft/network/FriendlyByteBuf;)Lappeng/menu/me/common/GridInventoryEntry;"))
    // spotless:on
    private GridInventoryEntry readEntryWithEmc(FriendlyByteBuf buffer) {
        return GridInventoryEMCEntry.readEntry(buffer);
    }
}
