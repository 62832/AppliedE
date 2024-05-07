package gripe._90.appliede.integration.tooltip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.part.EMCModulePart;

@SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
public class ModuleDataProvider implements BodyProvider<EMCModulePart>, ServerDataProvider<EMCModulePart> {
    static final ModuleDataProvider INSTANCE = new ModuleDataProvider();

    private ModuleDataProvider() {}

    @Override
    public void provideServerData(Player player, EMCModulePart module, CompoundTag serverData) {
        var node = module.getGridNode();
        if (node == null) return;

        var uuid = node.getOwningPlayerProfileId();
        if (uuid == null) return;

        var profileCache = node.getLevel().getServer().getProfileCache();
        if (profileCache == null) return;

        var profile = profileCache.get(uuid);
        profile.ifPresent(p -> serverData.putString("owner", p.getName()));
    }

    @Override
    public void buildTooltip(EMCModulePart module, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();

        if (serverData.contains("owner")) {
            tooltip.addLine(tooltipFor(serverData.getString("owner")));
        }
    }

    private Component tooltipFor(String username) {
        return Component.translatable("tooltip." + AppliedE.MODID + ".owner", username);
    }
}
