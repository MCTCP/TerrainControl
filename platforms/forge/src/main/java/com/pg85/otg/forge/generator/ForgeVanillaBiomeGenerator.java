package com.pg85.otg.forge.generator;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.generator.biome.OutputType;
import com.pg85.otg.generator.biome.VanillaBiomeGenerator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

/**
 * A biome generator that gets its information from Mojang's WorldChunkManager.
 *
 * <p>
 * This can be somewhat dangerous, because a subclass for WorldChunkManager,
 * {@link OTGBiomeProvider}, gets its information from a BiomeGenerator. This
 * would cause infinite recursion. To combat this, a check has been added to
 * {@link #setBiomeProvider(BiomeProvider)}.
 *
 */
public class ForgeVanillaBiomeGenerator extends VanillaBiomeGenerator
{

    private Biome[] BiomeArray;
    private BiomeProvider worldChunkManager;

    public ForgeVanillaBiomeGenerator(LocalWorld world)
    {
        super(world);
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        this.BiomeArray = this.worldChunkManager.getBiomesForGeneration(this.BiomeArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = Biome.getIdForBiome(this.BiomeArray[i]);
        return biomeArray;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        this.BiomeArray = this.worldChunkManager.getBiomes(this.BiomeArray, x, z, x_size, z_size, true);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = Biome.getIdForBiome(this.BiomeArray[i]);
        return biomeArray;
    }

    @Override
    public int getBiome(int x, int z)
    {
        return Biome.getIdForBiome(this.worldChunkManager.getBiome(new BlockPos(x, 0, z)));
    }

    @Override
    public void cleanupCache()
    {
        this.worldChunkManager.cleanupCache();
    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        return true;
    }

    /**
     * Sets the vanilla {@link BiomeProvider}. Must be called before generating
     * any biomes.
     *
     * @param biomeProvider The vanilla {@link BiomeProvider}.
     */
    public void setBiomeProvider(BiomeProvider biomeProvider)
    {
        if (biomeProvider instanceof OTGBiomeProvider)
        {
            // TCBiomeProvider is unusable, as it just asks the
            // BiomeGenerator for the biomes, creating an infinite loop
            throw new IllegalArgumentException(getClass() + " expects a vanilla BiomeProvider, " + biomeProvider.getClass() + " given");
        }
        this.worldChunkManager = biomeProvider;
    }

}
