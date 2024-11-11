package gripe._90.appliede.integration.ae2wtlib;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.init.client.InitScreens;
import appeng.items.tools.powered.WirelessTerminalItem;

import gripe._90.appliede.integration.Addons;

public class AE2WTIntegration {
    private static final Item TERMINAL = new WTTItem();
    public static final MenuType<?> MENU = WTTMenu.TYPE;

    static {
        GridLinkables.register(TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
    }

    public static Item getWirelessTerminalItem() {
        return TERMINAL;
    }

    public static ItemStack getChargedTerminal() {
        var stack = TERMINAL.getDefaultInstance();
        var terminal = (WTTItem) TERMINAL;
        terminal.injectAEPower(stack, terminal.getAEMaxPower(stack), Actionable.MODULATE);
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
