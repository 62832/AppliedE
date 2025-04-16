package gripe._90.appliede.me.misc;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.service.KnowledgeService;
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

            var node = Objects.requireNonNull(menu.getHost().getActionableNode());
            var storage = node.getGrid().getStorageService();
            var knowledge = node.getGrid().getService(KnowledgeService.class);

            if (knowledge.isTrackingPlayer(sender)) {
                for (var key : storage.getCachedInventory().keySet()) {
                    if (!(key instanceof AEItemKey item)) {
                        continue;
                    }

                    var provider = knowledge.getProviderFor(sender.getUUID());

                    if (provider == null || provider.hasKnowledge(item.toStack())) {
                        continue;
                    }

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
