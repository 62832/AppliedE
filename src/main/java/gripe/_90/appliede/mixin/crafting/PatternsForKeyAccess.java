package gripe._90.appliede.mixin.crafting;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "appeng.me.service.helpers.NetworkCraftingProviders$PatternsForKey")
public interface PatternsForKeyAccess {
    @Invoker("<init>")
    static PatternsForKeyAccess appliede$create() {
        throw new AssertionError();
    }

    @Accessor("patterns")
    Set<Object> appliede$infos();

    @Accessor("needsSorting")
    void appliede$setNeedsSorting(boolean needsSorting);
}
