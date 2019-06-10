package com.pg85.otg.bukkit.generator.structures;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.bukkit.BukkitBiome;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.minecraftTypes.StructureNames;

import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.StructureGenerator;
import net.minecraft.server.v1_12_R1.StructureStart;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldGenMonument.WorldGenMonumentStart;
import net.minecraft.server.v1_12_R1.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * The ocean monument generator.
 *
 * <p>Minecraft defines two parameters: spacing and separation. We use two
 * more descriptive parameters: gridSize and randomOffset. They are directly
 * related: <code>GridSize = spacing</code> and <code>spacing - separation =
 * randomOffset + 1</code>, in other words, <code>randomOffset = spacing -
 * separation - 1</code>
 *
 */
public class OTGOceanMonumentGen extends StructureGenerator
{
	
    private final List<BiomeBase> monumentSpawnBiomes;
    private final List<BiomeBase.BiomeMeta> mobList = Arrays.asList(new BiomeBase.BiomeMeta(EntityGuardian.class, 1, 2, 4));

    private int randomOffset;
    private int gridSize;

    public OTGOceanMonumentGen(ConfigProvider settings)
    {
        this.gridSize = settings.getWorldConfig().oceanMonumentGridSize;
        this.randomOffset = settings.getWorldConfig().oceanMonumentRandomOffset;
        this.monumentSpawnBiomes = new ArrayList<BiomeBase>();

        for (LocalBiome biome : settings.getBiomeArrayByOTGId())
        {
            if (biome == null || !biome.getBiomeConfig().oceanMonumentsEnabled)
            {
                continue;
            }

            monumentSpawnBiomes.add(((BukkitBiome) biome).getHandle());
        }
    }

    public BlockPosition getNearestGeneratedFeature(World var1, BlockPosition var2, boolean var3)
    {
        this.g = var1;
        return a(var1, this, var2, this.gridSize, this.gridSize - this.randomOffset - 1, 10387313, true, 100, var3);
    }
    
    public List<BiomeBase.BiomeMeta> getMobs()
    {
    	return mobList;
    }
    
    @Override
    public String a()
    {
        return StructureNames.OCEAN_MONUMENT;
    }

    @Override
    // canSpawnStructureAtChunkCoords
    protected boolean a(int chunkX, int chunkZ)
    {
        int originalChunkX = chunkX;
        int originalChunkZ = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.gridSize - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.gridSize - 1;
        }

        int structureChunkX = chunkX / this.gridSize;
        int structureChunkZ = chunkZ / this.gridSize;
        Random random = this.g.a(structureChunkX, structureChunkZ, 10387313);

        structureChunkX *= this.gridSize;
        structureChunkZ *= this.gridSize;
        // Adding one to the randomOffset ensures that randomOffset = 0
        // disables randomness instead of 1, as one would expect
        structureChunkX += (random.nextInt(this.randomOffset + 1) + random.nextInt(this.randomOffset + 1)) / 2;
        structureChunkZ += (random.nextInt(this.randomOffset + 1) + random.nextInt(this.randomOffset + 1)) / 2;
        if (originalChunkX == structureChunkX && originalChunkZ == structureChunkZ)
        {
            boolean flag = this.g.getWorldChunkManager().a(originalChunkX * 16 + 8, originalChunkZ * 16 + 8, 29, monumentSpawnBiomes);

            if (flag)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart b(int i, int j)
    {
        return new WorldGenMonumentStart(this.g, this.f, i, j);
    }

}
