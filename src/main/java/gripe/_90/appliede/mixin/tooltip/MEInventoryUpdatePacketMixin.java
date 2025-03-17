package gripe._90.appliede.mixin.tooltip;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.RegistryFriendlyByteBuf;

import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.menu.me.common.GridInventoryEntry;

import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;

@Mixin(value = MEInventoryUpdatePacket.class, priority = 500)
public abstract class MEInventoryUpdatePacketMixin {
    @ModifyReturnValue(method = "readEntry", at = @At("RETURN"))
    private static GridInventoryEntry readTransmutable(
            GridInventoryEntry original, @Local(argsOnly = true) RegistryFriendlyByteBuf buffer) {
        ((GridInventoryEMCEntry) original).appliede$setTransmutable(buffer.readBoolean());
        return original;
    }

    // spotless:off
    @Inject(
            method = "writeEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/common/GridInventoryEntry;isCraftable()Z",
                    shift = At.Shift.AFTER))
    // spotless:on
    private static void writeTransmutable(RegistryFriendlyByteBuf buffer, GridInventoryEntry entry, CallbackInfo ci) {
        buffer.writeBoolean(((GridInventoryEMCEntry) entry).appliede$isTransmutable());
    }
}
