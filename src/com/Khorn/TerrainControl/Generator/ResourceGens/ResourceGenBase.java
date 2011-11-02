package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.Chunk;
import net.minecraft.server.World;

import java.util.Random;

public abstract class ResourceGenBase
{
    protected Chunk cacheChunk;
    protected World world;
    protected Random rand;

    public ResourceGenBase(World world)
    {
        this.world = world;
    }

    public void Process( Random _rand, Resource res, int x, int z)
    {
        this.rand = _rand;
        this.SpawnResource(res, x, z);

    }

    protected abstract void SpawnResource(Resource res, int x, int z);

    protected void SetRawBlockId(int x, int y, int z, int BlockId)
    {
        if (cacheChunk.x != x >> 4 || cacheChunk.z != z >> 4)
            this.cacheChunk = this.world.getChunkAt(x >> 4, z >> 4);
        if (y >= 128 || y < 0)
            return;

        this.cacheChunk.b[((z & 0xF) * 16 + (x & 0xF)) * 128 + y] = (byte) BlockId;
    }

    protected void SetRawBlockIdAndData(int x, int y, int z, int BlockId, int Data)
    {
        if (cacheChunk.x != x >> 4 || cacheChunk.z != z >> 4)
            this.cacheChunk = this.world.getChunkAt(x >> 4, z >> 4);
        z = z & 0xF;
        x = x & 0xF;
        if (y >= 128 || y < 0)
            return;
        this.cacheChunk.g.a(x, y, z, Data);

        this.cacheChunk.b[(z * 16 + x) * 128 + y] = (byte) BlockId;
    }

    protected int GetRawBlockId(int x, int y, int z)
    {
        if (cacheChunk.x != x >> 4 || cacheChunk.z != z >> 4)
            this.cacheChunk = this.world.getChunkAt(x >> 4, z >> 4);

        z = z & 0xF;
        x = x & 0xF;
        if (y >= 128 || y < 0)
            return 0;

        return (int) this.cacheChunk.b[(z * 16 + x) * 128 + y];
    }

    protected int GetRawBlockData(int x, int y, int z)
    {
        if (cacheChunk.x != x >> 4 || cacheChunk.z != z >> 4)
            this.cacheChunk = this.world.getChunkAt(x >> 4, z >> 4);

        z = z & 0xF;
        x = x & 0xF;
        if (y >= 128 || y < 0)
            return 0;

        return this.cacheChunk.g.a(x, y, z);
    }
}
