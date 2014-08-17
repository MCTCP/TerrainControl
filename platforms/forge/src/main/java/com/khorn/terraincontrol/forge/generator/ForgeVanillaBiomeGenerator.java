package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.biome.BiomeCache;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

/**
 * A biome generator that gets its information from Mojang's WorldChunkManager.
 *
 * <p>
 * This can be somewhat dangerous, because a subclass for WorldChunkManager,
 * {@link TCWorldChunkManager}, gets its information from a BiomeGenerator. This
 * would cause infinite recursion. To combat this, a check has been added to
 * {@link #getWorldChunkManager()}.
 *
 */
public class ForgeVanillaBiomeGenerator extends VanillaBiomeGenerator {

    private BiomeGenBase[] biomeGenBaseArray;
    private WorldChunkManager worldChunkManager;

    public ForgeVanillaBiomeGenerator(LocalWorld world, BiomeCache cache) {
        super(world, cache);
    }

    private WorldChunkManager getWorldChunkManager()
    {
        if (worldChunkManager != null)
        {
            return worldChunkManager;
        }

        worldChunkManager = ((ForgeWorld) world).getWorld().getWorldChunkManager();
        if (worldChunkManager instanceof TCWorldChunkManager)
        {
            // Sanity check
            throw new AssertionError(getClass().getName() + " expects a vanilla WorldChunkManager, " + worldChunkManager.getClass()
                    + " given");
        }
        return worldChunkManager;
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        biomeGenBaseArray = getWorldChunkManager().getBiomesForGeneration(biomeGenBaseArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeGenBaseArray[i].biomeID;
        return biomeArray;
    }

    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size)
    {
        return getWorldChunkManager().getRainfall(paramArrayOfFloat, x, z, x_size, z_size);
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        biomeGenBaseArray = getWorldChunkManager().getBiomeGenAt(biomeGenBaseArray, x, z, x_size, z_size, true);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeGenBaseArray[i].biomeID;
        return biomeArray;
    }

    @Override
    public int getBiome(int x, int z)
    {
        return getWorldChunkManager().getBiomeGenAt(x, z).biomeID;
    }

    @Override
    public void cleanupCache()
    {
        getWorldChunkManager().cleanupCache();
    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        return true;
    }

}
