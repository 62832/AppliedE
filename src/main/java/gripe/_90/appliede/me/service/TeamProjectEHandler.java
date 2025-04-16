package gripe._90.appliede.me.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import appeng.api.features.IPlayerRegistry;

import cn.leomc.teamprojecte.TPTeam;
import cn.leomc.teamprojecte.TeamChangeEvent;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

final class TeamProjectEHandler {
    private final Map<TPTeam, IKnowledgeProvider> providersPerTeam = new HashMap<>();

    TeamProjectEHandler() {
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> clear());
        NeoForge.EVENT_BUS.addListener(TeamChangeEvent.class, event -> clear());
    }

    boolean sharingEMC(UUID uuid, IKnowledgeProvider provider) {
        var team = TPTeam.getOrCreateTeam(uuid);
        return team.isSharingEMC() && providersPerTeam.putIfAbsent(team, provider) == null;
    }

    IKnowledgeProvider getProviderFor(UUID uuid) {
        for (var team : providersPerTeam.keySet()) {
            if (team.getMembers().contains(uuid)) {
                return providersPerTeam.get(team);
            }
        }

        return null;
    }

    void syncTeamProviders(MinecraftServer server) {
        for (var team : providersPerTeam.keySet()) {
            for (var i = -1; i < team.getMembers().size(); i++) {
                var member = i == -1 ? team.getOwner() : team.getMembers().get(i);
                var id = IPlayerRegistry.getMapping(server).getPlayerId(member);
                var player = IPlayerRegistry.getConnected(server, id);

                if (player != null) {
                    ITransmutationProxy.INSTANCE
                            .getKnowledgeProviderFor(player.getUUID())
                            .syncEmc(player);
                }
            }
        }
    }

    void clear() {
        providersPerTeam.clear();
    }
}
