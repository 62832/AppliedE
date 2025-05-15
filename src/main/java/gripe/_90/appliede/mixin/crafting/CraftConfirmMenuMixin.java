package gripe._90.appliede.mixin.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Player;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.menu.me.crafting.CraftConfirmMenu;

import gripe._90.appliede.api.KnowledgeService;
import gripe._90.appliede.me.service.KnowledgeServiceImpl;
import gripe._90.appliede.me.service.TransmutationPattern;

@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuMixin {
    @Shadow
    private ICraftingPlan result;

    @Unique
    private boolean appliede$submitted = false;

    @Shadow
    protected abstract IGrid getGrid();

    // spotless:off
    @Inject(
            method = "startJob",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/crafting/CraftConfirmMenu;setAutoStart(Z)V"))
    // spotless:on
    private void setSubmitted(CallbackInfo ci) {
        appliede$submitted = true;
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void clearTemporaryPatterns(Player player, CallbackInfo ci) {
        if (getGrid() != null && result != null && !appliede$submitted) {
            for (var pattern : result.patternTimes().keySet()) {
                if (pattern instanceof TransmutationPattern) {
                    ((KnowledgeServiceImpl) getGrid().getService(KnowledgeService.class))
                            .removeTemporaryPattern(pattern);
                }
            }
        }
    }
}
