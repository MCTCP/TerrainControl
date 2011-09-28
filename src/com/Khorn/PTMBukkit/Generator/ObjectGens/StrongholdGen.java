package com.Khorn.PTMBukkit.Generator.ObjectGens;

import net.minecraft.server.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class StrongholdGen
{
    private BiomeBase[] a = {BiomeBase.DESERT, BiomeBase.FOREST, BiomeBase.EXTREME_HILLS, BiomeBase.SWAMPLAND, BiomeBase.PLAINS,BiomeBase.TAIGA};
    private boolean f;
    private ChunkCoordIntPair[] g = new ChunkCoordIntPair[3];

    protected HashMap<Long,StructureStart> e = new HashMap<Long,StructureStart>();

    private World world;
    private Random rand = new Random();

    private boolean CheckCoordinates(int paramInt1, int paramInt2)
    {
        int i;
        if (!this.f)
        {
            this.rand.setSeed(this.world.getSeed());

            double d1 = this.rand.nextDouble() * 3.141592653589793D * 2.0D;

            for (i = 0; i < this.g.length; i++)
            {
                double d2 = (1.25D + this.rand.nextDouble()) * 32.0D;
                int j = (int) Math.round(Math.cos(d1) * d2);
                int k = (int) Math.round(Math.sin(d1) * d2);

                ArrayList<BiomeBase> localArrayList = new ArrayList<BiomeBase>();
                for (BiomeBase localObject2 : this.a)
                {
                    localArrayList.add(localObject2);
                }

                ChunkPosition pair = this.world.getWorldChunkManager().a((j << 4) + 8, (k << 4) + 8, 112, localArrayList, this.rand);
                if (pair != null)
                {
                    j = pair.x >> 4;
                    k = pair.z >> 4;
                } else
                {
                    System.out.println("Placed stronghold in INVALID biome at (" + j + ", " + k + ")");
                }

                this.g[i] = new ChunkCoordIntPair(j, k);

                d1 += 6.283185307179586D / this.g.length;
            }
            this.f = true;
        }

        for (ChunkCoordIntPair localChunkCoordIntPair : this.g)
        {
            if ((paramInt1 == localChunkCoordIntPair.x) && (paramInt2 == localChunkCoordIntPair.z))
            {
                return true;
            }
        }
        return false;
    }

    private StructureStart b(int paramInt1, int paramInt2)
    {
        return new StrongholdStart(this.world, this.rand, paramInt1, paramInt2);
    }

    public void TestChunk(World world, int x, int z, byte[] chunkBytes)
    {
        int i = 8;
        this.world = world;
        if (this.e.containsKey(Long.valueOf(ChunkCoordIntPair.a(x, z))))
        {
            return;
        }


        this.rand.setSeed(world.getSeed());
        long l1 = this.rand.nextLong();
        long l2 = this.rand.nextLong();

        for (int j = x - i; j <= x + i; j++)
            for (int k = z - i; k <= z + i; k++)
            {
                long l3 = j * l1;
                long l4 = k * l2;
                this.rand.setSeed(l3 ^ l4 ^ world.getSeed());

                this.rand.nextInt();
                if (CheckCoordinates(x, z))
                {
                    StructureStart localStructureStart = b(x, z);
                    this.e.put(ChunkCoordIntPair.a(x, z), localStructureStart);
                }
            }
    }

    public boolean ProcessChunk(Random rnd, int x, int z)
    {
        int i = (x << 4) + 8;
        int j = (z << 4) + 8;

        boolean result = false;

        for (StructureStart localStructureStart : this.e.values())
        {
            if ((localStructureStart.a()) && (localStructureStart.b().a(i, j, i + 15, j + 15)))
            {
                localStructureStart.a(this.world, rnd, new StructureBoundingBox(i, j, i + 15, j + 15));
                result = true;
            }

        }

        return result;
    }

}