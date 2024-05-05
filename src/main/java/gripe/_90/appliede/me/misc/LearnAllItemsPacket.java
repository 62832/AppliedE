package gripe._90.appliede.me.misc;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;

import gripe._90.appliede.menu.TransmutationTerminalMenu;

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
                            var learned = emcStorage.learnNewItem(item, sender);
                            var meStorage = storage.getInventory();
                            meStorage.extract(item, learned, Actionable.MODULATE, IActionSource.ofMachine(host));

                            if (learned > 0) {
                                menu.showLearned();
                            }
                        });
            }
        });
        context.get().setPacketHandled(true);
    }
}
