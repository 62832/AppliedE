package gripe._90.appliede.emc;

import java.util.Collections;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

import appeng.api.features.P2PTunnelAttunement;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.mixin.misc.P2PTunnelAttunementAccessor;

import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@SuppressWarnings("unused")
@EMCMapper
public class P2PTunnelMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getTranslationKey() {
        return "config." + AppliedE.MODID + ".mapper.p2p_tunnel";
    }

    @Override
    public String getName() {
        return "P2P Tunnel Mapper";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Maps Applied Energistics 2 P2P tunnels.";
    }

    @Override
    public void addMappings(
            IMappingCollector<NormalizedSimpleStack, Long> collector,
            ReloadableServerResources resources,
            RegistryAccess access,
            ResourceManager resourceManager) {
        for (var tunnel : P2PTunnelAttunementAccessor.getTagTunnels().values()) {
            var meTunnel = NSSItem.createItem(P2PTunnelAttunement.ME_TUNNEL);
            var otherTunnel = NSSItem.createItem(tunnel);
            collector.addConversion(1, meTunnel, Collections.singletonList(otherTunnel));
            collector.addConversion(1, otherTunnel, Collections.singletonList(meTunnel));
        }
    }
}
