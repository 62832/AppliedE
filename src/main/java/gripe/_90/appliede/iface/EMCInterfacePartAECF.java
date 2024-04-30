package gripe._90.appliede.iface;

import appeng.api.parts.IPartItem;

import gripe._90.aecapfix.AECapFix;

public class EMCInterfacePartAECF extends EMCInterfacePart implements AECapFix.Invalidator {
    public EMCInterfacePartAECF(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void aecapfix$invalidate() {
        getInterfaceLogic().invalidateCaps();
    }
}
