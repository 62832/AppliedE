package gripe._90.appliede.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.blockentity.grid.AENetworkedBlockEntity;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.misc.EMCInterfaceLogic;
import gripe._90.appliede.me.misc.EMCInterfaceLogicHost;

public class EMCInterfaceBlockEntity extends AENetworkedBlockEntity implements EMCInterfaceLogicHost {
    private final EMCInterfaceLogic logic = createLogic();

    public EMCInterfaceBlockEntity(BlockPos pos, BlockState state) {
        this(AppliedE.EMC_INTERFACE_BE.get(), pos, state);
    }

    public EMCInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected EMCInterfaceLogic createLogic() {
        return new EMCInterfaceLogic(getMainNode(), this, getItemFromBlockEntity());
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public EMCInterfaceLogic getInterfaceLogic() {
        return logic;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (getMainNode().hasGridBooted()) {
            logic.notifyNeighbours();
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        logic.writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        logic.readFromNBT(data, registries);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        logic.addDrops(drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        logic.clearContent();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return AppliedE.EMC_INTERFACE.asItem();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AppliedE.EMC_INTERFACE.asItem().getDefaultInstance();
    }
}
