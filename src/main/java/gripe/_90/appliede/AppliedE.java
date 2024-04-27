package gripe._90.appliede;

import java.math.BigInteger;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import appeng.api.networking.GridServices;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;

import gripe._90.appliede.key.EMCKeyType;
import gripe._90.appliede.key.EMCRenderer;
import gripe._90.appliede.module.EMCModulePart;
import gripe._90.appliede.module.KnowledgeService;
import gripe._90.appliede.pattern.TransmutationPatternItem;

import moze_intel.projecte.gameObjs.registries.PECreativeTabs;

@Mod(AppliedE.MODID)
public final class AppliedE {
    public static final String MODID = "appliede";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> EMC_MODULE = ITEMS.register(
            "emc_module",
            () -> Util.make(() -> {
                PartModels.registerModels(PartModelsHelper.createModels(EMCModulePart.class));
                return new PartItem<>(new Item.Properties(), EMCModulePart.class, EMCModulePart::new);
            }));
    public static final RegistryObject<Item> TRANSMUTATION_PATTERN =
            ITEMS.register("transmutation_pattern", TransmutationPatternItem::new);

    public static final BigInteger TIER_LIMIT = BigInteger.valueOf((long) Math.pow(2, 42));

    public AppliedE() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        bus.addListener(EMCKeyType::register);
        bus.addListener(this::addToCreativeTab);

        GridServices.register(KnowledgeService.class, KnowledgeService.class);

        if (FMLEnvironment.dist.isClient()) {
            EMCRenderer.register();
        }
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab().equals(PECreativeTabs.PROJECTE.get())) {
            event.accept(EMC_MODULE::get);
        }
    }
}
