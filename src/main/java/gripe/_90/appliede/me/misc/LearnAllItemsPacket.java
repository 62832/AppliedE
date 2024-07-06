package gripe._90.appliede.me.misc;

import java.util.function.Supplier;

import net.minecraftforge.network.NetworkEvent;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;

import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

public class LearnAllItemsPacket {
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            var sender = context.get().getSender();

            if (sender == null || !(sender.containerMenu instanceof TransmutationTerminalMenu menu)) {
                return;
            }

            var node = menu.getHost().getActionableNode();

            if (node == null) {
                return;
            }

            var grid = node.getGrid();

            if (grid == null) {
                return;
            }

            var storage = grid.getStorageService();
            var knowledge = grid.getService(KnowledgeService.class);

            if (knowledge.isTrackingPlayer(sender)) {
                storage.getCachedInventory().keySet().stream()
                        .filter(key -> key instanceof AEItemKey)
                        .map(AEItemKey.class::cast)
                        .forEach(item -> {
                            var learned = knowledge
                                    .getStorage()
                                    .insertItem(
                                            item,
                                            1,
                                            Actionable.MODULATE,
                                            IActionSource.ofPlayer(sender),
                                            true,
                                            true,
                                            menu::showLearned);

                            var me = storage.getInventory();
                            me.extract(item, learned, Actionable.MODULATE, IActionSource.ofMachine(menu.getHost()));
                        });
            }
        });
        context.get().setPacketHandled(true);
    }
}
