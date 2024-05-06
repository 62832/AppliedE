package gripe._90.appliede.emc;

import java.util.Collections;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

import appeng.api.features.P2PTunnelAttunement;

import gripe._90.appliede.mixin.main.P2PTunnelAttunementAccessor;

import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@SuppressWarnings("unused")
@EMCMapper
public class P2PTunnelMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getName() {
        return "AE2P2PTunnels";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Maps Applied Energistics 2 P2P tunnels.";
    }

    @Override
    public void addMappings(
            IMappingCollector<NormalizedSimpleStack, Long> collector,
            CommentedFileConfig config,
            ReloadableServerResources resources,
            RegistryAccess access,
            ResourceManager resourceManager) {
        P2PTunnelAttunementAccessor.getTagTunnels().forEach((tag, tunnel) -> {
            var meTunnel = NSSItem.createItem(P2PTunnelAttunement.ME_TUNNEL);
            var otherTunnel = NSSItem.createItem(tunnel);
            collector.addConversion(1, meTunnel, Collections.singletonList(otherTunnel));
            collector.addConversion(1, otherTunnel, Collections.singletonList(meTunnel));
        });
    }
}
