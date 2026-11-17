package gripe._90.appliede.mixin.crafting;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;

@Mixin(targets = "appeng.me.service.helpers.NetworkCraftingProviders$ProviderState")
public interface ProviderStateAccess {
    @Accessor("patterns")
    List<IPatternDetails> appliede$patterns();

    @Accessor("provider")
    ICraftingProvider appliede$provider();
}
