package gripe._90.appliede.emc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@SuppressWarnings("unused")
@EMCMapper
public class FacadeMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getTranslationKey() {
        return "config." + AppliedE.MODID + ".mapper.facade";
    }

    @Override
    public String getName() {
        return "Facade Mapper";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Maps Applied Energistics 2 cable facades.";
    }

    @Override
    public void addMappings(
            IMappingCollector<NormalizedSimpleStack, Long> collector,
            ReloadableServerResources resources,
            RegistryAccess access,
            ResourceManager resourceManager) {
        var anchor = NSSItem.createItem(AEParts.CABLE_ANCHOR);
        var baseFacade = AEItems.FACADE.asItem();

        for (var block : BuiltInRegistries.BLOCK) {
            var blockStack = block.asItem().getDefaultInstance();
            var facade = baseFacade.createFacadeForItem(blockStack, false);

            if (!facade.isEmpty()) {
                var ingredients = new Object2IntOpenHashMap<NormalizedSimpleStack>();
                ingredients.put(anchor, 4);
                ingredients.put(NSSItem.createItem(blockStack), 1);
                collector.addConversion(4, NSSItem.createItem(facade), ingredients);
            }
        }
    }
}
