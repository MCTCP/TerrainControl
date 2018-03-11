package com.khorn.terraincontrol.forge.asm.mixin.iface;

import net.minecraftforge.registries.IForgeRegistry;

public interface IMixinNamespacedWrapper {

    <T extends IForgeRegistry<?>> T getDelegate();
}
