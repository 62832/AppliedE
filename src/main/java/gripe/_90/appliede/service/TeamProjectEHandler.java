package gripe._90.appliede.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;

import gripe._90.appliede.AppliedE;

import cn.leomc.teamprojecte.TPTeam;
import cn.leomc.teamprojecte.TeamKnowledgeProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;

class TeamProjectEHandler {
    private final Map<UUID, TPTeam> teamsSharingEmc = new HashMap<>();
    private final Set<IKnowledgeProvider> providersToKeep = new HashSet<>();

    TeamProjectEHandler() {
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            teamsSharingEmc.clear();
            providersToKeep.clear();
        });
    }

    boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
        if (!(provider.getValue().get() instanceof TeamKnowledgeProvider teamProvider)) {
            return true;
        }

        var uuid = provider.getKey();
        var team = TPTeam.getOrCreateTeam(uuid);

        if (team.isSharingEMC() && !teamsSharingEmc.containsValue(team)) {
            teamsSharingEmc.put(uuid, team);
            providersToKeep.add(teamProvider);
            return true;
        }

        if (!team.isSharingEMC()) {
            teamsSharingEmc.entrySet().stream()
                    .filter(entry -> entry.getValue() == team)
                    .forEach(entry -> teamsSharingEmc.remove(entry.getKey()));
            providersToKeep.remove(teamProvider);
        }

        return !teamsSharingEmc.containsValue(team) || providersToKeep.contains(teamProvider);
    }

    void removeTeamReference(UUID member) {
        teamsSharingEmc.remove(member);
    }

    static class Proxy {
        private Object handler;

        Proxy() {
            if (AppliedE.isModLoaded("teamprojecte")) {
                handler = new TeamProjectEHandler();
            }
        }

        boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
            if (handler != null) {
                return ((TeamProjectEHandler) handler).notSharingEmc(provider);
            }

            return true;
        }

        void removeTeamReference(UUID member) {
            if (handler != null) {
                ((TeamProjectEHandler) handler).removeTeamReference(member);
            }
        }
    }
}
