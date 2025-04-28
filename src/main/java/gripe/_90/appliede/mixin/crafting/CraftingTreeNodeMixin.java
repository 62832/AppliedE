package gripe._90.appliede.mixin.crafting;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.CraftingTreeProcess;
import appeng.crafting.inv.CraftingSimulationState;

import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.me.service.KnowledgeServiceImpl;
import gripe._90.appliede.me.service.TransmutationPattern;

@Mixin(CraftingTreeNode.class)
public abstract class CraftingTreeNodeMixin {
    @Unique
    private long appliede$requestedAmount;

    @Inject(method = "request", at = @At("HEAD"))
    private void trackRequested(
            CraftingSimulationState inv, long requestedAmount, KeyCounter containerItems, CallbackInfo ci) {
        appliede$requestedAmount = requestedAmount;
    }

    // spotless:off
    @WrapOperation(
            method = "buildChildPatterns",
            at = @At(
                    value = "NEW",
                    target = "(Lappeng/api/networking/crafting/ICraftingService;Lappeng/crafting/CraftingCalculation;Lappeng/api/crafting/IPatternDetails;Lappeng/crafting/CraftingTreeNode;)Lappeng/crafting/CraftingTreeProcess;"))
    // spotless:on
    private CraftingTreeProcess recalculatePattern(
            ICraftingService craftingService,
            CraftingCalculation job,
            IPatternDetails details,
            CraftingTreeNode node,
            Operation<CraftingTreeProcess> original,
            @Local IGridNode gridNode) {
        if (details instanceof TransmutationPattern) {
            if (details.getOutputs().getFirst().what() instanceof AEItemKey item) {
                details = new TransmutationPattern(item, appliede$requestedAmount, job.hashCode());
            }

            ((KnowledgeServiceImpl) gridNode.getGrid().getService(KnowledgeService.class)).addTemporaryPattern(details);
        }

        return original.call(craftingService, job, details, node);
    }
}
