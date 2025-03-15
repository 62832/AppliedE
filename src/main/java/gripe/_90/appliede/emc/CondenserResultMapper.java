package gripe._90.appliede.emc;

import java.util.Collections;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

import appeng.core.definitions.AEItems;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@SuppressWarnings("unused")
@EMCMapper
public class CondenserResultMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getTranslationKey() {
        return "config." + AppliedE.MODID + ".mapper.condenser";
    }

    @Override
    public String getName() {
        return "Condenser Mapper";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Maps Applied Energistics 2 matter condenser results.";
    }

    @Override
    public void addMappings(
            IMappingCollector<NormalizedSimpleStack, Long> collector,
            ReloadableServerResources resources,
            RegistryAccess access,
            ResourceManager resourceManager) {
        // very much hard-coded for now, as with the matter condenser itself
        // also, don't give matter balls too high of a default value since they're much easier to get
        collector.setValueBefore(NSSItem.createItem(AEItems.MATTER_BALL), 512L);

        var component64K = NSSItem.createItem(AEItems.CELL_COMPONENT_64K);
        var singularity = NSSItem.createItem(AEItems.SINGULARITY);
        collector.addConversion(1, component64K, Collections.singletonList(singularity));
        collector.addConversion(1, singularity, Collections.singletonList(component64K));
    }
}
