package gripe._90.appliede.emc;

import java.util.HashMap;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;

import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@SuppressWarnings("unused")
@EMCMapper
public class FacadeMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getName() {
        return "AE2FacadeMapper";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Maps Applied Energistics 2 cable facades.";
    }

    @Override
    public void addMappings(
            IMappingCollector<NormalizedSimpleStack, Long> collector,
            CommentedFileConfig config,
            ReloadableServerResources resources,
            RegistryAccess access,
            ResourceManager resourceManager) {
        var anchor = NSSItem.createItem(AEParts.CABLE_ANCHOR);
        var baseFacade = AEItems.FACADE.asItem();

        for (var block : ForgeRegistries.BLOCKS.getValues()) {
            var blockStack = block.asItem().getDefaultInstance();
            var facade = baseFacade.createFacadeForItem(blockStack, false);

            if (!facade.isEmpty()) {
                var ingredients = new HashMap<NormalizedSimpleStack, Integer>();
                ingredients.put(anchor, 4);
                ingredients.put(NSSItem.createItem(blockStack), 1);
                collector.addConversion(4, NSSItem.createItem(facade), ingredients);
            }
        }
    }
}
