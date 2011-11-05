package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.*;

import java.util.Random;

public abstract class ResourceGenBase
{
    protected Chunk cacheChunk;
    protected World world;
    protected Random rand;

    public ResourceGenBase(World world)
    {
        this.world = world;
        this.cacheChunk = ((ChunkProviderServer) this.world.chunkProvider).emptyChunk;
    }

    public void Process(Random _rand, Resource res, int _x, int _z)
    {
        this.rand = _rand;


        for (int t = 0; t < res.Frequency; t++)
        {
            if (this.rand.nextInt(100) > res.Rarity)
                continue;
            int x = _x + this.rand.nextInt(16) + 8;
            int z = _z + this.rand.nextInt(16) + 8;
            this.SpawnResource(res, x, z);
        }

    }

    protected abstract void SpawnResource(Resource res, int x, int z);

    private void CheckChunk(int x, int z)
    {
        if (cacheChunk == null || cacheChunk.x != x >> 4 || cacheChunk.z != z >> 4)
            this.cacheChunk = this.world.getChunkAt(x >> 4, z >> 4);
    }

    protected void SetRawBlockId(int x, int y, int z, int BlockId)
    {
        CheckChunk(x, z);
        z = z & 0xF;
        x = x & 0xF;
        if (y >= 128 || y < 0)
            return;

        this.cacheChunk.b[x << 11 | z << 7 | y] = (byte) BlockId;
    }

    protected void SetRawBlockIdAndData(int x, int y, int z, int BlockId, int Data)
    {
        CheckChunk(x, z);
        z = z & 0xF;
        x = x & 0xF;
        if (y >= 128 || y < 0)
            return;

        this.cacheChunk.b[x << 11 | z << 7 | y] = (byte) BlockId;
        this.cacheChunk.g.a(x, y, z, Data);

    }

    protected int GetRawBlockId(int x, int y, int z)
    {
        CheckChunk(x, z);

        z = z & 0xF;
        x = x & 0xF;
        if (y >= 128 || y < 0)
            return 0;

        return (int) this.cacheChunk.b[x << 11 | z << 7 | y];
    }


    protected boolean isEmpty(int x, int y, int z)
    {
        return this.GetRawBlockId(x, y, z) == 0;

    }

    protected Material getMaterial(int x, int y, int z)
    {
        int id = this.GetRawBlockId(x, y, z);
        return id == 0 ? Material.AIR : Block.byId[id].material;

    }
}
