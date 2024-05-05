package gripe._90.appliede.me.misc;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.ITerminalHost;

import gripe._90.appliede.me.service.KnowledgeService;

public interface ITransmutationTerminalHost extends ITerminalHost, IActionHost {
    boolean getShiftToTransmute();

    void setShiftToTransmute(boolean toggle);

    @Nullable
    KnowledgeService getKnowledgeService();

    @Nullable
    IStorageService getStorageService();
}
