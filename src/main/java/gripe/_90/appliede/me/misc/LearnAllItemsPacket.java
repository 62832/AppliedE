package gripe._90.appliede.me.misc;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;

import gripe._90.appliede.menu.TransmutationTerminalMenu;

import moze_intel.projecte.api.proxy.IEMCProxy;

public class LearnAllItemsPacket {
    public void encode(FriendlyByteBuf ignored) {}

    public static LearnAllItemsPacket decode(FriendlyByteBuf ignored) {
        return new LearnAllItemsPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            var sender = context.get().getSender();

            if (sender == null || !(sender.containerMenu instanceof TransmutationTerminalMenu menu)) {
                return;
            }

            var host = menu.getHost();
            var storage = host.getStorageService();
            var knowledge = host.getKnowledgeService();

            if (storage != null && knowledge != null && knowledge.isTrackingPlayer(sender.getUUID())) {
                var emcStorage = knowledge.getStorage();
                storage.getCachedInventory().keySet().stream()
                        .filter(key -> key instanceof AEItemKey item && !knowledge.knowsItem(item))
                        .map(AEItemKey.class::cast)
                        .forEach(item -> {
                            if (!IEMCProxy.INSTANCE.hasValue(item.toStack())) {
                                return;
                            }

                            storage.getInventory().extract(item, 1, Actionable.MODULATE, IActionSource.ofMachine(host));
                            emcStorage.insertItem(item, 1, Actionable.MODULATE, IActionSource.ofPlayer(sender), true);
                        });
            }
        });
    }
}
