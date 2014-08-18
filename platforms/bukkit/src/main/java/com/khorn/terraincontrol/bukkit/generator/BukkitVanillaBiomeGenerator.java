package com.khorn.terraincontrol.bukkit.generator;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import net.minecraft.server.v1_7_R4.BiomeBase;
import net.minecraft.server.v1_7_R4.WorldChunkManager;

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
public class BukkitVanillaBiomeGenerator extends VanillaBiomeGenerator {

    private BiomeBase[] biomeGenBaseArray;
    private WorldChunkManager worldChunkManager;

    public BukkitVanillaBiomeGenerator(LocalWorld world) {
        super(world);
    }

    private WorldChunkManager getWorldChunkManager()
    {
        if (worldChunkManager != null)
        {
            return worldChunkManager;
        }

        worldChunkManager = ((BukkitWorld) world).getWorld().getWorldChunkManager();
        if (worldChunkManager instanceof TCWorldChunkManager)
        {
            // Sanity check
            throw new AssertionError(getClass().getName() + " expects a vanilla WorldChunkManager, " + worldChunkManager.getClass() + " given");
        }
        return worldChunkManager;
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType outputType)
    {
        biomeGenBaseArray = getWorldChunkManager().getBiomes(biomeGenBaseArray, x, z, xSize, zSize);
        if (biomeArray == null || biomeArray.length < xSize * zSize)
            biomeArray = new int[xSize * zSize];
        for (int i = 0; i < xSize * zSize; i++)
            biomeArray[i] = biomeGenBaseArray[i].id;
        return biomeArray;
    }

    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size)
    {
        return getWorldChunkManager().getWetness(paramArrayOfFloat, x, z, x_size, z_size);
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int xSize, int z_size, OutputType outputType)
    {
        biomeGenBaseArray = getWorldChunkManager().a(biomeGenBaseArray, x, z, xSize, z_size, true);
        if (biomeArray == null || biomeArray.length < xSize * z_size)
            biomeArray = new int[xSize * z_size];
        for (int i = 0; i < xSize * z_size; i++)
            biomeArray[i] = biomeGenBaseArray[i].id;
        return biomeArray;
    }

    @Override
    public int getBiome(int x, int z)
    {
        return getWorldChunkManager().getBiome(x, z).id;
    }

    @Override
    public void cleanupCache()
    {
        getWorldChunkManager().b();
    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        return true;
    }

}
