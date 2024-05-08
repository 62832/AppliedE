package gripe._90.appliede.me.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;

import gripe._90.appliede.integration.Addons;

import cn.leomc.teamprojecte.TPTeam;
import cn.leomc.teamprojecte.TeamChangeEvent;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;

class TeamProjectEHandler {
    private final Map<TPTeam, Supplier<IKnowledgeProvider>> providersPerTeam = new HashMap<>();

    private TeamProjectEHandler() {
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> clear());
        MinecraftForge.EVENT_BUS.addListener((TeamChangeEvent event) -> clear());
    }

    private boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> entry) {
        var team = TPTeam.getOrCreateTeam(entry.getKey());
        var provider = entry.getValue();
        return !team.isSharingEMC()
                || providersPerTeam.containsValue(provider)
                || providersPerTeam.putIfAbsent(team, provider) == null;
    }

    private boolean isPlayerInTrackedTeam(UUID uuid) {
        return providersPerTeam.keySet().stream()
                .anyMatch(team -> team.getMembers().contains(uuid));
    }

    public Supplier<IKnowledgeProvider> getProviderFor(UUID uuid) {
        return providersPerTeam.keySet().stream()
                .filter(team -> team.getMembers().contains(uuid))
                .findFirst()
                .map(providersPerTeam::get)
                .orElse(null);
    }

    private void clear() {
        providersPerTeam.clear();
    }

    static class Proxy {
        private Object handler;

        Proxy() {
            if (Addons.TEAMPE.isLoaded()) {
                handler = new TeamProjectEHandler();
            }
        }

        boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
            return handler == null || ((TeamProjectEHandler) handler).notSharingEmc(provider);
        }

        boolean isPlayerInTrackedTeam(UUID uuid) {
            return handler == null || ((TeamProjectEHandler) handler).isPlayerInTrackedTeam(uuid);
        }

        Supplier<IKnowledgeProvider> getProviderFor(UUID uuid) {
            return handler != null ? ((TeamProjectEHandler) handler).getProviderFor(uuid) : null;
        }

        void clear() {
            if (handler != null) {
                ((TeamProjectEHandler) handler).clear();
            }
        }
    }
}
