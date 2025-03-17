package gripe._90.appliede.mixin.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.menu.me.common.MEStorageMenu;

import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenMixin {
    // spotless:off
    @WrapOperation(
            method = "renderTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/common/MEStorageMenu;isKeyVisible(Lappeng/api/stacks/AEKey;)Z"))
    // spotless:on
    private boolean hideEmcKey(MEStorageMenu menu, AEKey what, Operation<Boolean> original) {
        return original.call(menu, what) && (!(what instanceof EMCKey) || menu instanceof TransmutationTerminalMenu);
    }
}
