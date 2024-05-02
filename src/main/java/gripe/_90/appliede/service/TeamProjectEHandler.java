package gripe._90.appliede.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;

import gripe._90.appliede.AppliedE;

import cn.leomc.teamprojecte.TPTeam;
import cn.leomc.teamprojecte.TeamKnowledgeProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

class TeamProjectEHandler {
    private final Map<UUID, TPTeam> playersInSharingTeams = new HashMap<>();
    private final Map<TPTeam, IKnowledgeProvider> providersToKeep = new HashMap<>();

    TeamProjectEHandler() {
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            playersInSharingTeams.clear();
            providersToKeep.clear();
        });
    }

    boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
        if (!(provider.getValue().get() instanceof TeamKnowledgeProvider teamProvider)) {
            return true;
        }

        var uuid = provider.getKey();
        var team = TPTeam.getOrCreateTeam(uuid);

        if (team.isSharingEMC() && (!playersInSharingTeams.containsValue(team) || !providersToKeep.containsKey(team))) {
            playersInSharingTeams.put(uuid, team);
            providersToKeep.put(team, teamProvider);
            return true;
        }

        if (!team.isSharingEMC()) {
            playersInSharingTeams.entrySet().stream()
                    .filter(entry -> entry.getValue() == team)
                    .forEach(entry -> playersInSharingTeams.remove(entry.getKey()));
            providersToKeep.remove(team);
        }

        return !playersInSharingTeams.containsValue(team) || providersToKeep.containsValue(teamProvider);
    }

    void removeTeamReference(UUID member) {
        var team = playersInSharingTeams.remove(member);
        var provider = ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(member);

        if (team != null && providersToKeep.containsValue(provider)) {
            providersToKeep.remove(team);
        }
    }

    static class Proxy {
        private Object handler;

        Proxy() {
            if (AppliedE.isModLoaded("teamprojecte")) {
                handler = new TeamProjectEHandler();
            }
        }

        boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
            return handler == null || ((TeamProjectEHandler) handler).notSharingEmc(provider);
        }

        void removeTeamReference(UUID member) {
            if (handler != null) {
                ((TeamProjectEHandler) handler).removeTeamReference(member);
            }
        }
    }
}
