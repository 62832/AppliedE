package gripe._90.appliede.me.misc;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.api.KnowledgeService;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

public class LearnAllItemsPacket implements CustomPacketPayload {
    public static final LearnAllItemsPacket INSTANCE = new LearnAllItemsPacket();

    public static final Type<LearnAllItemsPacket> TYPE = new CustomPacketPayload.Type<>(AppliedE.id("learn_all"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LearnAllItemsPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    private LearnAllItemsPacket() {}

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var sender = context.player();

            if (!(sender.containerMenu instanceof TransmutationTerminalMenu menu)) {
                return;
            }

            var node = menu.getActionableNode();

            if (node == null) {
                return;
            }

            var provider = node.getGrid().getService(KnowledgeService.class).getProviderFor(sender.getUUID());

            if (provider != null) {
                var storageService = node.getGrid().getStorageService();

                for (var key : storageService.getCachedInventory().keySet()) {
                    if (!(key instanceof AEItemKey item)) {
                        continue;
                    }

                    if (provider.hasKnowledge(item.toStack())) {
                        continue;
                    }

                    var storage = storageService.getInventory();
                    var learned = storage.insert(item, 1, Actionable.MODULATE, IActionSource.ofPlayer(sender, menu));
                    storage.extract(item, learned, Actionable.MODULATE, IActionSource.ofMachine(menu));
                }
            }
        });
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
