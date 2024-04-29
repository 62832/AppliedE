package gripe._90.appliede.module;

import appeng.api.integrations.igtooltip.PartTooltips;
import appeng.api.integrations.igtooltip.TooltipProvider;

@SuppressWarnings("UnstableApiUsage")
public class ModuleTooltipProvider implements TooltipProvider {
    static {
        PartTooltips.addBody(EMCModulePart.class, ModuleDataProvider.INSTANCE);
        PartTooltips.addServerData(EMCModulePart.class, ModuleDataProvider.INSTANCE);
    }
}
