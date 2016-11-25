package com.khorn.terraincontrol.forge.generator.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureStrongholdPieces;

public class TXStrongholdGen extends MapGenStructure
{
    private List<Biome> allowedBiomes;

    private boolean ranBiomeCheck;
    private ChunkPos[] structureCoords;
    private double distance;
    private int spread;

    public TXStrongholdGen(ServerConfigProvider configs)
    {
        this.distance = configs.getWorldConfig().strongholdDistance;
        this.structureCoords = new ChunkPos[configs.getWorldConfig().strongholdCount];
        this.spread = configs.getWorldConfig().strongholdSpread;

        allowedBiomes = new ArrayList<Biome>();

        for (LocalBiome biome : configs.getBiomeArray())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().strongholdsEnabled)
            {
                allowedBiomes.add(((ForgeBiome) biome).getHandle());
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
                Collections.addAll(var12, this.allowedBiomes);
                BlockPos var13 = this.worldObj.getBiomeProvider().findBiomePosition((var10 << 4) + 8, (var11 << 4) + 8, 112, var12,
                        random);

                if (var13 != null)
                {
                    var10 = var13.getX() >> 4;
                    var11 = var13.getZ() >> 4;
                }

                this.structureCoords[i] = new ChunkPos(var10, var11);
                randomNumBetween0and2PI += (Math.PI * 2D) * var6 / this.spread;

                if (i == this.spread)
                {
                    var6 += 2 + random.nextInt(5);
                    this.spread += 1 + random.nextInt(2);
                }
            }

            this.ranBiomeCheck = true;
        }

        ChunkPos[] structureCoordsLocal = this.structureCoords;

        for (ChunkPos structureCoord : structureCoordsLocal)
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

        for (ChunkPos structureCoord : structureCoords)
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
