package gripe._90.appliede.me.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import appeng.api.features.IPlayerRegistry;

import cn.leomc.teamprojecte.TPTeam;
import cn.leomc.teamprojecte.TeamChangeEvent;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

final class TeamProjectEHandler {
    private final Map<TPTeam, IKnowledgeProvider> providersPerTeam = new HashMap<>();

    private TeamProjectEHandler() {
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> clear());
        NeoForge.EVENT_BUS.addListener(TeamChangeEvent.class, event -> clear());
    }

    private boolean notSharingEmc(Map.Entry<UUID, IKnowledgeProvider> entry) {
        var team = TPTeam.getOrCreateTeam(entry.getKey());
        var provider = entry.getValue();
        return !team.isSharingEMC()
                || providersPerTeam.containsValue(provider)
                || providersPerTeam.putIfAbsent(team, provider) == null;
    }

    private boolean isPlayerInTrackedTeam(UUID uuid) {
        for (var team : providersPerTeam.keySet()) {
            if (team.getMembers().contains(uuid)) {
                return true;
            }
        }

        return false;
    }

    private IKnowledgeProvider getProviderFor(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        for (var entry : providersPerTeam.entrySet()) {
            if (entry.getKey().getMembers().contains(uuid)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void syncTeamProviders() {
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server != null) {
            for (var team : providersPerTeam.keySet()) {
                var members = new ArrayList<>(team.getMembers());
                members.add(team.getOwner());

                for (var member : members) {
                    var id = IPlayerRegistry.getMapping(server).getPlayerId(member);
                    var player = IPlayerRegistry.getConnected(server, id);

                    if (player != null) {
                        ITransmutationProxy.INSTANCE
                                .getKnowledgeProviderFor(member)
                                .syncEmc(player);
                    }
                }
            }
        }
    }

    private void clear() {
        providersPerTeam.clear();
    }

    static class Proxy {
        private final Object handler = ModList.get().isLoaded("teamprojecte") ? new TeamProjectEHandler() : null;

        boolean notSharingEmc(Map.Entry<UUID, IKnowledgeProvider> provider) {
            return handler == null || ((TeamProjectEHandler) handler).notSharingEmc(provider);
        }

        boolean isPlayerInTrackedTeam(UUID uuid) {
            return handler == null || ((TeamProjectEHandler) handler).isPlayerInTrackedTeam(uuid);
        }

        IKnowledgeProvider getProviderFor(UUID uuid) {
            return handler != null ? ((TeamProjectEHandler) handler).getProviderFor(uuid) : null;
        }

        void syncTeamProviders(Map<UUID, IKnowledgeProvider> fallbackProviders) {
            if (handler != null) {
                ((TeamProjectEHandler) handler).syncTeamProviders();
            } else {
                var server = ServerLifecycleHooks.getCurrentServer();

                if (server != null) {
                    fallbackProviders.forEach((uuid, provider) -> {
                        var id = IPlayerRegistry.getMapping(server).getPlayerId(uuid);
                        var player = IPlayerRegistry.getConnected(server, id);

                        if (player != null) {
                            provider.syncEmc(player);
                        }
                    });
                }
            }
        }

        void clear() {
            if (handler != null) {
                ((TeamProjectEHandler) handler).clear();
            }
        }
    }
}
