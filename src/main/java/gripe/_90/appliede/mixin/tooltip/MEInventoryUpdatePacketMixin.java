package gripe._90.appliede.mixin.tooltip;

import java.util.HashSet;
import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.RegistryFriendlyByteBuf;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.menu.me.common.GridInventoryEntry;

import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;
import gripe._90.appliede.me.reporting.TransmutablePacketBuilder;

@Mixin(value = MEInventoryUpdatePacket.class, priority = 500)
public abstract class MEInventoryUpdatePacketMixin {
    @ModifyReturnValue(method = "readEntry", at = @At("RETURN"))
    private static GridInventoryEntry readTransmutable(
            GridInventoryEntry original, @Local(argsOnly = true) RegistryFriendlyByteBuf buffer) {
        ((GridInventoryEMCEntry) original).appliede$setTransmutable(buffer.readBoolean());
        return original;
    }

    @Inject(method = "writeEntry", at = @At(value = "RETURN"))
    private static void writeTransmutable(RegistryFriendlyByteBuf buffer, GridInventoryEntry entry, CallbackInfo ci) {
        buffer.writeBoolean(((GridInventoryEMCEntry) entry).appliede$isTransmutable());
    }

    @SuppressWarnings("unused")
    @Mixin(MEInventoryUpdatePacket.Builder.class)
    private abstract static class BuilderMixin implements TransmutablePacketBuilder {
        @Unique
        private Set<AEItemKey> appliede$transmutables = new HashSet<>();

        @Override
        public void appliede$addTransmutables(Set<AEItemKey> transmutables) {
            appliede$transmutables = transmutables;
        }

        // spotless:off
        @ModifyExpressionValue(
                method = "addChanges",
                at = @At(
                        value = "NEW",
                        target = "(JLappeng/api/stacks/AEKey;JJZ)Lappeng/menu/me/common/GridInventoryEntry;",
                        ordinal = 1))
        // spotless:on
        private GridInventoryEntry includeTransmutables(GridInventoryEntry original, @Local(ordinal = 0) AEKey key) {
            ((GridInventoryEMCEntry) original)
                    .appliede$setTransmutable(key instanceof AEItemKey item && appliede$transmutables.contains(item));
            return original;
        }
    }
}
