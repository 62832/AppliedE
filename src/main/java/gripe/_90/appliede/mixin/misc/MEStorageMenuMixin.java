package gripe._90.appliede.mixin.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.menu.me.common.MEStorageMenu;

import gripe._90.appliede.me.key.EMCKey;

@Mixin(MEStorageMenu.class)
public abstract class MEStorageMenuMixin {
    // spotless:off
    @WrapOperation(
            method = "lambda$tryFillContainerItem$3",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/storage/StorageHelper;poweredExtraction(Lappeng/api/networking/energy/IEnergySource;Lappeng/api/storage/MEStorage;Lappeng/api/stacks/AEKey;JLappeng/api/networking/security/IActionSource;Lappeng/api/config/Actionable;)J"))
    // spotless:on
    private long emcExtraction(
            IEnergySource energySource,
            MEStorage storage,
            AEKey what,
            long amount,
            IActionSource source,
            Actionable mode,
            Operation<Long> original) {
        return original.call(energySource, storage, what instanceof EMCKey ? EMCKey.BASE : what, amount, source, mode);
    }
}
