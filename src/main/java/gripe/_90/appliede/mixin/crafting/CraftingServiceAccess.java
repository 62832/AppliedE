package gripe._90.appliede.mixin.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.me.service.CraftingService;
import appeng.me.service.helpers.NetworkCraftingProviders;

@Mixin(CraftingService.class)
public interface CraftingServiceAccess {
    @Accessor("craftingProviders")
    NetworkCraftingProviders appliede$craftingProviders();
}
