package gripe._90.appliede.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import cn.leomc.teamprojecte.TPTeam;
import cn.leomc.teamprojecte.TeamKnowledgeProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;

class TeamProjectEIntegration {
    private static final Set<TPTeam> TEAMS_SHARING_EMC = new HashSet<>();
    private static final Set<IKnowledgeProvider> PROVIDERS_TO_KEEP = new HashSet<>();

    static boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
        if (!(provider.getValue().get() instanceof TeamKnowledgeProvider teamProvider)) {
            return true;
        }

        var team = TPTeam.getOrCreateTeam(provider.getKey());

        if (team.isSharingEMC() && !TEAMS_SHARING_EMC.contains(team)) {
            TEAMS_SHARING_EMC.add(team);
            PROVIDERS_TO_KEEP.add(teamProvider);
            return true;
        }

        if (!team.isSharingEMC()) {
            TEAMS_SHARING_EMC.remove(team);
            PROVIDERS_TO_KEEP.remove(teamProvider);
        }

        return !TEAMS_SHARING_EMC.contains(team) || PROVIDERS_TO_KEEP.contains(teamProvider);
    }
}
