package gripe._90.appliede.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.menu.me.common.MEStorageMenu;

import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenMixin {
    // spotless:off
    @Redirect(
            method = "renderTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/common/MEStorageMenu;isKeyVisible(Lappeng/api/stacks/AEKey;)Z",
                    remap = false))
    // spotless:on
    private boolean hideEmcKey(MEStorageMenu menu, AEKey what) {
        return menu.isKeyVisible(what) && (!(what instanceof EMCKey) || menu instanceof TransmutationTerminalMenu);
    }
}
