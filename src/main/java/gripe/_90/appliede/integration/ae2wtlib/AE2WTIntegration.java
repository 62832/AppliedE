package gripe._90.appliede.integration.ae2wtlib;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.core.AppEng;
import appeng.init.client.InitScreens;
import appeng.items.tools.powered.WirelessTerminalItem;

import gripe._90.appliede.integration.Addons;

public class AE2WTIntegration {
    private static final Item TERMINAL = new WTTItem();

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

    public static void registerTerminalMenu(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.MENU)) {
            ForgeRegistries.MENU_TYPES.register(AppEng.makeId("wireless_transmutation_terminal"), WTTMenu.TYPE);
        }
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
