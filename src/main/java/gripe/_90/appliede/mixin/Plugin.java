package gripe._90.appliede.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import gripe._90.appliede.integration.Addons;

public class Plugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(Addons.AE2WTLIB.getModId())) {
            return Addons.AE2WTLIB.isLoaded();
        }

        if (mixinClassName.contains(Addons.AECAPFIX.getModId())) {
            return Addons.AECAPFIX.isLoaded();
        }

        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
