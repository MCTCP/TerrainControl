package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.WorldGenRegistration.WorldGenJungleTemple;
import net.minecraft.server.v1_12_R1.WorldGenRegistration.WorldGenPyramidPiece;
import net.minecraft.server.v1_12_R1.WorldGenRegistration.WorldGenWitchHut;
import net.minecraft.server.v1_12_R1.WorldGenRegistration.b;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TXRareBuildingGen extends StructureGenerator
{
    private List<BiomeBase> biomeList;
    private List<BiomeBase.BiomeMeta> mobList = Arrays.asList(new BiomeBase.BiomeMeta(EntityWitch.class, 1, 1, 1));

    /**
     * the maximum distance between scattered features
     */
    private int maxDistanceBetweenScatteredFeatures;

    /**
     * the minimum distance between scattered features
     */
    private int minDistanceBetweenScatteredFeatures;

    public TXRareBuildingGen(ServerConfigProvider configs)
    {
        biomeList = new ArrayList<BiomeBase>();

        for (LocalBiome biome : configs.getBiomeArray())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().rareBuildingType != RareBuildingType.disabled)
            {
                biomeList.add(((BukkitBiome) biome).getHandle());
            }
        }

        this.maxDistanceBetweenScatteredFeatures = configs.getWorldConfig().maximumDistanceBetweenRareBuildings;
        // Minecraft's internal minimum distance is one chunk lower than TC's
        // value
        this.minDistanceBetweenScatteredFeatures = configs.getWorldConfig().minimumDistanceBetweenRareBuildings - 1;
    }

    public boolean isWitchHutAt(BlockPosition blockposition) {
        StructureStart structurestart = this.c(blockposition);
        if(structurestart != null && structurestart instanceof RareBuildingStart && !structurestart.c().isEmpty()) {
            StructurePiece structurepiece = (StructurePiece)structurestart.c().get(0);
            return structurepiece instanceof WorldGenWitchHut;
        } else {
            return false;
        }
    }

    public List<BiomeBase.BiomeMeta> getWitchHutMobs()
    {
        return this.mobList;
    }

    public BlockPosition getNearestGeneratedFeature(World world, BlockPosition blockposition, boolean flag)
    {
        this.g = world;
        return a(world, this, blockposition, this.maxDistanceBetweenScatteredFeatures, this.minDistanceBetweenScatteredFeatures - 1, 14357617, false, 100, flag);
    }

    @Override
    // canSpawnStructureAtChunkCoords
    protected boolean a(int chunkX, int chunkZ)
    {
        int var3 = chunkX;
        int var4 = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        int var5 = chunkX / this.maxDistanceBetweenScatteredFeatures;
        int var6 = chunkZ / this.maxDistanceBetweenScatteredFeatures;
        Random random = this.g.a(var5, var6, 14357617);
        var5 *= this.maxDistanceBetweenScatteredFeatures;
        var6 *= this.maxDistanceBetweenScatteredFeatures;
        var5 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures + 1);
        var6 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures + 1);

        if (var3 == var5 && var4 == var6)
        {
            BiomeBase biomeAtPosition = this.g.getWorldChunkManager().getBiome(new BlockPosition(var3 * 16 + 8, 0, var4 * 16 + 8));

            for (BiomeBase biome : biomeList)
            {
                if (biomeAtPosition.equals(biome))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    // getStructureStart
    protected StructureStart b(int chunkX, int chunkZ)
    {
        return new RareBuildingStart(this.g, this.f, chunkX, chunkZ);
    }

    @Override
    public String a()
    {
        return StructureNames.RARE_BUILDING;
    }

    public static class RareBuildingStart extends StructureStart
    {
        RareBuildingStart(World world, Random random, int chunkX, int chunkZ)
        {
            LocalWorld localWorld = WorldHelper.toLocalWorld(world);
            BiomeConfig biomeConfig = localWorld.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8).getBiomeConfig();
            StructurePiece building;
            switch (biomeConfig.rareBuildingType)
            {
                case desertPyramid:
                    building = new WorldGenPyramidPiece(random, chunkX * 16, chunkZ * 16);
                    break;
                case jungleTemple:
                    building = new WorldGenJungleTemple(random, chunkX * 16, chunkZ * 16);
                    break;
                case swampHut:
                    building = new WorldGenWitchHut(random, chunkX * 16, chunkZ * 16);
                    break;
                case igloo:
                    building = new b(random, chunkX * 16, chunkZ * 16);
                    break;
                case disabled:
                default:
                    // Should never happen, but on biome borders there is chance
                    // that a
                    // structure gets started in a biome where it shouldn't.
                    building = null;
                    break;
            }

            if (building != null)
            {
                // Add building to components
                this.a.add(building);
            }

            // Update boundingbox
            this.d();
        }

        public RareBuildingStart()
        {
            // Required by Minecraft's structure loading code
        }
    }
}
