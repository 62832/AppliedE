package gripe._90.appliede.mixin.crafting;

import java.util.ArrayList;
import java.util.Iterator;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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
import gripe._90.appliede.me.service.TransmutationPattern;

@Mixin(value = CraftingTreeNode.class, remap = false)
public abstract class CraftingTreeNodeMixin {
    @Shadow
    private ArrayList<CraftingTreeProcess> nodes;

    @Shadow
    @Final
    private CraftingCalculation job;

    @Unique
    private long appliede$requestedAmount;

    @Inject(method = "request", at = @At("HEAD"))
    private void trackRequested(
            CraftingSimulationState inv, long requestedAmount, KeyCounter containerItems, CallbackInfo ci) {
        appliede$requestedAmount = requestedAmount;
    }

    // spotless:off
    @Inject(
            method = "buildChildPatterns",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            cancellable = true)
    // spotless:on
    private void recalculatePattern(
            CallbackInfo ci,
            IGridNode gridNode,
            ICraftingService craftingService,
            Iterator<IPatternDetails> iterator,
            IPatternDetails details) {
        if (details instanceof TransmutationPattern) {
            if (details.getOutputs()[0].what() instanceof AEItemKey item) {
                ci.cancel();
                details = new TransmutationPattern(item, appliede$requestedAmount);
                nodes.add(new CraftingTreeProcess(craftingService, job, details, (CraftingTreeNode) (Object) this));
            }

            gridNode.getGrid().getService(KnowledgeService.class).addTemporaryPattern(details);
        }
    }
}
