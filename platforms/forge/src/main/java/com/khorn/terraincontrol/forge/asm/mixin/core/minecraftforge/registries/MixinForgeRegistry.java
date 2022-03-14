package com.khorn.terraincontrol.forge.asm.mixin.core.minecraftforge.registries;

import com.google.common.collect.BiMap;
import com.khorn.terraincontrol.forge.asm.mixin.iface.IMixinForgeRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.Map;

@Mixin(value = ForgeRegistry.class, remap = false)
public abstract class MixinForgeRegistry implements IMixinForgeRegistry {

    @Shadow @Final private BiMap<Integer, ?> ids;
    @Shadow @Final private BiMap<ResourceLocation, ?> names;
    @Shadow @Final private BitSet availabilityMap;
    @Shadow private int max;

    @Inject(method = "loadIds", at = @At("HEAD"))
    private void onLoadIds(Map<ResourceLocation, Integer> ids, Map<ResourceLocation, String> overrides, Map<ResourceLocation, Integer> missing,
            Map<ResourceLocation, Integer[]> remapped, ForgeRegistry<?> old, ResourceLocation name, CallbackInfo ci) {
        ids.keySet().removeIf(next -> next.getResourceDomain().equalsIgnoreCase("terraincontrol"));
    }

    @Override
    public BitSet getAvailabilityMap() {
        return this.availabilityMap;
    }

    @Override
    public Map getIds() {
        return this.ids;
    }

    @Override
    public Map getNames() {
        return this.names;
    }

    @Override
    public void setMax(int max) {
        this.max = max;
    }
}
