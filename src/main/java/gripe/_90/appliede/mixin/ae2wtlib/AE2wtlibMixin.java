package gripe._90.appliede.mixin.ae2wtlib;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.mari_023.ae2wtlib.AE2wtlib;
import de.mari_023.ae2wtlib.wut.WUTHandler;

import gripe._90.appliede.integration.ae2wtlib.AE2WTIntegration;
import gripe._90.appliede.integration.ae2wtlib.WTTItem;
import gripe._90.appliede.integration.ae2wtlib.WTTMenu;
import gripe._90.appliede.integration.ae2wtlib.WTTMenuHost;

@Mixin(AE2wtlib.class)
public abstract class AE2wtlibMixin {
    @Inject(method = "onAe2Initialized", at = @At("HEAD"), remap = false)
    private static void addWirelessTransmutationTerminal(CallbackInfo ci) {
        var terminal = (WTTItem) AE2WTIntegration.getWirelessTerminalItem();
        WUTHandler.addTerminal(
                "transmutation",
                terminal::tryOpen,
                WTTMenuHost::new,
                WTTMenu.TYPE,
                terminal,
                terminal.getDescriptionId());
    }
}
