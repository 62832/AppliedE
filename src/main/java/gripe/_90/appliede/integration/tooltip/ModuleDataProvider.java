package gripe._90.appliede.integration.tooltip;

import java.util.Objects;

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
        var node = Objects.requireNonNull(module.getGridNode());
        var uuid = node.getOwningPlayerProfileId();

        if (uuid != null) {
            var profileCache = node.getLevel().getServer().getProfileCache();

            if (profileCache != null) {
                var profile = profileCache.get(uuid);
                profile.ifPresent(p -> serverData.putString("owner", p.getName()));
            }
        }
    }

    @Override
    public void buildTooltip(EMCModulePart module, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();

        if (serverData.contains("owner")) {
            var owner = serverData.getString("owner");
            tooltip.addLine(Component.translatable("tooltip." + AppliedE.MODID + ".owner", owner));
        }
    }
}
