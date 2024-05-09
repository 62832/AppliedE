package gripe._90.appliede.integration;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.forgespi.language.IModInfo;

public enum Addons {
    TEAMPE("teamprojecte"),
    AE2WTLIB("ae2wtlib"),
    AECAPFIX("aecapfix");

    private final String modId;

    Addons(String modId) {
        this.modId = modId;
    }

    public String getModId() {
        return modId;
    }

    public boolean isLoaded() {
        return ModList.get() != null
                ? ModList.get().isLoaded(modId)
                : LoadingModList.get().getMods().stream()
                        .map(IModInfo::getModId)
                        .anyMatch(modId::equals);
    }
}
