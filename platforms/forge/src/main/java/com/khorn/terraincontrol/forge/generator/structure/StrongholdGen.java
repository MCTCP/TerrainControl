package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureStrongholdPieces;

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

    public StrongholdGen(WorldSettings configs)
    {
        this.distance = configs.worldConfig.strongholdDistance;
        this.structureCoords = new ChunkCoordIntPair[configs.worldConfig.strongholdCount];
        this.spread = configs.worldConfig.strongholdSpread;

        allowedBiomeGenBases = new ArrayList<BiomeGenBase>();

        for (LocalBiome biome : configs.biomes)
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().strongholdsEnabled)
            {
                allowedBiomeGenBases.add(((ForgeBiome) biome).getHandle());
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
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
                double var8 = (1.25D * var6 + random.nextDouble()) * this.distance * var6;
                int var10 = (int) Math.round(Math.cos(randomNumBetween0and2PI) * var8);
                int var11 = (int) Math.round(Math.sin(randomNumBetween0and2PI) * var8);
                ArrayList var12 = new ArrayList();
                Collections.addAll(var12, this.allowedBiomeGenBases);
                BlockPos var13 = this.worldObj.getBiomeProvider().findBiomePosition((var10 << 4) + 8, (var11 << 4) + 8, 112, var12,
                        random);

                if (var13 != null)
                {
                    var10 = var13.getX() >> 4;
                    var11 = var13.getZ() >> 4;
                }

                this.structureCoords[i] = new ChunkCoordIntPair(var10, var11);
                randomNumBetween0and2PI += (Math.PI * 2D) * var6 / this.spread;

                if (i == this.spread)
                {
                    var6 += 2 + random.nextInt(5);
                    this.spread += 1 + random.nextInt(2);
                }
            }

            this.ranBiomeCheck = true;
        }

        ChunkCoordIntPair[] structureCoordsLocal = this.structureCoords;

        for (ChunkCoordIntPair structureCoord : structureCoordsLocal)
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
    @Override
    protected List<BlockPos> getCoordList()
    {       
        List<BlockPos> chunkPositions = new ArrayList<BlockPos>();

        for (ChunkCoordIntPair structureCoord : structureCoords)
        {
            if (structureCoord != null)
            {
                chunkPositions.add(structureCoord.getCenterBlock(64));
            }
        }

        return chunkPositions;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2)
    {
        MapGenStronghold.Start start = new MapGenStronghold.Start(this.worldObj, this.rand, par1, par2);

        while (start.getComponents().isEmpty() || ((StructureStrongholdPieces.Stairs2) start.getComponents().get(0)).strongholdPortalRoom == null)
        {
            start = new MapGenStronghold.Start(this.worldObj, this.rand, par1, par2);
        }

        return start;
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.STRONGHOLD;
    }
}
