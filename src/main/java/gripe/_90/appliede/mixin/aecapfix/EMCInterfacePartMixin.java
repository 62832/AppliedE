package gripe._90.appliede.mixin.aecapfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import appeng.api.parts.IPartItem;
import appeng.parts.AEBasePart;

import gripe._90.aecapfix.AECapFix;
import gripe._90.appliede.me.misc.EMCInterfaceLogic;
import gripe._90.appliede.part.EMCInterfacePart;

@Mixin(EMCInterfacePart.class)
public abstract class EMCInterfacePartMixin extends AEBasePart implements AECapFix.Invalidator {
    public EMCInterfacePartMixin(IPartItem<?> partItem) {
        super(partItem);
    }

    @Shadow(remap = false)
    public abstract EMCInterfaceLogic getInterfaceLogic();

    @Override
    public void aecapfix$invalidate() {
        getInterfaceLogic().invalidateCaps();
    }
}
