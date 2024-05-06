package gripe._90.appliede.integration.ae2wtlib;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.BackgroundPanel;

import de.mari_023.ae2wtlib.wut.CycleTerminalButton;
import de.mari_023.ae2wtlib.wut.IUniversalTerminalCapable;

import gripe._90.appliede.client.screen.TransmutationTerminalScreen;

public class WTTScreen extends TransmutationTerminalScreen<WTTMenu> implements IUniversalTerminalCapable {
    public WTTScreen(WTTMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        if (menu.isWUT()) {
            addToLeftToolbar(new CycleTerminalButton(btn -> cycleTerminal()));
        }

        widgets.add("singularityBackground", new BackgroundPanel(style.getImage("singularityBackground")));
    }
}
