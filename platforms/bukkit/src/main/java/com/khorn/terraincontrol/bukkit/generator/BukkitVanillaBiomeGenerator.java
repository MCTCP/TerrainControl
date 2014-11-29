package com.khorn.terraincontrol.bukkit.generator;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import net.minecraft.server.v1_8_R1.BiomeBase;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.WorldChunkManager;

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

    public void setWorldChunkManager(WorldChunkManager worldChunkManager)
    {
        if (worldChunkManager instanceof TCWorldChunkManager)
        {
            // TCWorldChunkManager is unusable, as it just asks the
            // BiomeGenerator for the biomes, creating an infinite loop
            throw new IllegalArgumentException(getClass()
                    + " expects a vanilla WorldChunkManager, "
                    + worldChunkManager.getClass() + " given");
        }
        this.worldChunkManager = worldChunkManager;
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType outputType)
    {
        biomeGenBaseArray = worldChunkManager.getBiomes(biomeGenBaseArray, x, z, xSize, zSize);
        if (biomeArray == null || biomeArray.length < xSize * zSize)
            biomeArray = new int[xSize * zSize];
        for (int i = 0; i < xSize * zSize; i++)
            biomeArray[i] = biomeGenBaseArray[i].id;
        return biomeArray;
    }

    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size)
    {
        return worldChunkManager.getWetness(paramArrayOfFloat, x, z, x_size, z_size);
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int xSize, int z_size, OutputType outputType)
    {
        biomeGenBaseArray = worldChunkManager.a(biomeGenBaseArray, x, z, xSize, z_size, true);
        if (biomeArray == null || biomeArray.length < xSize * z_size)
            biomeArray = new int[xSize * z_size];
        for (int i = 0; i < xSize * z_size; i++)
            biomeArray[i] = biomeGenBaseArray[i].id;
        return biomeArray;
    }

    @Override
    public int getBiome(int x, int z)
    {
        return worldChunkManager.getBiome(new BlockPosition(x, 0, z)).id;
    }

    @Override
    public void cleanupCache()
    {
        worldChunkManager.b();
    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        return true;
    }

}
