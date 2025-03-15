package gripe._90.appliede.integration.ae2wtlib;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.BackgroundPanel;

import de.mari_023.ae2wtlib.api.terminal.IUniversalTerminalCapable;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;

import gripe._90.appliede.client.screen.TransmutationTerminalScreen;

public class WTTScreen extends TransmutationTerminalScreen<WTTMenu> implements IUniversalTerminalCapable {
    public WTTScreen(WTTMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        if (menu.isWUT()) {
            addToLeftToolbar(cycleTerminalButton());
        }

        widgets.add("singularityBackground", new BackgroundPanel(style.getImage("singularityBackground")));
    }

    @NotNull
    @Override
    public WTMenuHost getHost() {
        return (WTMenuHost) getMenu().getHost();
    }
}
