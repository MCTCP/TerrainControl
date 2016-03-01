package com.khorn.terraincontrol.bukkit.generator.structures;

import com.google.common.collect.Lists;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.WorldGenStronghold.WorldGenStronghold2Start;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StrongholdGen extends StructureGenerator
{

    private List<BiomeBase> allowedBiomes;
    private boolean b;
    private ChunkCoordIntPair[] structureCoords;
    private double distance;
    private int spread;

    public StrongholdGen(WorldSettings configs)
    {
        this.structureCoords = new ChunkCoordIntPair[configs.worldConfig.strongholdCount];
        this.distance = configs.worldConfig.strongholdDistance;
        this.spread = configs.worldConfig.strongholdSpread;
        this.allowedBiomes = Lists.newArrayList();

        for (LocalBiome biome : configs.biomes)
        {
            if (biome == null)
                continue;

            if (biome.getBiomeConfig().strongholdsEnabled)
            {
                this.allowedBiomes.add(((BukkitBiome) biome).getHandle());
            }
        }
    }

    @Override
    public String a()
    {
        return StructureNames.STRONGHOLD;
    }

    @Override
    public BlockPosition getNearestGeneratedFeature(World world, BlockPosition blockposition)
    {
        if (!this.b)
        {
            this.c();
            this.b = true;
        }

        BlockPosition blockposition1 = null;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(0, 0, 0);
        double d0 = Double.MAX_VALUE;
        ChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;
        int i = achunkcoordintpair.length;

        for (int j = 0; j < i; ++j)
        {
            ChunkCoordIntPair chunkcoordintpair = achunkcoordintpair[j];

            blockposition_mutableblockposition.c((chunkcoordintpair.x << 4) + 8, 32, (chunkcoordintpair.z << 4) + 8);
            double d1 = blockposition_mutableblockposition.k(blockposition);

            if (blockposition1 == null)
            {
                blockposition1 = new BlockPosition(blockposition_mutableblockposition);
                d0 = d1;
            } else if (d1 < d0)
            {
                blockposition1 = new BlockPosition(blockposition_mutableblockposition);
                d0 = d1;
            }
        }

        return blockposition1;
    }

    @Override
    protected boolean a(int i, int j)
    {
        if (!this.b)
        {
            this.c();
            this.b = true;
        }

        ChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;
        int k = achunkcoordintpair.length;

        for (int l = 0; l < k; ++l)
        {
            ChunkCoordIntPair chunkcoordintpair = achunkcoordintpair[l];

            if (i == chunkcoordintpair.x && j == chunkcoordintpair.z)
            {
                return true;
            }
        }

        return false;
    }

    private void c()
    {
        this.a(this.g);
        int i = 0;

        for (StructureStart structurestart : this.c.values())
        {
            if (i < this.structureCoords.length)
            {
                this.structureCoords[i++] = new ChunkCoordIntPair(structurestart.e(), structurestart.f());
            }
        }

        Random random = new Random();

        random.setSeed(this.g.getSeed());
        double d0 = random.nextDouble() * 3.141592653589793D * 2.0D;
        int j = 0;
        int k = 0;
        int l = this.c.size();

        if (l < this.structureCoords.length)
        {
            for (int i1 = 0; i1 < this.structureCoords.length; ++i1)
            {
                double d1 = 4.0D * this.distance + this.distance * j * 6.0D + (random.nextDouble() - 0.5D) * this.distance * 2.5D;
                int j1 = (int) Math.round(Math.cos(d0) * d1);
                int k1 = (int) Math.round(Math.sin(d0) * d1);
                BlockPosition blockposition = this.g.getWorldChunkManager().a((j1 << 4) + 8, (k1 << 4) + 8, 112, this.allowedBiomes, random);

                if (blockposition != null)
                {
                    j1 = blockposition.getX() >> 4;
                    k1 = blockposition.getZ() >> 4;
                }

                if (i1 >= l)
                {
                    this.structureCoords[i1] = new ChunkCoordIntPair(j1, k1);
                }

                d0 += 6.283185307179586D / this.spread;
                ++k;
                if (k == this.spread)
                {
                    ++j;
                    k = 0;
                    this.spread += 2 * this.spread / (j + 1);
                    this.spread = Math.min(this.spread, this.structureCoords.length - i1);
                    d0 += random.nextDouble() * 3.141592653589793D * 2.0D;
                }
            }
        }

    }

    @Override
    protected List<BlockPosition> E_()
    {
        ArrayList<BlockPosition> arraylist = Lists.newArrayList();
        ChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;
        int i = achunkcoordintpair.length;

        for (int j = 0; j < i; ++j)
        {
            ChunkCoordIntPair chunkcoordintpair = achunkcoordintpair[j];

            if (chunkcoordintpair != null)
            {
                arraylist.add(chunkcoordintpair.a(64));
            }
        }

        return arraylist;
    }

    @Override
    protected StructureStart b(int i, int j)
    {
        WorldGenStronghold2Start start = new WorldGenStronghold2Start(this.g, this.f, i, j);

        while (start.c().isEmpty() || ((WorldGenStrongholdPieces.WorldGenStrongholdStart) start.c().get(0)).b == null)
        {
            start = new WorldGenStronghold.WorldGenStronghold2Start(this.g, this.f, i, j);
        }

        return start;
    }
}
