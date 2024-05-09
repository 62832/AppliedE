package gripe._90.appliede.me.misc;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;

import gripe._90.appliede.me.service.KnowledgeService;
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
            var grid = host.getGrid();

            if (grid == null) {
                return;
            }

            var storage = grid.getStorageService();
            var knowledge = grid.getService(KnowledgeService.class);

            if (storage != null && knowledge != null && knowledge.isTrackingPlayer(sender)) {
                var emc = knowledge.getStorage();
                storage.getCachedInventory().keySet().stream()
                        .filter(key -> key instanceof AEItemKey)
                        .map(AEItemKey.class::cast)
                        .forEach(item -> {
                            var learned = emc.insertItem(
                                    item,
                                    1,
                                    Actionable.MODULATE,
                                    IActionSource.ofPlayer(sender),
                                    true,
                                    menu::showLearned);

                            var me = storage.getInventory();
                            me.extract(item, learned, Actionable.MODULATE, IActionSource.ofMachine(host));
                        });
            }
        });
        context.get().setPacketHandled(true);
    }
}
