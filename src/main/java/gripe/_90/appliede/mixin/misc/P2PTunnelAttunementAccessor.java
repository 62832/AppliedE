package gripe._90.appliede.mixin.misc;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import appeng.api.features.P2PTunnelAttunement;

@Mixin(P2PTunnelAttunement.class)
public interface P2PTunnelAttunementAccessor {
    @Accessor
    static Map<TagKey<Item>, Item> getTagTunnels() {
        throw new AssertionError();
    }
}
