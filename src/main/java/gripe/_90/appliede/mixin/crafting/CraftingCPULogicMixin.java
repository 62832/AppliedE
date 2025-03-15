package gripe._90.appliede.mixin.crafting;

import java.util.Map;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.energy.IEnergyService;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;

import gripe._90.appliede.me.misc.TransmutationPattern;
import gripe._90.appliede.me.service.KnowledgeService;

@Mixin(CraftingCpuLogic.class)
public abstract class CraftingCPULogicMixin {
    @Shadow
    @Final
    CraftingCPUCluster cluster;

    @Inject(method = "executeCrafting", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
    private void removeOnFinishStep(
            int maxPatterns,
            CraftingService craftingService,
            IEnergyService energyService,
            Level level,
            CallbackInfoReturnable<Integer> cir,
            @Local Map.Entry<IPatternDetails, ?> task) {
        appliede$removeTemporaryPattern(task.getKey());
    }

    @Inject(
            method = "finishJob",
            at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;"))
    private void removeOnCancel(boolean success, CallbackInfo ci, @Local Map.Entry<IPatternDetails, ?> entry) {
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
