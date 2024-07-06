package gripe._90.appliede.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.menu.me.common.MEStorageMenu;

import gripe._90.appliede.me.key.EMCKey;

@Mixin(value = MEStorageMenu.class, remap = false)
public abstract class MEStorageMenuMixin {
    // spotless:off
    @Redirect(
            method = "lambda$tryFillContainerItem$2",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/storage/StorageHelper;poweredExtraction(Lappeng/api/networking/energy/IEnergySource;Lappeng/api/storage/MEStorage;Lappeng/api/stacks/AEKey;JLappeng/api/networking/security/IActionSource;Lappeng/api/config/Actionable;)J"))
    // spotless:on
    private long emcExtraction(
            IEnergySource powerSource,
            MEStorage storage,
            AEKey what,
            long amount,
            IActionSource source,
            Actionable mode) {
        return StorageHelper.poweredExtraction(
                powerSource, storage, what instanceof EMCKey ? EMCKey.BASE : what, amount, source, mode);
    }
}
