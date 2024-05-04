package gripe._90.appliede.me.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;

import gripe._90.appliede.AppliedE;

import cn.leomc.teamprojecte.TPTeam;
import cn.leomc.teamprojecte.TeamChangeEvent;
import cn.leomc.teamprojecte.TeamKnowledgeProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

class TeamProjectEHandler {
    private final Map<UUID, TPTeam> playersInSharingTeams = new HashMap<>();
    private final Map<TPTeam, Supplier<IKnowledgeProvider>> providersToKeep = new HashMap<>();

    private TeamProjectEHandler() {
        MinecraftForge.EVENT_BUS.addListener(this::onTeamChange);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStop);
    }

    private boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
        if (!(provider.getValue().get() instanceof TeamKnowledgeProvider)) {
            return true;
        }

        var uuid = provider.getKey();
        var team = TPTeam.getOrCreateTeam(uuid);

        if (team.isSharingEMC() && (!playersInSharingTeams.containsValue(team))) {
            playersInSharingTeams.put(uuid, team);
            providersToKeep.putIfAbsent(team, provider.getValue());
            return true;
        }

        return !playersInSharingTeams.containsValue(team) || providersToKeep.containsValue(provider.getValue());
    }

    private void removeTeamReference(UUID member) {
        var team = playersInSharingTeams.remove(member);

        if (team != null && !playersInSharingTeams.containsValue(team)) {
            providersToKeep.remove(team);
        }
    }

    private void onTeamChange(TeamChangeEvent event) {
        var uuid = event.getPlayerUUID();
        providersToKeep.remove(playersInSharingTeams.remove(uuid));

        var team = event.getTeam();

        if (team != null && !team.isSharingEMC()) {
            playersInSharingTeams.entrySet().stream()
                    .filter(entry -> entry.getValue() == team)
                    .forEach(entry -> playersInSharingTeams.remove(entry.getKey()));
            providersToKeep.remove(team);
        }

        var newTeam = event.getNewTeam();

        if (newTeam != null && uuid != null) {
            playersInSharingTeams.put(uuid, newTeam);

            if (newTeam.isSharingEMC()) {
                providersToKeep.putIfAbsent(newTeam, () -> ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(uuid));
            }
        }
    }

    private void onServerStop(ServerStoppedEvent event) {
        playersInSharingTeams.clear();
        providersToKeep.clear();
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
