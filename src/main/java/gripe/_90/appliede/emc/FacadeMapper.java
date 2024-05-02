package gripe._90.appliede.emc;

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
public class FacadeMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getName() {
        return "AE2FacadeMapper";
    }

    @Override
    public String getDescription() {
        return """
               (AppliedE) Maps the AE2 cable facade to an initial value of 1 for use with the facade NBT processor.
               Disabling this mapper will prevent AE2FacadeProcessor (under processing.toml) from running.""";
    }

    @Override
    public void addMappings(
            IMappingCollector<NormalizedSimpleStack, Long> collector,
            CommentedFileConfig config,
            ReloadableServerResources resources,
            RegistryAccess access,
            ResourceManager resourceManager) {
        collector.setValueBefore(NSSItem.createItem(AEItems.FACADE.asItem()), 1L);
    }
}
