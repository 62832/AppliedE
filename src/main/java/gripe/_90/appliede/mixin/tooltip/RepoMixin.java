package gripe._90.appliede.mixin.tooltip;

import com.google.common.collect.BiMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import appeng.client.gui.me.common.Repo;
import appeng.menu.me.common.GridInventoryEntry;

import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;

@Mixin(Repo.class)
public abstract class RepoMixin {
    // spotless:off
    @SuppressWarnings("MixinExtrasOperationParameters") // because Java still has horrible generics
    @WrapOperation(
            method = "handleUpdate(Lappeng/menu/me/common/GridInventoryEntry;)V",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lappeng/menu/me/common/GridInventoryEntry;isMeaningful()Z",
                            ordinal = 1)),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/BiMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0))
    // spotless:on
    private <K, V> V setTransmutable(
            BiMap<K, V> instance,
            K serial,
            V entry,
            Operation<V> original,
            @Local(name = "serverEntry") GridInventoryEntry serverEntry) {
        if (entry instanceof GridInventoryEMCEntry transmutable) {
            transmutable.appliede$setTransmutable(((GridInventoryEMCEntry) serverEntry).appliede$isTransmutable());
        }

        return original.call(instance, serial, entry);
    }
}
