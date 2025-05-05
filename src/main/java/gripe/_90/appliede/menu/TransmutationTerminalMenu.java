package gripe._90.appliede.menu;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.FakeSlot;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.misc.TransmutationCapable;
import gripe._90.appliede.me.misc.TransmutationTerminalHost;
import gripe._90.appliede.me.service.KnowledgeService;

import moze_intel.projecte.api.ItemInfo;

public class TransmutationTerminalMenu extends MEStorageMenu implements TransmutationCapable {
    protected static final SlotSemantic TRANSMUTE = SlotSemantics.register("APPLIEDE_TRANSMUTE", false);
    protected static final SlotSemantic UNLEARN = SlotSemantics.register("APPLIEDE_UNLEARN", false);

    private static final String ACTION_TOGGLE_SHIFT = "toggleShiftDestination";
    private static final String ACTION_HIDE_LEARNED = "hideLearnedText";
    private static final String ACTION_HIDE_UNLEARNED = "hideUnlearnedText";

    private final TransmutationTerminalHost host;
    private final IActionSource transmutationSource;
    private boolean isTransmuting;

    private final Slot transmuteSlot = new FakeSlot(InternalInventory.empty(), 0);
    private final Slot unlearnSlot = new FakeSlot(InternalInventory.empty(), 0);

    @GuiSync(1)
    public boolean shiftToTransmute;

    @GuiSync(2)
    public int learnedLabelTicks;

    @GuiSync(3)
    public int unlearnedLabelTicks;

    public TransmutationTerminalMenu(int id, Inventory ip, TransmutationTerminalHost host) {
        this(AppliedE.TRANSMUTATION_TERMINAL_MENU.get(), id, ip, host, true);
    }

    public TransmutationTerminalMenu(
            MenuType<?> menuType, int id, Inventory ip, TransmutationTerminalHost host, boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory);
        this.host = host;
        transmutationSource = IActionSource.ofPlayer(getPlayer(), this);

        registerClientAction(ACTION_TOGGLE_SHIFT, host::toggleShiftToTransmute);
        registerClientAction(ACTION_HIDE_LEARNED, () -> learnedLabelTicks--);
        registerClientAction(ACTION_HIDE_UNLEARNED, () -> unlearnedLabelTicks--);
        addSlot(transmuteSlot, TRANSMUTE);
        addSlot(unlearnSlot, UNLEARN);
    }

    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        super.doAction(player, action, slot, id);
        var stack = getCarried();

        if (stack.isEmpty()) {
            return;
        }

        var s = getSlot(slot);

        if (s.equals(transmuteSlot)) {
            var transmuted = (int) storage.insert(
                    AEItemKey.of(stack),
                    action == InventoryAction.SPLIT_OR_PLACE_SINGLE ? 1 : stack.getCount(),
                    Actionable.MODULATE,
                    transmutationSource);
            var reduced = stack.copy();
            reduced.setCount(reduced.getCount() - transmuted);
            setCarried(reduced.getCount() <= 0 ? ItemStack.EMPTY : reduced);
        }

        if (s.equals(unlearnSlot)) {
            var node = host.getActionableNode();

            if (node != null) {
                var provider = node.getGrid().getService(KnowledgeService.class).getProviderFor(player.getUUID());

                if (provider != null && provider.hasKnowledge(stack)) {
                    provider.removeKnowledge(stack);
                    provider.syncKnowledgeChange(player, ItemInfo.fromStack(stack), false);
                    unlearnedLabelTicks = 300;
                    learnedLabelTicks = 0;
                    broadcastChanges();
                }
            }
        }
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        isTransmuting = shiftToTransmute;
        var moved = super.quickMoveStack(player, idx);
        isTransmuting = false;
        return moved;
    }

    @Override
    protected void handleNetworkInteraction(ServerPlayer player, @Nullable AEKey clickedKey, InventoryAction action) {
        if (clickedKey instanceof AEItemKey) {
            isTransmuting = switch (action) {
                case SHIFT_CLICK, ROLL_UP, PICKUP_SINGLE, MOVE_REGION -> notAlreadyStored(clickedKey);
                case PICKUP_OR_SET_DOWN, SPLIT_OR_PLACE_SINGLE -> getCarried().isEmpty()
                        && notAlreadyStored(clickedKey);
                default -> false;};
        } else if (clickedKey instanceof EMCKey) {
            clickedKey = EMCKey.BASE;
        }

        super.handleNetworkInteraction(player, clickedKey, action);
        isTransmuting = false;
    }

    private boolean notAlreadyStored(AEKey key) {
        if (getActionableNode() == null) {
            return false;
        }

        var storage = getActionableNode().getGrid().getStorageService();
        return storage.getCachedInventory().get(key) == 0;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServerSide()) {
            shiftToTransmute = host.getShiftToTransmute();
        }
    }

    @Override
    public boolean mayLearn() {
        return true;
    }

    @Override
    public void onLearn() {
        learnedLabelTicks = 300;
        unlearnedLabelTicks = 0;
        broadcastChanges();
    }

    public void toggleShiftToTransmute() {
        if (isClientSide()) {
            sendClientAction(ACTION_TOGGLE_SHIFT);
            return;
        }

        shiftToTransmute = !shiftToTransmute;
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
    public TransmutationTerminalHost getHost() {
        return host;
    }

    @Override
    public IActionSource getActionSource() {
        return isTransmuting ? transmutationSource : super.getActionSource();
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return host.getActionableNode();
    }
}
