package gripe._90.appliede.mixin.crafting;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import appeng.api.networking.crafting.ICraftingProvider;

@Mixin(targets = "appeng.me.service.helpers.NetworkCraftingProviders$CraftingProviderList")
public interface CraftingProviderListAccess {
    @Invoker("add")
    void appliede$add(ICraftingProvider provider);

    @Invoker("remove")
    void appliede$remove(ICraftingProvider provider);

    @Accessor("providers")
    List<ICraftingProvider> appliede$providers();
}
