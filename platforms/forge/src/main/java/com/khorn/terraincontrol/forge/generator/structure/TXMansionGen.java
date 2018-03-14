package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.WoodlandMansion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TXMansionGen extends MapGenStructure
{
    private final int maxDistance;
    private final int minDistance;
    private int debugrun = 0;
    private int startX = 0;
    private int startZ = 0;
    /**
     * Roofed forest and mutated roofed forest
     */
    private final List<Biome> biomeList;
    private ChunkGeneratorOverworld defaultTerrainGeneratorOrNull;

    public TXMansionGen(ServerConfigProvider configs)
    {
        biomeList = new ArrayList<Biome>();

        for (LocalBiome biome : configs.getBiomeArray())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().mansionsEnabled)
            {
                biomeList.add(((ForgeBiome) biome).getHandle());
            }
        }

        this.maxDistance = configs.getWorldConfig().maximumDistanceBetweenMansions;
        this.minDistance = configs.getWorldConfig().minimumDistanceBetweenMansions;
    }

    private ChunkGeneratorOverworld getDefaultTerrainGenerator(World world)
    {
        if (this.defaultTerrainGeneratorOrNull == null)
        {
            this.defaultTerrainGeneratorOrNull = new ChunkGeneratorOverworld(world, world.getSeed(), false, "")
            {

            };
        }
        return this.defaultTerrainGeneratorOrNull;
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.MANSION;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int i = chunkX;
        int j = chunkZ;
        if (chunkX < 0)
        {
            i = chunkX - (maxDistance - 1);
        }

        if (chunkZ < 0)
        {
            j = chunkZ - (maxDistance - 1);
        }

        int k = i / maxDistance;
        int l = j / maxDistance;
        Random random = this.world.setRandomSeed(k, l, 10387319);
        k *= maxDistance;
        l *= maxDistance;
        k += (random.nextInt(maxDistance - minDistance) + random.nextInt(maxDistance - minDistance)) / 2;
        l += (random.nextInt(maxDistance - minDistance) + random.nextInt(maxDistance - minDistance)) / 2;
        if (chunkX == k && chunkZ == l)
        {
            //System.out.println("Checking for Mansion Placement at: " + (chunkX * 16 + 8) + ", y: unknown , z: " + (chunkZ * 16 + 8));

            if (debugrun == 0) {
                for(int zz = 0; zz < biomeList.size(); zz++) {
                    //System.out.println(biomeList.get(zz).biomeName);
                }
                debugrun++;
            }

            boolean flag = this.world.getBiomeProvider().areBiomesViable(chunkX * 16 + 8, chunkZ * 16 + 8, 32, biomeList);
            if (flag)
            {
                //System.out.println("Mansion spawned at: x:" + (chunkX * 16 + 8) + ", y: unknown , z: " + (chunkZ * 16 + 8));
                return true;
            }
            if (chunkX != startX || chunkZ != startZ) {
                //System.out.println("Failed to spawn Mansion at: x:" + (chunkX * 16 + 8) + ", y: unknown , z: " + (chunkZ * 16 + 8));
                startX = chunkX;
                startZ = chunkZ;
            }
        }

        return false;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
    {
        this.world = worldIn;
        BiomeProvider biomeprovider = worldIn.getBiomeProvider();
        return biomeprovider.isFixedBiome() && biomeprovider.getFixedBiome() != Biomes.ROOFED_FOREST ? null : findNearestStructurePosBySpacing(worldIn, this, pos, maxDistance, minDistance, 10387319, true, 100, findUnexplored);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new WoodlandMansion.Start(this.world, getDefaultTerrainGenerator(this.world), this.rand, chunkX, chunkZ);
    }

}
