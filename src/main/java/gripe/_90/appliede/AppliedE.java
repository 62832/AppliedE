package gripe._90.appliede;

import java.math.BigInteger;
import java.util.function.Function;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.client.AEKeyRendering;
import appeng.api.networking.GridServices;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.AEColor;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.render.StaticItemColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.init.client.InitScreens;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.MenuTypeBuilder;

import gripe._90.appliede.block.EMCInterfaceBlock;
import gripe._90.appliede.block.EMCInterfaceBlockEntity;
import gripe._90.appliede.client.EMCRenderer;
import gripe._90.appliede.client.screen.EMCInterfaceScreen;
import gripe._90.appliede.client.screen.EMCSetStockAmountScreen;
import gripe._90.appliede.client.screen.TransmutationTerminalScreen;
import gripe._90.appliede.integration.DummyIntegrationItem;
import gripe._90.appliede.integration.ae2wtlib.AE2WTIntegration;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.misc.EMCContainerItemStrategy;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;
import gripe._90.appliede.me.misc.LearnAllItemsPacket;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;
import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.menu.EMCInterfaceMenu;
import gripe._90.appliede.menu.EMCSetStockAmountMenu;
import gripe._90.appliede.menu.TransmutationTerminalMenu;
import gripe._90.appliede.part.EMCExportBusPart;
import gripe._90.appliede.part.EMCImportBusPart;
import gripe._90.appliede.part.EMCInterfacePart;
import gripe._90.appliede.part.EMCModulePart;
import gripe._90.appliede.part.TransmutationTerminalPart;

// spotless:off
@Mod(AppliedE.MODID)
public final class AppliedE {
    public static final String MODID = "appliede";
    public static final BigInteger TIER_LIMIT = BigInteger.valueOf((long) Math.pow(10, 12));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BE_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AppEng.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    @SuppressWarnings("UnstableApiUsage")
    public static final RegistryObject<Item> EMC_MODULE = ITEMS.register("emc_module", () -> {
        AEKeyTypes.register(EMCKeyType.TYPE);
        GridServices.register(KnowledgeService.class, KnowledgeService.class);
        ContainerItemStrategy.register(EMCKeyType.TYPE, EMCKey.class, EMCContainerItemStrategy.INSTANCE);
        return part(EMCModulePart.class, EMCModulePart::new);
    });

    public static final RegistryObject<EMCInterfaceBlock> EMC_INTERFACE = BLOCKS.register("emc_interface", () -> {
        var block = new EMCInterfaceBlock();
        ITEMS.register("emc_interface", () -> new BlockItem(block, new Item.Properties()));
        return block;
    });
    public static final RegistryObject<Item> CABLE_EMC_INTERFACE = ITEMS.register("cable_emc_interface", () -> part(EMCInterfacePart.class, EMCInterfacePart::new));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<EMCInterfaceBlockEntity>> EMC_INTERFACE_BE = BE_TYPES.register("emc_interface", () -> {
        var type = BlockEntityType.Builder.of(EMCInterfaceBlockEntity::new, EMC_INTERFACE.get()).build(null);
        EMC_INTERFACE.get().setBlockEntity(EMCInterfaceBlockEntity.class, type, null, null);
        return type;
    });

    public static final RegistryObject<MenuType<EMCInterfaceMenu>> EMC_INTERFACE_MENU = menu("emc_interface", EMCInterfaceMenu::new, EMCInterfaceLogicHost.class);
    public static final RegistryObject<MenuType<EMCSetStockAmountMenu>> EMC_SET_STOCK_AMOUNT_MENU = menu("emc_set_stock_amount", EMCSetStockAmountMenu::new, EMCInterfaceLogicHost.class);

    public static final RegistryObject<Item> EMC_EXPORT_BUS = ITEMS.register("emc_export_bus", () -> part(EMCExportBusPart.class, EMCExportBusPart::new));
    public static final RegistryObject<Item> EMC_IMPORT_BUS = ITEMS.register("emc_import_bus", () -> part(EMCImportBusPart.class, EMCImportBusPart::new));
    public static final RegistryObject<MenuType<IOBusMenu>> EMC_EXPORT_BUS_MENU = menu("emc_export_bus", IOBusMenu::new, EMCExportBusPart.class);
    public static final RegistryObject<MenuType<IOBusMenu>> EMC_IMPORT_BUS_MENU = menu("emc_import_bus", IOBusMenu::new, EMCImportBusPart.class);

    public static final RegistryObject<Item> TRANSMUTATION_TERMINAL = ITEMS.register("transmutation_terminal", () -> part(TransmutationTerminalPart.class, TransmutationTerminalPart::new));
    public static final RegistryObject<MenuType<TransmutationTerminalMenu>> TRANSMUTATION_TERMINAL_MENU = menu("transmutation_terminal", TransmutationTerminalMenu::new, TransmutationTerminalHost.class);
    public static final RegistryObject<Item> LEARNING_CARD = ITEMS.register("learning_card", () -> Upgrades.createUpgradeCardItem(new Item.Properties()));

    public static final RegistryObject<Item> DUMMY_EMC_ITEM = ITEMS.register("dummy_emc_item", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WIRELESS_TRANSMUTATION_TERMINAL = ITEMS.register("wireless_transmutation_terminal", () -> ModList.get().isLoaded("ae2wtlib")
            ? AE2WTIntegration.getWirelessTerminalItem()
            : new DummyIntegrationItem(new Item.Properties().stacksTo(1), "AE2WTLib"));
    
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            id("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    
    static {
        TABS.register(MODID, () -> CreativeModeTab.builder()
                .title(Component.translatable("mod." + MODID))
                .icon(() -> EMC_INTERFACE.get().asItem().getDefaultInstance())
                .displayItems((params, output) -> {
                    output.accept(EMC_MODULE.get());
                    output.accept(EMC_INTERFACE.get());
                    output.accept(CABLE_EMC_INTERFACE.get());
                    output.accept(EMC_EXPORT_BUS.get());
                    output.accept(EMC_IMPORT_BUS.get());
                    output.accept(TRANSMUTATION_TERMINAL.get());
                    output.accept(LEARNING_CARD.get());
                    output.accept(WIRELESS_TRANSMUTATION_TERMINAL.get());

                    if (ModList.get().isLoaded("ae2wtlib")) {
                        output.accept(AE2WTIntegration.getChargedTerminal());
                    }
                })
                .build());
        
        PACKET_HANDLER.messageBuilder(LearnAllItemsPacket.class, 0)
                .encoder((packet, buffer) -> {})
                .decoder(buffer -> new LearnAllItemsPacket())
                .consumerMainThread(LearnAllItemsPacket::handle)
                .add();
    }

    public AppliedE() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AppliedEConfig.SPEC);

        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        MENU_TYPES.register(bus);
        BE_TYPES.register(bus);
        TABS.register(bus);

        bus.addListener((FMLCommonSetupEvent event) -> {
            var busesGroup = GuiText.IOBuses.getTranslationKey();
            Upgrades.add(AEItems.REDSTONE_CARD, EMC_EXPORT_BUS.get(), 1, busesGroup);
            Upgrades.add(AEItems.CAPACITY_CARD, EMC_EXPORT_BUS.get(), 5, busesGroup);
            Upgrades.add(AEItems.SPEED_CARD, EMC_EXPORT_BUS.get(), 4, busesGroup);
            Upgrades.add(AEItems.REDSTONE_CARD, EMC_IMPORT_BUS.get(), 1, busesGroup);
            Upgrades.add(AEItems.CAPACITY_CARD, EMC_IMPORT_BUS.get(), 5, busesGroup);
            Upgrades.add(AEItems.SPEED_CARD, EMC_IMPORT_BUS.get(), 4, busesGroup);
            Upgrades.add(AEItems.INVERTER_CARD, EMC_IMPORT_BUS.get(), 1, busesGroup);

            var emcInterfaceGroup = EMC_INTERFACE.get().getDescriptionId();
            Upgrades.add(LEARNING_CARD.get(), EMC_INTERFACE.get(), 1, emcInterfaceGroup);
            Upgrades.add(LEARNING_CARD.get(), CABLE_EMC_INTERFACE.get(), 1, emcInterfaceGroup);
            Upgrades.add(LEARNING_CARD.get(), EMC_IMPORT_BUS.get(), 1);
        });

        if (ModList.get().isLoaded("ae2wtlib")) {
            bus.addListener(AE2WTIntegration::registerTerminalMenu);
            bus.addListener(AE2WTIntegration::addTerminalToAE2WTLibTab);
        }

        if (FMLEnvironment.dist.isClient()) {
            Client.setup(bus);
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    private static <P extends IPart> Item part(Class<P> partClass, Function<IPartItem<P>, P> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return new PartItem<>(new Item.Properties(), partClass, factory);
    }

    private static <M extends AEBaseMenu, H> RegistryObject<MenuType<M>> menu(String id, MenuTypeBuilder.MenuFactory<M, H> factory, Class<H> host) {
        return MENU_TYPES.register(id, () -> MenuTypeBuilder.create(factory, host).build(id));
    }

    private static <M extends AEBaseMenu, H> RegistryObject<MenuType<M>> menu(String id, MenuTypeBuilder.TypedMenuFactory<M, H> factory, Class<H> host) {
        return MENU_TYPES.register(id, () -> MenuTypeBuilder.create(factory, host).build(id));
    }

    private static class Client {
        private static void setup(IEventBus bus) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AppliedEConfig.Client.SPEC);

            bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
                AEKeyRendering.register(EMCKeyType.TYPE, EMCKey.class, EMCRenderer.INSTANCE);

                InitScreens.register(
                        AppliedE.EMC_INTERFACE_MENU.get(),
                        EMCInterfaceScreen<EMCInterfaceMenu>::new,
                        "/screens/appliede/emc_interface.json");
                InitScreens.register(
                        AppliedE.EMC_SET_STOCK_AMOUNT_MENU.get(),
                        EMCSetStockAmountScreen::new,
                        "/screens/set_stock_amount.json");
                InitScreens.register(
                        AppliedE.EMC_EXPORT_BUS_MENU.get(),
                        IOBusScreen::new,
                        "/screens/export_bus.json");
                InitScreens.register(
                        AppliedE.EMC_IMPORT_BUS_MENU.get(),
                        IOBusScreen::new,
                        "/screens/import_bus.json");
                InitScreens.register(
                        AppliedE.TRANSMUTATION_TERMINAL_MENU.get(),
                        TransmutationTerminalScreen<TransmutationTerminalMenu>::new,
                        "/screens/appliede/transmutation_terminal.json");

                if (ModList.get().isLoaded("ae2wtlib")) {
                    AE2WTIntegration.Client.initScreen();
                }
            }));

            bus.addListener((RegisterColorHandlersEvent.Item event) -> event.register(
                    new StaticItemColor(AEColor.TRANSPARENT),
                    AppliedE.TRANSMUTATION_TERMINAL.get()));
        }
    }
}
// spotless:on
