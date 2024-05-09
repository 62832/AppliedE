package gripe._90.appliede.menu;

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

import gripe._90.appliede.me.misc.ITransmutationTerminalHost;
import gripe._90.appliede.me.service.KnowledgeService;

public class TransmutationTerminalMenu extends MEStorageMenu {
    public static final MenuType<TransmutationTerminalMenu> TYPE = MenuTypeBuilder.create(
                    TransmutationTerminalMenu::new, ITransmutationTerminalHost.class)
            .build("transmutation_terminal");
    protected static final SlotSemantic TRANSMUTE = SlotSemantics.register("APPLIEDE_TRANSMUTE", false);

    private static final String ACTION_SET_SHIFT = "setShiftDestination";
    private static final String ACTION_HIDE_LEARNED = "hideLearnedText";

    private final ITransmutationTerminalHost host;
    private final Slot transmuteSlot = new FakeSlot(InternalInventory.empty(), 0);

    @GuiSync(1)
    public boolean shiftToTransmute;

    @GuiSync(2)
    public int learnedLabelTicks;

    public TransmutationTerminalMenu(int id, Inventory ip, ITransmutationTerminalHost host) {
        this(TYPE, id, ip, host, true);
    }

    public TransmutationTerminalMenu(
            MenuType<?> menuType, int id, Inventory ip, ITransmutationTerminalHost host, boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory);
        this.host = host;
        registerClientAction(ACTION_SET_SHIFT, Boolean.class, host::setShiftToTransmute);
        registerClientAction(ACTION_HIDE_LEARNED, () -> learnedLabelTicks--);
        addSlot(transmuteSlot, TRANSMUTE);
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
    }

    private int transmuteItem(ItemStack stack, boolean singleItem, ServerPlayer player) {
        if (!stack.isEmpty()) {
            var grid = host.getGrid();

            if (grid == null) {
                return 0;
            }

            var knowledge = grid.getService(KnowledgeService.class);

            if (knowledge != null) {
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
                                this::showLearned);
            }
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
        broadcastChanges();
    }

    public void decrementLearnedTicks() {
        if (isClientSide()) {
            sendClientAction(ACTION_HIDE_LEARNED);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        if (player instanceof ServerPlayer serverPlayer && shiftToTransmute) {
            var stack = slots.get(idx).getItem();

            var remaining = stack.copy();
            remaining.setCount(remaining.getCount() - transmuteItem(stack, false, serverPlayer));
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
    public ITransmutationTerminalHost getHost() {
        return host;
    }
}
