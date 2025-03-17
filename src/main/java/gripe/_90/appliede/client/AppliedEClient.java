package gripe._90.appliede.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import appeng.api.client.AEKeyRendering;
import appeng.api.util.AEColor;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.render.StaticItemColor;
import appeng.init.client.InitScreens;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.client.screen.EMCInterfaceScreen;
import gripe._90.appliede.client.screen.EMCSetStockAmountScreen;
import gripe._90.appliede.client.screen.TransmutationTerminalScreen;
import gripe._90.appliede.integration.ae2wtlib.WTTScreen;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.menu.EMCInterfaceMenu;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

@Mod(value = AppliedE.MODID, dist = Dist.CLIENT)
public class AppliedEClient {
    public AppliedEClient(ModContainer container, IEventBus bus) {
        container.registerConfig(ModConfig.Type.CLIENT, AppliedEConfig.Client.SPEC);

        bus.addListener(
                FMLClientSetupEvent.class,
                event -> event.enqueueWork(
                        () -> AEKeyRendering.register(EMCKeyType.TYPE, EMCKey.class, EMCRenderer.INSTANCE)));

        bus.addListener(RegisterMenuScreensEvent.class, event -> {
            InitScreens.register(
                    event,
                    AppliedE.EMC_INTERFACE_MENU.get(),
                    EMCInterfaceScreen<EMCInterfaceMenu>::new,
                    "/screens/appliede/emc_interface.json");
            InitScreens.register(
                    event,
                    AppliedE.EMC_SET_STOCK_AMOUNT_MENU.get(),
                    EMCSetStockAmountScreen::new,
                    "/screens/set_stock_amount.json");
            InitScreens.register(
                    event, AppliedE.EMC_EXPORT_BUS_MENU.get(), IOBusScreen::new, "/screens/export_bus.json");
            InitScreens.register(
                    event, AppliedE.EMC_IMPORT_BUS_MENU.get(), IOBusScreen::new, "/screens/import_bus.json");
            InitScreens.register(
                    event,
                    AppliedE.TRANSMUTATION_TERMINAL_MENU.get(),
                    TransmutationTerminalScreen<TransmutationTerminalMenu>::new,
                    "/screens/appliede/transmutation_terminal.json");

            if (ModList.get().isLoaded("ae2wtlib")) {
                WTTScreen.register(event);
            }
        });

        bus.addListener(
                RegisterColorHandlersEvent.Item.class,
                event -> event.register(
                        (stack, tintIndex) ->
                                new StaticItemColor(AEColor.TRANSPARENT).getColor(stack, tintIndex) | 0xFF000000,
                        AppliedE.TRANSMUTATION_TERMINAL.get()));
    }
}
