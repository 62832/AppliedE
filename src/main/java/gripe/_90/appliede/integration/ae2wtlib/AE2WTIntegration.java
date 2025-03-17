package gripe._90.appliede.integration.ae2wtlib;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.RegisterEvent;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.core.AppEng;
import appeng.items.tools.powered.WirelessTerminalItem;

import de.mari_023.ae2wtlib.api.gui.Icon;
import de.mari_023.ae2wtlib.api.registration.AddTerminalEvent;

import gripe._90.appliede.AppliedE;

public class AE2WTIntegration {
    public static final Item TERMINAL = new WTTItem();

    static {
        var iconTexture = new Icon.Texture(AppliedE.id("textures/item/wireless_terminal_icon.png"), 16, 16);
        var icon = new Icon(0, 0, 16, 16, iconTexture);
        AddTerminalEvent.register(
                event -> event.builder("transmutation", WTTMenuHost::new, WTTMenu.TYPE, (WTTItem) TERMINAL, icon)
                        .addTerminal());

        GridLinkables.register(TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
    }

    public static ItemStack getChargedTerminal() {
        var stack = new ItemStack(TERMINAL);
        var terminal = (WTTItem) TERMINAL;
        terminal.injectAEPower(stack, terminal.getAEMaxPower(stack), Actionable.MODULATE);
        return stack;
    }

    public static void registerTerminalMenu(RegisterEvent event) {
        event.register(Registries.MENU, AppEng.makeId("wireless_transmutation_terminal"), () -> WTTMenu.TYPE);
    }
}
