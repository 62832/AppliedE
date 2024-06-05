package gripe._90.appliede.mixin.teamprojecte;

import java.math.BigInteger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.leomc.teamprojecte.TeamKnowledgeProvider;

@Mixin(value = TeamKnowledgeProvider.class, remap = false)
public abstract class TeamKnowledgeProviderMixin {
    // spotless:off
    @Inject(
            method = "setEmc",
            at = @At(
                    value = "INVOKE",
                    target = "Lcn/leomc/teamprojecte/TeamKnowledgeProvider;syncEmc(Ljava/util/UUID;)V"),
            cancellable = true)
    // spotless:on
    private void suppressSync(BigInteger emc, CallbackInfo ci) {
        ci.cancel();
    }
}
