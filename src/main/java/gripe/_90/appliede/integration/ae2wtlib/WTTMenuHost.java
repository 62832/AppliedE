package gripe._90.appliede.integration.ae2wtlib;

import java.util.function.BiConsumer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;

import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.api.TransmutationTerminalHost;

public class WTTMenuHost extends WTMenuHost implements IViewCellStorage, TransmutationTerminalHost {
    private boolean shiftToTransmute = getItemStack().getOrDefault(AppliedE.SHIFT_TO_TRANSMUTE.get(), false);

    public WTTMenuHost(
            ItemWT item, Player player, ItemMenuHostLocator locator, BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AE2WTIntegration.TERMINAL.getDefaultInstance();
    }

    @Override
    public boolean getShiftToTransmute() {
        return shiftToTransmute;
    }

    @Override
    public void toggleShiftToTransmute() {
        shiftToTransmute = !shiftToTransmute;
        getItemStack().set(AppliedE.SHIFT_TO_TRANSMUTE.get(), shiftToTransmute);
    }
}
