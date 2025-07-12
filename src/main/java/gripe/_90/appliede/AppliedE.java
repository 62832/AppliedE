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
import gripe._90.appliede.integration.ae2wtlib.WTTItem;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.misc.EMCContainerItemStrategy;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;
import gripe._90.appliede.me.misc.LearnAllItemsPacket;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;
import gripe._90.appliede.me.service.KnowledgeService;
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
        GridServices.register(KnowledgeService.class, KnowledgeService.class);
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
        return BlockEntityType.Builder.of(EMCInterfaceBlockEntity::new, EMC_INTERFACE.get()).build(null);
    });

    public static final DeferredItem<Item> EMC_EXPORT_BUS = ITEMS.register("emc_export_bus", () -> part(EMCExportBusPart.class, EMCExportBusPart::new));
    public static final DeferredItem<Item> EMC_IMPORT_BUS = ITEMS.register("emc_import_bus", () -> part(EMCImportBusPart.class, EMCImportBusPart::new));
    public static final DeferredItem<Item> TRANSMUTATION_TERMINAL = ITEMS.register("transmutation_terminal", () -> part(TransmutationTerminalPart.class, TransmutationTerminalPart::new));

    public static final DeferredItem<Item> LEARNING_CARD = ITEMS.register("learning_card", () -> new Item(new Item.Properties().stacksTo(16)));

    public static final Supplier<MenuType<EMCInterfaceMenu>> EMC_INTERFACE_MENU = MENU_TYPES.register("emc_interface", () -> {
        return MenuTypeBuilder.create(EMCInterfaceMenu::new, EMCInterfaceLogicHost.class).build();
    });
    public static final Supplier<MenuType<TransmutationTerminalMenu>> TRANSMUTATION_TERMINAL_MENU = MENU_TYPES.register("transmutation_terminal", () -> {
        return MenuTypeBuilder.create(TransmutationTerminalMenu::new, TransmutationTerminalHost.class).build();
    });
    public static final Supplier<MenuType<EMCSetStockAmountMenu>> SET_STOCK_AMOUNT_MENU = MENU_TYPES.register("set_stock_amount", () -> {
        return MenuTypeBuilder.create(EMCSetStockAmountMenu::new, TransmutationTerminalHost.class).build();
    });

    public static final Supplier<DataComponentType<TransmutationPattern.Encoded>> TRANSMUTATION_PATTERN = COMPONENT_TYPES.register(
            "transmutation_pattern",
            () -> DataComponentType.<TransmutationPattern.Encoded>builder()
                    .persistent(TransmutationPattern.Encoded.CODEC)
                    .networkSynchronized(TransmutationPattern.Encoded.STREAM_CODEC)
                    .build());

    public static final DeferredItem<Item> WIRELESS_TRANSMUTATION_TERMINAL = ITEMS.register("wireless_transmutation_terminal", () -> ModList.get().isLoaded("ae2wtlib")
            ? new WTTItem()
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
                })
                .build());
    }

    private static final Function<Class<? extends IPart>, IPartItem<?>> part = clazz -> {
        var impl = new PartItem<>(new Item.Properties(), clazz);
        PartModels.registerModels(PartModelsHelper.createModels(clazz));
        return impl;
    };

    @SuppressWarnings("unchecked")
    private static <T extends IPart> IPartItem<T> part(Class<T> clazz, Function<IPartItem<T>, T> factory) {
        return (IPartItem<T>) part.apply(clazz);
    }

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }

    public AppliedE(IEventBus bus, ModContainer container) {
        ITEMS.register(bus);
        BLOCKS.register(bus);
        BE_TYPES.register(bus);
        MENU_TYPES.register(bus);
        COMPONENT_TYPES.register(bus);
        TABS.register(bus);

        var config = new AppliedEConfig();
        container.registerConfig(ModConfig.Type.COMMON, config.spec());

        bus.addListener(this::registerCapabilities);
        bus.addListener(this::commonSetup);
        bus.addListener(this::registerPayloads);
        bus.addListener((RegisterEvent event) -> {
            if (ModList.get().isLoaded("ae2wtlib")) {
                AE2WTIntegration.registerTerminalMenu(event);
            }
        });
        bus.addListener((RegisterPartCapabilitiesEvent event) -> {
            event.register(AECapabilities.GENERIC_INTERNAL_INV, EMCImportBusPart.class, EMCExportBusPart.class, EMCInterfacePart.class);
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> new GenericStackItemStorage(() -> stack, EMCKeyType.TYPE), LEARNING_CARD);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.FUZZY_CARD, EMC_EXPORT_BUS, 1);
            Upgrades.add(AEItems.FUZZY_CARD, EMC_IMPORT_BUS, 1);
            Upgrades.add(AEItems.CAPACITY_CARD, EMC_EXPORT_BUS, 2);
            Upgrades.add(AEItems.CAPACITY_CARD, EMC_IMPORT_BUS, 2);
            Upgrades.add(AEItems.REDSTONE_CARD, EMC_EXPORT_BUS, 1);
            Upgrades.add(AEItems.REDSTONE_CARD, EMC_IMPORT_BUS, 1);
            Upgrades.add(AEItems.ACCELERATION_CARD, EMC_EXPORT_BUS, 5);
            Upgrades.add(AEItems.ACCELERATION_CARD, EMC_IMPORT_BUS, 5);

            Upgrades.add(AEItems.REDSTONE_CARD, TRANSMUTATION_TERMINAL, 1);
            Upgrades.add(AEItems.CRAFTING_CARD, TRANSMUTATION_TERMINAL, 1);

            Upgrades.add(AEItems.FUZZY_CARD, CABLE_EMC_INTERFACE, 1);
            Upgrades.add(AEItems.REDSTONE_CARD, CABLE_EMC_INTERFACE, 1);
            Upgrades.add(AEItems.CAPACITY_CARD, CABLE_EMC_INTERFACE, 2);
            Upgrades.add(AEItems.CRAFTING_CARD, CABLE_EMC_INTERFACE, 1);
            Upgrades.add(AEItems.PATTERN_EXPANSION_CARD, CABLE_EMC_INTERFACE, 3);

            Upgrades.add(AEItems.FUZZY_CARD, EMC_INTERFACE, 1);
            Upgrades.add(AEItems.REDSTONE_CARD, EMC_INTERFACE, 1);
            Upgrades.add(AEItems.CAPACITY_CARD, EMC_INTERFACE, 2);
            Upgrades.add(AEItems.CRAFTING_CARD, EMC_INTERFACE, 1);
            Upgrades.add(AEItems.PATTERN_EXPANSION_CARD, EMC_INTERFACE, 3);

            AEBaseMenu.registerMenuType(EMC_INTERFACE_MENU.get(), EMCInterfaceMenu.class);
            AEBaseMenu.registerMenuType(TRANSMUTATION_TERMINAL_MENU.get(), TransmutationTerminalMenu.class);
            AEBaseMenu.registerMenuType(SET_STOCK_AMOUNT_MENU.get(), EMCSetStockAmountMenu.class);

            GuiText.TERMINAL.setTranslationKey("gui." + MODID + ".terminal");
        });
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        var version = event.registrar(MODID).versioned("1.0.0");
        version.playToServer(LearnAllItemsPacket.TYPE, LearnAllItemsPacket.STREAM_CODEC, LearnAllItemsPacket::handle);
    }
}
