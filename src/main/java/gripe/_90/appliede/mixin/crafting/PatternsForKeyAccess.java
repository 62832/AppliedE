package gripe._90.appliede.mixin.crafting;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "appeng.me.service.helpers.NetworkCraftingProviders$PatternsForKey")
public interface PatternsForKeyAccess {
    @Accessor("patterns")
    Set<Object> appliede$infos();

    @Accessor("needsSorting")
    void appliede$setNeedsSorting(boolean needsSorting);
}
