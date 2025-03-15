package gripe._90.appliede.mixin.tooltip;

import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.reporting.GridInventoryEMCEntry;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenMixin<C extends MEStorageMenu> extends AEBaseScreen<C> {
    public MEStorageScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    // spotless:off
    @Inject(
            method = "renderGridInventoryEntryTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;"))
    // spotless:on
    private void addTransmutable(
            GuiGraphics guiGraphics,
            GridInventoryEntry entry,
            int x,
            int y,
            CallbackInfo ci,
            @Local List<Component> currentTooltip) {
        if (menu instanceof TransmutationTerminalMenu
                && ((GridInventoryEMCEntry) entry).appliede$isTransmutable()
                && entry.getStoredAmount() == 0) {
            currentTooltip.add(Component.translatable("tooltip." + AppliedE.MODID + ".transmutable")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
