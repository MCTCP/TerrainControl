package com.khorn.terraincontrol.forge.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Z_SIZE;

import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.List;

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

        this.TestMode = world.getConfigs().getWorldConfig().ModeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.generator = new ChunkProviderTC(this.world.getConfigs(), this.world);
        this.spawner = new ObjectSpawner(this.world.getConfigs(), this.world);

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
        ConfigProvider configProvider = world.getConfigs();
        biomeIntArray = world.getBiomeGenerator().getBiomes(biomeIntArray,
                chunk.xPosition * CHUNK_X_SIZE, chunk.zPosition * CHUNK_Z_SIZE,
                CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);

        for (int i = 0; i < chunkBiomeArray.length; i++)
        {
            int generationId = biomeIntArray[i];
            chunkBiomeArray[i] = (byte) configProvider.getBiomeByIdOrNull(generationId).getIds().getSavedId();
        }
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

    // getPossibleCreatures
    @Override
    public List<SpawnListEntry> func_177458_a(EnumCreatureType paramaca, BlockPos blockPos)
    {
        WorldConfig worldConfig = this.world.getConfigs().getWorldConfig();
        BiomeGenBase biomeBase = this.worldHandle.getBiomeGenForCoords(blockPos);

        if (worldConfig.rareBuildingsEnabled)
        {
            if (paramaca == EnumCreatureType.MONSTER && this.world.rareBuildingGen.isSwampHutAtLocation(blockPos))
            {
                return this.world.rareBuildingGen.getMonsterSpawnList();
            }
        }
        if (worldConfig.oceanMonumentsEnabled)
        {
            if (paramaca == EnumCreatureType.MONSTER && this.world.oceanMonumentGen.func_175796_a(this.worldHandle, blockPos))
            {
                return this.world.oceanMonumentGen.getMonsterSpawnList();
            }
        }
        @SuppressWarnings("unchecked")
        List<SpawnListEntry> returnList = biomeBase.getSpawnableList(paramaca);
        return returnList;
    }

    // findNearestStructure
    @Override
    public BlockPos func_180513_a(World worldIn, String s, BlockPos blockPos)
    {
        // Gets the nearest stronghold
        if (("Stronghold".equals(s)) && (this.world.strongholdGen != null))
        {
            return this.world.strongholdGen.func_180706_b(worldIn, blockPos);
        }
        return null;
    }

    @Override
    public int getLoadedChunkCount()
    {
        return 0;
    }

    @Override
    public void func_180514_a(Chunk p_180514_1_, int chunkX, int chunkZ)
    {
        // recreateStructures
        WorldConfig worldConfig = world.getConfigs().getWorldConfig();
        if (worldConfig.mineshaftsEnabled)
        {
            world.mineshaftGen.func_175792_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.villagesEnabled)
        {
            world.villageGen.func_175792_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.strongholdsEnabled)
        {
            world.strongholdGen.func_175792_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.rareBuildingsEnabled)
        {
            world.rareBuildingGen.func_175792_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.netherFortressesEnabled)
        {
            world.netherFortressGen.func_175792_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.oceanMonumentsEnabled)
        {
            world.oceanMonumentGen.func_175792_a(this, world.getWorld(), chunkX, chunkZ, null);
        }
    }

    @Override
    public void saveExtraData()
    {
        // Empty, just like Minecraft's ChunkProviderGenerate
    }

    @Override
    public Chunk func_177459_a(BlockPos blockPos)
    {
        // provideChunkForBlock
        return provideChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    @Override
    public boolean func_177460_a(IChunkProvider chunkProvider, Chunk chunk, int chunkX, int chunkZ)
    {
        // retroGen -> generated ocean monument in existing chunks in vanilla
        // Disabled, as
        // * it's not enabled in the Bukkit version, as Spigot's
        // generator API doesn't support it
        // * people updating to 1.8 might be surprised why this monument
        // spawns
        // in existing chunks of their customized ocean biome
        // * changing the spawn settings of ocean monuments makes them spawn
        // at different positions, so extra monuments will be spawned in old
        // chunks
        return false;
    }

}
