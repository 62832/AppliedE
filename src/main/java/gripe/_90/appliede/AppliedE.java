package gripe._90.appliede;

import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.networking.GridServices;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.helpers.externalstorage.GenericStackItemStorage;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.MenuTypeBuilder;

import gripe._90.appliede.block.EMCInterfaceBlock;
import gripe._90.appliede.block.EMCInterfaceBlockEntity;
import gripe._90.appliede.integration.DummyIntegrationItem;
import gripe._90.appliede.integration.ae2wtlib.AE2WTIntegration;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.misc.EMCContainerItemStrategy;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;
import gripe._90.appliede.me.misc.LearnAllItemsPacket;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;
import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.me.service.KnowledgeServiceImpl;
import gripe._90.appliede.me.service.TransmutationPattern;
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

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    private static final DeferredRegister<BlockEntityType<?>> BE_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);
    private static final DeferredRegister<DataComponentType<?>> COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    @SuppressWarnings("UnstableApiUsage")
    public static final DeferredItem<Item> EMC_MODULE = ITEMS.register("emc_module", () -> {
        AEKeyTypes.register(EMCKeyType.TYPE);
        GridServices.register(KnowledgeService.class, KnowledgeServiceImpl.class);
        ContainerItemStrategy.register(EMCKeyType.TYPE, EMCKey.class, EMCContainerItemStrategy.INSTANCE);
        return part(EMCModulePart.class, EMCModulePart::new);
    });

    public static final DeferredBlock<EMCInterfaceBlock> EMC_INTERFACE = BLOCKS.register("emc_interface", () -> {
        var block = new EMCInterfaceBlock();
        ITEMS.register("emc_interface", () -> new BlockItem(block, new Item.Properties()));
        return block;
    });
    public static final DeferredItem<Item> CABLE_EMC_INTERFACE = ITEMS.register("cable_emc_interface", () -> part(EMCInterfacePart.class, EMCInterfacePart::new));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<EMCInterfaceBlockEntity>> EMC_INTERFACE_BE = BE_TYPES.register("emc_interface", () -> {
        var type = BlockEntityType.Builder.of(EMCInterfaceBlockEntity::new, EMC_INTERFACE.get()).build(null);
        EMC_INTERFACE.get().setBlockEntity(EMCInterfaceBlockEntity.class, type, null, null);
        return type;
    });

    public static final Supplier<MenuType<EMCInterfaceMenu>> EMC_INTERFACE_MENU = menu("emc_interface", EMCInterfaceMenu::new, EMCInterfaceLogicHost.class);
    public static final Supplier<MenuType<EMCSetStockAmountMenu>> EMC_SET_STOCK_AMOUNT_MENU = menu("emc_set_stock_amount", EMCSetStockAmountMenu::new, EMCInterfaceLogicHost.class);

    public static final DeferredItem<Item> EMC_EXPORT_BUS = ITEMS.register("emc_export_bus", () -> part(EMCExportBusPart.class, EMCExportBusPart::new));
    public static final DeferredItem<Item> EMC_IMPORT_BUS = ITEMS.register("emc_import_bus", () -> part(EMCImportBusPart.class, EMCImportBusPart::new));
    public static final Supplier<MenuType<IOBusMenu>> EMC_EXPORT_BUS_MENU = menu("emc_export_bus", IOBusMenu::new, EMCExportBusPart.class);
    public static final Supplier<MenuType<IOBusMenu>> EMC_IMPORT_BUS_MENU = menu("emc_import_bus", IOBusMenu::new, EMCImportBusPart.class);

    public static final DeferredItem<Item> TRANSMUTATION_TERMINAL = ITEMS.register("transmutation_terminal", () -> part(TransmutationTerminalPart.class, TransmutationTerminalPart::new));
    public static final Supplier<MenuType<TransmutationTerminalMenu>> TRANSMUTATION_TERMINAL_MENU = menu("transmutation_terminal", TransmutationTerminalMenu::new, TransmutationTerminalHost.class);
    public static final DeferredItem<Item> LEARNING_CARD = ITEMS.register("learning_card", () -> Upgrades.createUpgradeCardItem(new Item.Properties()));

    public static final DeferredItem<Item> DUMMY_EMC_ITEM = ITEMS.register("dummy_emc_item", () -> new Item(new Item.Properties()));
    public static final Supplier<DataComponentType<TransmutationPattern.Encoded>> ENCODED_TRANSMUTATION_PATTERN = COMPONENT_TYPES.register(
            "encoded_transmutation_pattern",
            () -> DataComponentType.<TransmutationPattern.Encoded>builder()
                    .persistent(TransmutationPattern.Encoded.CODEC)
                    .networkSynchronized(TransmutationPattern.Encoded.STREAM_CODEC)
                    .build());

    public static final DeferredItem<Item> WIRELESS_TRANSMUTATION_TERMINAL = ITEMS.register("wireless_transmutation_terminal", () -> ModList.get().isLoaded("ae2wtlib")
            ? AE2WTIntegration.TERMINAL
            : new DummyIntegrationItem(new Item.Properties().stacksTo(1), "AE2WTLib"));
    public static final Supplier<DataComponentType<Boolean>> SHIFT_TO_TRANSMUTE = COMPONENT_TYPES.register(
            "shift_to_transmute",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());
    
    static {
        TABS.register(MODID, () -> CreativeModeTab.builder()
                .title(Component.translatable("mod." + MODID))
                .icon(() -> EMC_INTERFACE.asItem().getDefaultInstance())
                .displayItems((params, output) -> {
                    output.accept(EMC_MODULE);
                    output.accept(EMC_INTERFACE);
                    output.accept(CABLE_EMC_INTERFACE);
                    output.accept(EMC_EXPORT_BUS);
                    output.accept(EMC_IMPORT_BUS);
                    output.accept(TRANSMUTATION_TERMINAL);
                    output.accept(LEARNING_CARD);
                    output.accept(WIRELESS_TRANSMUTATION_TERMINAL);

                    if (ModList.get().isLoaded("ae2wtlib")) {
                        output.accept(AE2WTIntegration.getChargedTerminal());
                    }
                })
                .build());
    }

    @SuppressWarnings("UnstableApiUsage")
    public AppliedE(ModContainer container, IEventBus eventBus) {
        container.registerConfig(ModConfig.Type.COMMON, AppliedEConfig.SPEC);

        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        MENU_TYPES.register(eventBus);
        BE_TYPES.register(eventBus);
        COMPONENT_TYPES.register(eventBus);
        TABS.register(eventBus);

        eventBus.addListener((FMLCommonSetupEvent event) -> {
            var busesGroup = GuiText.IOBuses.getTranslationKey();
            Upgrades.add(AEItems.REDSTONE_CARD, EMC_EXPORT_BUS, 1, busesGroup);
            Upgrades.add(AEItems.CAPACITY_CARD, EMC_EXPORT_BUS, 5, busesGroup);
            Upgrades.add(AEItems.SPEED_CARD, EMC_EXPORT_BUS, 4, busesGroup);
            Upgrades.add(AEItems.REDSTONE_CARD, EMC_IMPORT_BUS, 1, busesGroup);
            Upgrades.add(AEItems.CAPACITY_CARD, EMC_IMPORT_BUS, 5, busesGroup);
            Upgrades.add(AEItems.SPEED_CARD, EMC_IMPORT_BUS, 4, busesGroup);
            Upgrades.add(AEItems.INVERTER_CARD, EMC_IMPORT_BUS, 1, busesGroup);

            var emcInterfaceGroup = EMC_INTERFACE.get().getDescriptionId();
            Upgrades.add(LEARNING_CARD, EMC_INTERFACE, 1, emcInterfaceGroup);
            Upgrades.add(LEARNING_CARD, CABLE_EMC_INTERFACE, 1, emcInterfaceGroup);
            Upgrades.add(LEARNING_CARD, EMC_IMPORT_BUS, 1);
        });

        eventBus.addListener(RegisterCapabilitiesEvent.class, event -> {
            event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, EMC_INTERFACE_BE.get(), (be, $) -> be);
            event.registerBlockEntity(AECapabilities.ME_STORAGE, EMC_INTERFACE_BE.get(), (be, $) -> be.getInterfaceLogic().getInventory());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, EMC_INTERFACE_BE.get(), (be, $) -> new GenericStackItemStorage(be.getInterfaceLogic().getStorage()));
        });

        eventBus.addListener(RegisterPartCapabilitiesEvent.class, event -> {
            event.register(AECapabilities.ME_STORAGE, (part, $) -> part.getInterfaceLogic().getInventory(), EMCInterfacePart.class);
            event.register(Capabilities.ItemHandler.BLOCK, (part, $) -> new GenericStackItemStorage(part.getInterfaceLogic().getStorage()), EMCInterfacePart.class);
        });

        eventBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1").playToServer(LearnAllItemsPacket.TYPE, LearnAllItemsPacket.STREAM_CODEC, LearnAllItemsPacket::handle));

        if (ModList.get().isLoaded("ae2wtlib")) {
            eventBus.addListener(AE2WTIntegration::registerTerminalMenu);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private static <P extends IPart> Item part(Class<P> partClass, Function<IPartItem<P>, P> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return new PartItem<>(new Item.Properties(), partClass, factory);
    }

    private static <M extends AEBaseMenu, H> Supplier<MenuType<M>> menu(String id, MenuTypeBuilder.MenuFactory<M, H> factory, Class<H> host) {
        return MENU_TYPES.register(id, () -> MenuTypeBuilder.create(factory, host).buildUnregistered(id(id)));
    }

    private static <M extends AEBaseMenu, H> Supplier<MenuType<M>> menu(String id, MenuTypeBuilder.TypedMenuFactory<M, H> factory, Class<H> host) {
        return MENU_TYPES.register(id, () -> MenuTypeBuilder.create(factory, host).buildUnregistered(id(id)));
    }
}
// spotless:on
