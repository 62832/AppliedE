package gripe._90.appliede;

import java.math.BigInteger;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
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

@Mod(AppliedE.MODID)
public final class AppliedE {
    public static final String MODID = "appliede";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static final BigInteger TIER_LIMIT = BigInteger.valueOf((long) Math.pow(2, 42));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Item> EMC_MODULE = ITEMS.register(
            "emc_module",
            () -> Util.make(() -> {
                PartModels.registerModels(PartModelsHelper.createModels(EMCModulePart.class));
                return new PartItem<>(new Item.Properties(), EMCModulePart.class, EMCModulePart::new);
            }));
    public static final RegistryObject<Item> TRANSMUTATION_PATTERN =
            ITEMS.register("transmutation_pattern", TransmutationPatternItem::new);

    @SuppressWarnings("unused")
    private static final RegistryObject<CreativeModeTab> TAB = TABS.register(MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("mod." + MODID))
            .icon(() -> EMC_MODULE.get().getDefaultInstance())
            .displayItems((params, output) -> output.accept(EMC_MODULE.get()))
            .build());

    public AppliedE() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        TABS.register(bus);
        bus.addListener(EMCKeyType::register);

        GridServices.register(KnowledgeService.class, KnowledgeService.class);

        if (FMLEnvironment.dist.isClient()) {
            EMCRenderer.register();
        }
    }
}
