package gripe._90.appliede.emc;

import java.util.Collections;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

import appeng.core.definitions.AEItems;

import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@SuppressWarnings("unused")
@EMCMapper
public class CondenserResultMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getName() {
        return "AE2MatterCondenser";
    }

    @Override
    public String getDescription() {
        return "Maps Applied Energistics 2 matter condenser results.";
    }

    @Override
    public void addMappings(
            IMappingCollector<NormalizedSimpleStack, Long> collector,
            CommentedFileConfig config,
            ReloadableServerResources resources,
            RegistryAccess access,
            ResourceManager resourceManager) {
        // very much hard-coded for now, as with the matter condenser itself
        var component1K = NSSItem.createItem(AEItems.CELL_COMPONENT_1K);
        var matterBall = NSSItem.createItem(AEItems.MATTER_BALL);
        collector.addConversion(1, component1K, Collections.singletonList(matterBall));
        collector.addConversion(1, matterBall, Collections.singletonList(component1K));

        var component64K = NSSItem.createItem(AEItems.CELL_COMPONENT_64K);
        var singularity = NSSItem.createItem(AEItems.SINGULARITY);
        collector.addConversion(1, component64K, Collections.singletonList(singularity));
        collector.addConversion(1, singularity, Collections.singletonList(component64K));
    }
}
