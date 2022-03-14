package com.khorn.terraincontrol.forge.asm.mixin.iface;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.BitSet;
import java.util.Map;

public interface IMixinForgeRegistry<T extends IForgeRegistryEntry<T>> {

    BitSet getAvailabilityMap();

    <V extends IForgeRegistryEntry<V>> Map<Integer, V> getIds();

    <V extends IForgeRegistryEntry<V>> Map<ResourceLocation, V> getNames();

    void setMax(int max);
}
