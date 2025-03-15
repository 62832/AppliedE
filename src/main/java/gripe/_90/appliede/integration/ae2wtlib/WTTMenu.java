package gripe._90.appliede.integration.ae2wtlib;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.networking.IGridNode;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;

import de.mari_023.ae2wtlib.api.gui.AE2wtlibSlotSemantics;
import de.mari_023.ae2wtlib.api.terminal.ItemWUT;

import gripe._90.appliede.menu.TransmutationTerminalMenu;

public class WTTMenu extends TransmutationTerminalMenu {
    public static final MenuType<WTTMenu> TYPE =
            MenuTypeBuilder.create(WTTMenu::new, WTTMenuHost.class).build("wireless_transmutation_terminal");

    public WTTMenu(int id, Inventory ip, WTTMenuHost host) {
        super(TYPE, id, ip, host, true);

        var singularitySlot = new RestrictedInputSlot(
                RestrictedInputSlot.PlacableItemType.QE_SINGULARITY,
                host.getSubInventory(WTTMenuHost.INV_SINGULARITY),
                0);
        addSlot(singularitySlot, AE2wtlibSlotSemantics.SINGULARITY);
    }

    @Nullable
    @Override
    public IGridNode getGridNode() {
        return getHost().getActionableNode();
    }

    boolean isWUT() {
        return ((WTTMenuHost) getHost()).getItemStack().getItem() instanceof ItemWUT;
    }
}
