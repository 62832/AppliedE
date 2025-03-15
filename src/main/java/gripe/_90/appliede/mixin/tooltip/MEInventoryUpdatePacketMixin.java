package gripe._90.appliede.mixin.tooltip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.RegistryFriendlyByteBuf;

import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.menu.me.common.GridInventoryEntry;

import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;

@Mixin(MEInventoryUpdatePacket.class)
public abstract class MEInventoryUpdatePacketMixin {
    // spotless:off
    @Redirect(
            method = "decodeEntriesPayload",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/network/clientbound/MEInventoryUpdatePacket;readEntry(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lappeng/menu/me/common/GridInventoryEntry;"))
    // spotless:on
    private static GridInventoryEntry readEntryWithEmc(RegistryFriendlyByteBuf buffer) {
        return GridInventoryEMCEntry.readEntry(buffer);
    }
}
