package gripe._90.appliede.integration.ae2wtlib;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.init.client.InitScreens;
import appeng.items.tools.powered.WirelessTerminalItem;

import gripe._90.appliede.integration.Addons;

public class AE2WTIntegration {
    private static final WTTItem TERMINAL = new WTTItem();

    static {
        GridLinkables.register(TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
    }

    public static WTTItem getWirelessTerminalItem() {
        return TERMINAL;
    }

    public static MenuType<?> getWirelessTerminalMenu() {
        return WTTMenu.TYPE;
    }

    public static ItemStack getChargedTerminal() {
        var stack = TERMINAL.getDefaultInstance();
        TERMINAL.injectAEPower(stack, TERMINAL.getAEMaxPower(stack), Actionable.MODULATE);
        return stack;
    }

    public static void addTerminalToAE2WTLibTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().getNamespace().equals(Addons.AE2WTLIB.getModId())) {
            event.accept(TERMINAL);
            event.accept(AE2WTIntegration.getChargedTerminal());
        }
    }

    public static class Client {
        public static void initScreen() {
            InitScreens.register(
                    WTTMenu.TYPE, WTTScreen::new, "/screens/appliede/wireless_transmutation_terminal.json");
        }
    }
}
