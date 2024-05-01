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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.networking.GridServices;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.api.stacks.AEKeyTypes;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;

import gripe._90.appliede.iface.EMCInterfaceBlock;
import gripe._90.appliede.iface.EMCInterfaceBlockEntity;
import gripe._90.appliede.iface.EMCInterfaceMenu;
import gripe._90.appliede.iface.EMCInterfacePart;
import gripe._90.appliede.iface.EMCInterfacePartAECF;
import gripe._90.appliede.iface.EMCInterfaceScreen;
import gripe._90.appliede.key.EMCKey;
import gripe._90.appliede.key.EMCKeyType;
import gripe._90.appliede.module.EMCModulePart;
import gripe._90.appliede.module.TransmutationPatternItem;
import gripe._90.appliede.service.KnowledgeService;
import gripe._90.appliede.strategy.EMCContainerItemStrategy;
import gripe._90.appliede.strategy.EMCExportStrategy;
import gripe._90.appliede.strategy.EMCImportStrategy;

import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.emc.mappers.APICustomEMCMapper;

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

        // external storage strategy not provided so as not to clash with mounted EMC service storage
        StackImportStrategy.register(EMCKeyType.TYPE, EMCImportStrategy::new);
        StackExportStrategy.register(EMCKeyType.TYPE, EMCExportStrategy::new);
        ContainerItemStrategy.register(EMCKeyType.TYPE, EMCKey.class, new EMCContainerItemStrategy());

        return part(EMCModulePart.class, EMCModulePart::new);
    });
    public static final RegistryObject<Item> TRANSMUTATION_PATTERN = ITEMS.register("transmutation_pattern", TransmutationPatternItem::new);

    public static final RegistryObject<EMCInterfaceBlock> EMC_INTERFACE = BLOCKS.register("emc_interface", () -> {
        var block = new EMCInterfaceBlock();
        ITEMS.register("emc_interface", () -> new BlockItem(block, new Item.Properties()));
        return block;
    });
    public static final RegistryObject<Item> CABLE_EMC_INTERFACE = ITEMS.register("cable_emc_interface", () -> ModList.get().isLoaded("aecapfix")
            ? part(EMCInterfacePartAECF.class, EMCInterfacePartAECF::new)
            : part(EMCInterfacePart.class, EMCInterfacePart::new));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<EMCInterfaceBlockEntity>> EMC_INTERFACE_BE = BE_TYPES.register("emc_interface", () -> {
        var type = BlockEntityType.Builder.of(EMCInterfaceBlockEntity::new, EMC_INTERFACE.get()).build(null);
        EMC_INTERFACE.get().setBlockEntity(EMCInterfaceBlockEntity.class, type, null, null);
        return type;
    });

    static {
        MENU_TYPES.register("emc_interface", () -> EMCInterfaceMenu.TYPE);
        TABS.register(MODID, () -> CreativeModeTab.builder()
                .title(Component.translatable("mod." + MODID))
                .icon(() -> EMC_MODULE.get().getDefaultInstance())
                .displayItems((params, output) -> {
                    output.accept(EMC_MODULE.get());
                    output.accept(EMC_INTERFACE.get());
                    output.accept(CABLE_EMC_INTERFACE.get());
                })
                .build());
    }

    public AppliedE() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        MENU_TYPES.register(bus);
        BE_TYPES.register(bus);
        TABS.register(bus);

        bus.addListener((FMLCommonSetupEvent event) -> {
            registerEMC(AEItems.CERTUS_QUARTZ_CRYSTAL, 256);
            registerEMC(AEBlocks.SKY_STONE_BLOCK, 256);
            registerEMC(AEItems.QUANTUM_ENTANGLED_SINGULARITY, 0);
            registerEMC(AEParts.CABLE_ANCHOR, 32);
            registerEMC(AEItems.FACADE, 1); // will be replaced by FacadeProcessor
        });

        if (FMLEnvironment.dist.isClient()) {
            bus.addListener(EMCInterfaceScreen::register);
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static long clampedLong(BigInteger toClamp) {
        return toClamp.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    private static void registerEMC(ItemLike item, int emc) {
        APICustomEMCMapper.INSTANCE.registerCustomEMC(MODID, new CustomEMCRegistration(NSSItem.createItem(item), emc));
    }

    private static <P extends IPart> Item part(Class<P> partClass, Function<IPartItem<P>, P> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return new PartItem<>(new Item.Properties(), partClass, factory);
    }
}
// spotless:on
