package gripe._90.appliede.me.misc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;

import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;

@SuppressWarnings("UnstableApiUsage")
public class EMCContainerItemStrategy implements ContainerItemStrategy<EMCKey, ItemStack> {
    public static final EMCContainerItemStrategy INSTANCE = new EMCContainerItemStrategy();

    private EMCContainerItemStrategy() {}

    @Nullable
    @Override
    public GenericStack getContainedStack(ItemStack stack) {
        return stack.isEmpty()
                ? null
                : stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY)
                        .map(handler -> new GenericStack(EMCKey.BASE, handler.getStoredEmc(stack)))
                        .orElse(null);
    }

    @Nullable
    @Override
    public ItemStack findCarriedContext(Player player, AbstractContainerMenu menu) {
        if (!(menu instanceof TransmutationTerminalMenu)) {
            return null;
        }

        var carried = menu.getCarried();
        return carried.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).isPresent() ? carried : null;
    }

    @Override
    public long extract(ItemStack context, EMCKey what, long amount, Actionable mode) {
        var action = mode.isSimulate() ? IEmcStorage.EmcAction.SIMULATE : IEmcStorage.EmcAction.EXECUTE;
        return context.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY)
                .map(handler -> handler.extractEmc(context, amount, action))
                .orElse(0L);
    }

    @Override
    public long insert(ItemStack context, EMCKey what, long amount, Actionable mode) {
        var action = mode.isSimulate() ? IEmcStorage.EmcAction.SIMULATE : IEmcStorage.EmcAction.EXECUTE;
        return context.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY)
                .map(handler -> handler.insertEmc(context, amount, action))
                .orElse(0L);
    }

    @Override
    public void playFillSound(Player player, EMCKey what) {
        player.playNotifySound(PESoundEvents.CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void playEmptySound(Player player, EMCKey what) {
        player.playNotifySound(PESoundEvents.UNCHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Nullable
    @Override
    public GenericStack getExtractableContent(ItemStack context) {
        return getContainedStack(context);
    }
}
