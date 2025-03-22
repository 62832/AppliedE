package gripe._90.appliede.integration.ae2wtlib;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import appeng.client.gui.style.ScreenStyle;
import appeng.init.client.InitScreens;

import de.mari_023.ae2wtlib.api.gui.ScrollingUpgradesPanel;
import de.mari_023.ae2wtlib.api.terminal.IUniversalTerminalCapable;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;

import gripe._90.appliede.client.screen.TransmutationTerminalScreen;

public class WTTScreen extends TransmutationTerminalScreen<WTTMenu> implements IUniversalTerminalCapable {
    private final ScrollingUpgradesPanel upgradesPanel;

    public WTTScreen(WTTMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        if (menu.isWUT()) {
            addToLeftToolbar(cycleTerminalButton());
        }

        upgradesPanel = addUpgradePanel(widgets, getMenu());
    }

    @Override
    public void init() {
        super.init();
        upgradesPanel.setMaxRows(Math.max(2, getVisibleRows()));
    }

    public static void register(RegisterMenuScreensEvent event) {
        InitScreens.register(
                event, WTTMenu.TYPE, WTTScreen::new, "/screens/appliede/wireless_transmutation_terminal.json");
    }

    @NotNull
    @Override
    public WTMenuHost getHost() {
        return (WTMenuHost) getMenu().getHost();
    }
}
