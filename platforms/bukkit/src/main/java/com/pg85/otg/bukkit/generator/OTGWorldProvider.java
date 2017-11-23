package com.pg85.otg.bukkit.generator;

import com.pg85.otg.bukkit.BukkitWorld;
import net.minecraft.server.v1_12_R1.WorldProvider;
import net.minecraft.server.v1_12_R1.WorldProviderNormal;

/**
 * We extend this file to be able to set the sea level.
 * In Minecraft this is used in a few places such as spawning algorithms for villages.
 * The value seem to be hardcoded in CraftWorld and we are a bit unsure about if that matters.
 * At least it should be a good thing that we set the value here.
 */
public class OTGWorldProvider extends WorldProviderNormal
{
    protected BukkitWorld localWorld;
    private final WorldProvider oldWorldProvider;

    public OTGWorldProvider(BukkitWorld localWorld, WorldProvider oldWorldProvider)
    {
        this.localWorld = localWorld;
        this.oldWorldProvider = oldWorldProvider;
        this.a(localWorld.getWorld());
        this.d = oldWorldProvider.l(); // doesWaterVaporize
        this.f = oldWorldProvider.m(); // hasSkyLight
        this.e = oldWorldProvider.n(); // hasNoSky (see https://github.com/ModCoderPack/MCPBot-Issues/issues/330)
    }

    @Override
    public int getSeaLevel()
    {
        return localWorld.getConfigs().getWorldConfig().waterLevelMax;
    }

    /**
     * Returns the world provider that was replaced by the current world provider.
     * When the plugin disables, this needs to be restored.
     * 
     * @return The old world provider.
     */
    public WorldProvider getOldWorldProvider()
    {
        return oldWorldProvider;
    }
}