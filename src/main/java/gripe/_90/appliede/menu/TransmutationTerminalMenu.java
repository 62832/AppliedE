package gripe._90.appliede.menu;

import moze_intel.projecte.api.ItemInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.FakeSlot;

import gripe._90.appliede.me.misc.TransmutationTerminalHost;
import gripe._90.appliede.me.service.KnowledgeService;

public class TransmutationTerminalMenu extends MEStorageMenu {
    public static final MenuType<TransmutationTerminalMenu> TYPE = MenuTypeBuilder.create(
                    TransmutationTerminalMenu::new, TransmutationTerminalHost.class)
            .build("transmutation_terminal");
    protected static final SlotSemantic TRANSMUTE = SlotSemantics.register("APPLIEDE_TRANSMUTE", false);
    protected static final SlotSemantic UNLEARN = SlotSemantics.register("APPLIEDE_UNLEARN", false);

    private static final String ACTION_SET_SHIFT = "setShiftDestination";
    private static final String ACTION_HIDE_LEARNED = "hideLearnedText";
    private static final String ACTION_HIDE_UNLEARNED = "hideUnlearnedText";

    private final TransmutationTerminalHost host;
    private final Slot transmuteSlot = new FakeSlot(InternalInventory.empty(), 0);
    private final Slot unlearnSlot = new FakeSlot(InternalInventory.empty(), 0);

    @GuiSync(1)
    public boolean shiftToTransmute;

    @GuiSync(2)
    public int learnedLabelTicks;

    @GuiSync(3)
    public int unlearnedLabelTicks;

    public TransmutationTerminalMenu(int id, Inventory ip, TransmutationTerminalHost host) {
        this(TYPE, id, ip, host, true);
    }

    public TransmutationTerminalMenu(
            MenuType<?> menuType, int id, Inventory ip, TransmutationTerminalHost host, boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory);
        this.host = host;
        registerClientAction(ACTION_SET_SHIFT, Boolean.class, host::setShiftToTransmute);
        registerClientAction(ACTION_HIDE_LEARNED, () -> learnedLabelTicks--);
        registerClientAction(ACTION_HIDE_UNLEARNED, () -> unlearnedLabelTicks--);
        addSlot(transmuteSlot, TRANSMUTE);
        addSlot(unlearnSlot, UNLEARN);
    }

    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        super.doAction(player, action, slot, id);
        var s = getSlot(slot);

        if (s.equals(transmuteSlot) && !getCarried().isEmpty()) {
            var transmuted = transmuteItem(getCarried(), action == InventoryAction.SPLIT_OR_PLACE_SINGLE, player);
            var reduced = getCarried().copy();
            reduced.setCount(reduced.getCount() - transmuted);
            setCarried(reduced.getCount() <= 0 ? ItemStack.EMPTY : reduced);
        }

        if (s.equals(unlearnSlot) && !getCarried().isEmpty()) {
            var node = host.getActionableNode();

            if (node != null) {
                var knowledge = node.getGrid().getService(KnowledgeService.class);

                if (knowledge.isTrackingPlayer(player)) {
                    var provider = knowledge.getProviderFor(player.getUUID()).get();
                    provider.removeKnowledge(getCarried());
                    provider.syncKnowledgeChange(player, ItemInfo.fromStack(getCarried()), false);
                    unlearnedLabelTicks = 300;
                    learnedLabelTicks = 0;
                    broadcastChanges();
                }
            }
        }
    }

    private int transmuteItem(ItemStack stack, boolean singleItem, Player player) {
        if (!stack.isEmpty()) {
            var node = host.getActionableNode();

            if (node == null) {
                return 0;
            }

            var knowledge = node.getGrid().getService(KnowledgeService.class);

            if (!knowledge.isTrackingPlayer(player)) {
                return 0;
            }

            return (int) knowledge
                    .getStorage()
                    .insertItem(
                            AEItemKey.of(stack),
                            singleItem ? 1 : stack.getCount(),
                            Actionable.MODULATE,
                            IActionSource.ofPlayer(player),
                            true,
                            true,
                            this::showLearned);
        }

        return 0;
    }

    public void setShiftToTransmute(boolean transmute) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_SHIFT, transmute);
            return;
        }

        shiftToTransmute = transmute;
    }

    public void showLearned() {
        learnedLabelTicks = 300;
        unlearnedLabelTicks = 0;
        broadcastChanges();
    }

    public void decrementLearnedTicks() {
        if (isClientSide()) {
            sendClientAction(ACTION_HIDE_LEARNED);
        }
    }

    public void decrementUnlearnedTicks() {
        if (isClientSide()) {
            sendClientAction(ACTION_HIDE_UNLEARNED);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        if (shiftToTransmute) {
            var stack = slots.get(idx).getItem();

            var remaining = stack.copy();
            remaining.setCount(remaining.getCount() - transmuteItem(stack, false, player));
            slots.get(idx).set(remaining.getCount() <= 0 ? ItemStack.EMPTY : remaining);

            return ItemStack.EMPTY;
        }

        return super.quickMoveStack(player, idx);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServerSide()) {
            shiftToTransmute = host.getShiftToTransmute();
        }
    }

    @Override
    public TransmutationTerminalHost getHost() {
        return host;
    }
}
