package gripe._90.appliede.integration.ae2wtlib;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.menu.ISubMenu;

import de.mari_023.ae2wtlib.terminal.WTMenuHost;

import gripe._90.appliede.me.misc.TransmutationTerminalHost;

public class WTTMenuHost extends WTMenuHost implements IViewCellStorage, TransmutationTerminalHost {
    private boolean shiftToTransmute;

    public WTTMenuHost(
            Player player,
            @Nullable Integer inventorySlot,
            ItemStack is,
            BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(player, inventorySlot, is, returnToMainMenu);
        readFromNbt();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AE2WTIntegration.getWirelessTerminalItem().getDefaultInstance();
    }

    @Override
    protected void readFromNbt() {
        super.readFromNbt();
        shiftToTransmute = getItemStack().getOrCreateTag().getBoolean("shiftToTransmute");
    }

    @Override
    public void saveChanges() {
        super.saveChanges();
        getItemStack().getOrCreateTag().putBoolean("shiftToTransmute", shiftToTransmute);
    }

    @Override
    public boolean getShiftToTransmute() {
        return shiftToTransmute;
    }

    @Override
    public void setShiftToTransmute(boolean shift) {
        shiftToTransmute = shift;
        saveChanges();
    }
}
