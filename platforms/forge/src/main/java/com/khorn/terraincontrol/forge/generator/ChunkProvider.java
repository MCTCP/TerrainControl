package com.khorn.terraincontrol.forge.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Z_SIZE;

import java.util.List;

import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.ChunkCoordinate;

public class ChunkProvider implements IChunkProvider
{

    private ForgeWorld world;
    private World worldHandle;
    private boolean TestMode = false;

    private ChunkProviderTC generator;
    private ObjectSpawner spawner;

    /** 
     * Used in {@link #fillBiomeArray(Chunk)}, to avoid creating
     * new int arrays.
     */
    private int[] biomeIntArray;

    public ChunkProvider(ForgeWorld _world)
    {
        this.world = _world;
        this.worldHandle = _world.getWorld();

        this.TestMode = world.getSettings().worldConfig.ModeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.generator = new ChunkProviderTC(this.world.getSettings(), this.world);
        this.spawner = new ObjectSpawner(this.world.getSettings(), this.world);

    }

    @Override
    public boolean chunkExists(int i, int i1)
    {
        return true;
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        ForgeChunkBuffer chunkBuffer = new ForgeChunkBuffer(chunkCoord);
        this.generator.generate(chunkBuffer);

        Chunk chunk = chunkBuffer.toChunk(this.worldHandle);
        fillBiomeArray(chunk);
        chunk.generateSkylightMap();

        return chunk;
    }

    /**
     * Fills the biome array of a chunk with the proper saved ids (no
     * generation ids).
     * @param chunk The chunk to fill the biomes of.
     */
    private void fillBiomeArray(Chunk chunk)
    {
        byte[] chunkBiomeArray = chunk.getBiomeArray();
        LocalBiome[] biomeMap = world.getSettings().biomes;
        biomeIntArray = world.getBiomeGenerator().getBiomes(biomeIntArray,
                chunk.xPosition * CHUNK_X_SIZE, chunk.zPosition * CHUNK_Z_SIZE,
                CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);

        for (int i = 0; i < chunkBiomeArray.length; i++)
        {
            int biomeId = biomeIntArray[i];
            chunkBiomeArray[i] = (byte) biomeMap[biomeId].getIds().getSavedId();
        }
    }

    @Override
    public Chunk loadChunk(int i, int i1)
    {
        return provideChunk(i, i1);
    }

    @Override
    public void populate(IChunkProvider ChunkProvider, int x, int z)
    {
        if (this.TestMode)
            return;
        BlockSand.fallInstantly = true;
        this.spawner.populate(ChunkCoordinate.fromChunkCoords(x, z));
        BlockSand.fallInstantly = false;
    }

    @Override
    public boolean saveChunks(boolean b, IProgressUpdate il)
    {
        return true;
    }

    @Override
    public boolean unloadQueuedChunks()
    {
        return false;
    }

    @Override
    public boolean canSave()
    {
        return true;
    }

    @Override
    public String makeString()
    {
        return "TerrainControlLevelSource";
    }

    @Override
    public List<?> getPossibleCreatures(EnumCreatureType paramaca, int paramInt1, int paramInt2, int paramInt3)
    {
        BiomeGenBase Biome = this.worldHandle.getBiomeGenForCoords(paramInt1, paramInt3);
        if (Biome == null)
        {
            return null;
        }
        return Biome.getSpawnableList(paramaca);
    }

    @Override
    public ChunkPosition func_147416_a(World world, String s, int x, int y, int z)
    {
        // Gets the nearest stronghold
        if (("Stronghold".equals(s)) && (this.world.strongholdGen != null))
        {
            return this.world.strongholdGen.func_151545_a(world, x, y, z);
        }
        return null;
    }

    @Override
    public int getLoadedChunkCount()
    {
        return 0;
    }

    @Override
    public void recreateStructures(int chunkX, int chunkZ)
    {
        if (world.mineshaftGen != null)
        {
            world.mineshaftGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (world.villageGen != null)
        {
            world.villageGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (world.strongholdGen != null)
        {
            world.strongholdGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (world.rareBuildingGen != null)
        {
            world.rareBuildingGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (world.netherFortressGen != null)
        {
            world.netherFortressGen.func_151539_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
    }

    @Override
    public void saveExtraData()
    {
        // Empty, just like Minecraft's ChunkProviderGenerate
    }
}
