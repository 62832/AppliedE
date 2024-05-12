package gripe._90.appliede.mixin.crafting;

import java.util.Iterator;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.energy.IEnergyService;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;

import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.me.service.TransmutationPattern;

@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class CraftingCPULogicMixin {
    @Shadow
    @Final
    CraftingCPUCluster cluster;

    @Inject(
            method = "executeCrafting",
            at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void removeOnFinishStep(
            int maxPatterns,
            CraftingService craftingService,
            IEnergyService energyService,
            Level level,
            CallbackInfoReturnable<Integer> cir,
            ExecutingCraftingJob job,
            int pushedPatterns,
            Iterator<?> it,
            Map.Entry<IPatternDetails, ?> task) {
        appliede$removeTemporaryPattern(task.getKey());
    }

    @Inject(
            method = "finishJob",
            at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void removeOnCancel(
            boolean success, CallbackInfo ci, Iterator<?> iter, Map.Entry<IPatternDetails, ?> entry) {
        appliede$removeTemporaryPattern(entry.getKey());
    }

    @Unique
    private void appliede$removeTemporaryPattern(IPatternDetails pattern) {
        if (pattern instanceof TransmutationPattern) {
            var grid = cluster.getGrid();

            if (grid != null) {
                grid.getService(KnowledgeService.class).removeTemporaryPattern(pattern);
            }
        }
    }
}
