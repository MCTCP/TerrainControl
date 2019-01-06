package com.pg85.otg.forge.generator.structure;

import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.network.ServerConfigProvider;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureStart;

public class OTGStrongholdGen extends MapGenStronghold
{
    public final List<Biome> allowedBiomes;
    /** is spawned false and set true once the defined BiomeGenBases were compared with the present ones */
    private boolean ranBiomeCheck;
    private ChunkPos[] structureCoords;
    private double distance;
    private int spread;

    public OTGStrongholdGen(ServerConfigProvider configs, WorldServer world)
    {
    	this.world = world;
    	this.structureCoords = new ChunkPos[128];
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

        this.distance = MathHelper.getDouble(String.valueOf(configs.getWorldConfig().strongholdDistance), this.distance, 1.0D);
        this.structureCoords = new ChunkPos[MathHelper.getInt(String.valueOf(configs.getWorldConfig().strongholdCount), this.structureCoords.length, 1)];
        this.spread = MathHelper.getInt(String.valueOf(configs.getWorldConfig().strongholdSpread), this.spread, 1);
    }

    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
    {
        if (!this.ranBiomeCheck)
        {
            this.generatePositions();
            this.ranBiomeCheck = true;
        }

        BlockPos blockpos = null;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(0, 0, 0);
        double d0 = Double.MAX_VALUE;

        for (ChunkPos chunkpos : this.structureCoords)
        {
            blockpos$mutableblockpos.setPos((chunkpos.x << 4) + 8, 32, (chunkpos.z << 4) + 8);
            double d1 = blockpos$mutableblockpos.distanceSq(pos);

            if (blockpos == null)
            {
                blockpos = new BlockPos(blockpos$mutableblockpos);
                d0 = d1;
            }
            else if (d1 < d0)
            {
                blockpos = new BlockPos(blockpos$mutableblockpos);
                d0 = d1;
            }
        }

        return blockpos;
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        if (!this.ranBiomeCheck)
        {
            this.generatePositions();
            this.ranBiomeCheck = true;
        }

        for (ChunkPos chunkpos : this.structureCoords)
        {
            if (chunkX == chunkpos.x && chunkZ == chunkpos.z)
            {
                return true;
            }
        }

        return false;
    }

    private void generatePositions()
    {
        this.initializeStructureData(this.world);
        int i = 0;
        ObjectIterator<?> lvt_2_1_ = this.structureMap.values().iterator();

        while (lvt_2_1_.hasNext())
        {
            StructureStart structurestart = (StructureStart)lvt_2_1_.next();

            if (i < this.structureCoords.length)
            {
                this.structureCoords[i++] = new ChunkPos(structurestart.getChunkPosX(), structurestart.getChunkPosZ());
            }
        }

        Random random = new Random();
        random.setSeed(this.world.getSeed());
        double d1 = random.nextDouble() * Math.PI * 2.0D;
        int j = 0;
        int k = 0;
        int l = this.structureMap.size();

        if (l < this.structureCoords.length)
        {
            for (int i1 = 0; i1 < this.structureCoords.length; ++i1)
            {
                double d0 = 4.0D * this.distance + this.distance * (double)j * 6.0D + (random.nextDouble() - 0.5D) * this.distance * 2.5D;
                int j1 = (int)Math.round(Math.cos(d1) * d0);
                int k1 = (int)Math.round(Math.sin(d1) * d0);
                BlockPos blockpos = this.world.getBiomeProvider().findBiomePosition((j1 << 4) + 8, (k1 << 4) + 8, 112, this.allowedBiomes, random);

                if (blockpos != null)
                {
                    j1 = blockpos.getX() >> 4;
                    k1 = blockpos.getZ() >> 4;
                }

                if (i1 >= l)
                {
                    this.structureCoords[i1] = new ChunkPos(j1, k1);
                }

                d1 += (Math.PI * 2D) / (double)this.spread;
                ++k;

                if (k == this.spread)
                {
                    ++j;
                    k = 0;
                    this.spread += 2 * this.spread / (j + 1);
                    this.spread = Math.min(this.spread, this.structureCoords.length - i1);
                    d1 += random.nextDouble() * Math.PI * 2.0D;
                }
            }
        }
    }
}