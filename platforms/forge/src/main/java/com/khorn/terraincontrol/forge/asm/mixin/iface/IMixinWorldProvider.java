package com.khorn.terraincontrol.forge.asm.mixin.iface;

import net.minecraft.world.biome.BiomeProvider;

public interface IMixinWorldProvider {

    void setBiomeProvider(BiomeProvider provider);
}
