package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.Biome;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.ComponentStrongholdStairs2;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StrongholdGen extends MapGenStructure
{
    private List<BiomeGenBase> allowedBiomeGenBases;

    private boolean ranBiomeCheck;
    private ChunkCoordIntPair[] structureCoords;
    private double distance;
    private int spread;

    public StrongholdGen(WorldConfig worldConfig)
    {
        this.distance = worldConfig.strongholdDistance;
        this.structureCoords = new ChunkCoordIntPair[worldConfig.strongholdCount];
        this.spread = worldConfig.strongholdSpread;

        allowedBiomeGenBases = new ArrayList<BiomeGenBase>();

        for (BiomeConfig biomeConfig : worldConfig.biomeConfigs)
        {
            if (biomeConfig == null)
                continue;
            if (biomeConfig.strongholdsEnabled)
            {
                allowedBiomeGenBases.add(((Biome) biomeConfig.Biome).getHandle());
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected boolean canSpawnStructureAtCoords(int par1, int par2)
    {
        if (!this.ranBiomeCheck)
        {
            Random random = new Random();
            random.setSeed(this.worldObj.getSeed());
            double randomNumBetween0and2PI = random.nextDouble() * Math.PI * 2.0D;
            int var6 = 1;

            for (int i = 0; i < this.structureCoords.length; ++i)
            {
                double var8 = (1.25D * (double) var6 + random.nextDouble()) * this.distance * (double) var6;
                int var10 = (int) Math.round(Math.cos(randomNumBetween0and2PI) * var8);
                int var11 = (int) Math.round(Math.sin(randomNumBetween0and2PI) * var8);
                ArrayList var12 = new ArrayList();
                Collections.addAll(var12, this.allowedBiomeGenBases);
                ChunkPosition var13 = this.worldObj.getWorldChunkManager().findBiomePosition((var10 << 4) + 8, (var11 << 4) + 8, 112, var12, random);

                if (var13 != null)
                {
                    var10 = var13.x >> 4;
                    var11 = var13.z >> 4;
                }

                this.structureCoords[i] = new ChunkCoordIntPair(var10, var11);
                randomNumBetween0and2PI += (Math.PI * 2D) * (double) var6 / (double) this.spread;

                if (i == this.spread)
                {
                    var6 += 2 + random.nextInt(5);
                    this.spread += 1 + random.nextInt(2);
                }
            }

            this.ranBiomeCheck = true;
        }

        ChunkCoordIntPair[] structureCoords = this.structureCoords;

        for (ChunkCoordIntPair structureCoord : structureCoords)
        {
            if (par1 == structureCoord.chunkXPos && par2 == structureCoord.chunkZPos)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a list of other locations at which the structure generation has
     * been run, or null if not relevant to this structure generator.
     */
    protected List<ChunkPosition> getCoordList()
    {
        ArrayList<ChunkPosition> chunkPositions = new ArrayList<ChunkPosition>();

        for (ChunkCoordIntPair structureCoord : structureCoords)
        {
            if (structureCoord != null)
            {
                chunkPositions.add(structureCoord.getChunkPosition(64));
            }
        }

        return chunkPositions;
    }

    protected StructureStart getStructureStart(int par1, int par2)
    {
        StrongholdStart start = new StrongholdStart(this.worldObj, this.rand, par1, par2);

        while (start.getComponents().isEmpty() || ((ComponentStrongholdStairs2) start.getComponents().get(0)).strongholdPortalRoom == null)
        {
            start = new StrongholdStart(this.worldObj, this.rand, par1, par2);
        }

        return start;
    }
}
