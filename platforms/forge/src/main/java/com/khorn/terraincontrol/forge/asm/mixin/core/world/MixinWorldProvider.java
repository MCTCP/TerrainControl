package com.khorn.terraincontrol.forge.asm.mixin.core.world;

import com.khorn.terraincontrol.forge.TXPlugin;
import com.khorn.terraincontrol.forge.asm.mixin.iface.IMixinWorldProvider;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider implements IMixinWorldProvider {

    @Shadow protected BiomeProvider biomeProvider;

    @Inject(method = "setWorld", at = @At("RETURN"))
    private void onSetWorld(World world, CallbackInfo ci) {
        TXPlugin.instance.worldLoader.initializeTCWorld(world);
    }

    @Override
    public void setBiomeProvider(BiomeProvider provider) {
        this.biomeProvider = provider;
    }
}
