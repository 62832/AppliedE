package gripe._90.appliede.mixin.crafting;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.me.service.helpers.NetworkCraftingProviders;

import gripe._90.appliede.me.service.PatternIndex;

@Mixin(NetworkCraftingProviders.class)
public class NetworkCraftingProvidersMixin implements PatternIndex {
    @Shadow
    private Map<IGridNode, Object> craftingProviders;

    @Shadow
    private Map<AEKey, Object> craftableItems;

    @Shadow
    private Map<IPatternDetails, Object> craftingMethods;

    @Shadow
    private KeyCounter craftableItemsList;

    @Unique
    private static MethodHandle appliede$patternInfoConstructor;

    @Shadow
    private void setLastModifiedOnTick() {}

    @Override
    public boolean appliede$hasProvider(IGridNode node) {
        return node != null && craftingProviders.containsKey(node);
    }

    @Override
    public void appliede$replacePatterns(IGridNode node, List<IPatternDetails> removed, List<IPatternDetails> added) {
        if (!(craftingProviders.get(node) instanceof ProviderStateAccess state)) {
            return;
        }

        for (var pattern : removed) {
            appliede$removeOne(state, pattern);
        }

        for (var pattern : added) {
            appliede$addOne(state, pattern);
        }

        if (!removed.isEmpty() || !added.isEmpty()) {
            setLastModifiedOnTick();
        }
    }

    @Unique
    private void appliede$addOne(ProviderStateAccess state, IPatternDetails pattern) {
        if (pattern.getPrimaryOutput() == null || state.appliede$patterns().contains(pattern)) {
            return;
        }

        state.appliede$patterns().add(pattern);
        var output = pattern.getPrimaryOutput().what();
        craftableItemsList.add(output, 1);

        var patternsForKey = (PatternsForKeyAccess) craftableItems.computeIfAbsent(output, key -> PatternsForKeyAccess.appliede$create());
        patternsForKey.appliede$infos().add(appliede$patternInfo(pattern, state));
        patternsForKey.appliede$setNeedsSorting(true);

        var providers = (CraftingProviderListAccess)
                craftingMethods.computeIfAbsent(pattern, key -> CraftingProviderListAccess.appliede$create());
        providers.appliede$add(state.appliede$provider());
    }

    @Unique
    private void appliede$removeOne(ProviderStateAccess state, IPatternDetails pattern) {
        if (pattern.getPrimaryOutput() == null || !state.appliede$patterns().remove(pattern)) {
            return;
        }

        var output = pattern.getPrimaryOutput().what();
        craftableItemsList.remove(output, 1);
        var patternsForKey = (PatternsForKeyAccess) craftableItems.get(output);

        if (patternsForKey != null) {
            patternsForKey.appliede$infos().remove(appliede$patternInfo(pattern, state));
            patternsForKey.appliede$setNeedsSorting(true);

            if (patternsForKey.appliede$infos().isEmpty()) {
                craftableItems.remove(output);
            }
        }

        craftingMethods.computeIfPresent(pattern, (key, providers) -> {
            var list = (CraftingProviderListAccess) providers;
            list.appliede$remove(state.appliede$provider());
            return list.appliede$providers().isEmpty() ? null : providers;
        });
    }

    @Unique
    private Object appliede$patternInfo(IPatternDetails pattern, Object state) {
        try {
            if (appliede$patternInfoConstructor == null) {
                var type = Class.forName("appeng.me.service.helpers.NetworkCraftingProviders$PatternInfo");
                appliede$patternInfoConstructor = MethodHandles.lookup()
                        .findConstructor(type, MethodType.methodType(void.class, IPatternDetails.class, state.getClass()));
            }

            return appliede$patternInfoConstructor.invoke(pattern, state);
        } catch (Throwable failure) {
            throw new IllegalStateException("Could not create an AE2 pattern entry", failure);
        }
    }
}
