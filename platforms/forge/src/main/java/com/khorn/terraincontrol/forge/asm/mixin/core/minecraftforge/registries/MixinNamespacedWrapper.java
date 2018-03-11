package com.khorn.terraincontrol.forge.asm.mixin.core.minecraftforge.registries;

import com.khorn.terraincontrol.forge.asm.mixin.iface.IMixinNamespacedWrapper;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraftforge.registries.NamespacedWrapper", remap = false)
public abstract class MixinNamespacedWrapper implements IMixinNamespacedWrapper {

    @Shadow private ForgeRegistry<?> delegate;

    @Override
    public <T extends IForgeRegistry<?>> T getDelegate() {
        return (T) this.delegate;
    }
}
