package com.khorn.terraincontrol.forge.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Z_SIZE;

import java.util.List;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;

public class ChunkProvider implements IChunkGenerator
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

        this.TestMode = this.world.getConfigs().getWorldConfig().ModeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.generator = new ChunkProviderTC(this.world.getConfigs(), this.world);
        this.spawner = new ObjectSpawner(this.world.getConfigs(), this.world);
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ)
    {
    	ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
    	
    	Chunk chunk = new Chunk(this.worldHandle, chunkX, chunkZ);
		if(world.IsInsideWorldBorder(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), false))
    	{
	        ForgeChunkBuffer chunkBuffer = new ForgeChunkBuffer(chunkCoord);
	        this.generator.generate(chunkBuffer);
	        	        
	        chunk = chunkBuffer.toChunk(this.worldHandle);
	        fillBiomeArray(chunk);
	        chunk.generateSkylightMap();	        
    	}    		

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
        ConfigProvider configProvider = this.world.getConfigs();
        this.biomeIntArray = this.world.getBiomeGenerator().getBiomes(this.biomeIntArray, chunk.xPosition * CHUNK_X_SIZE,
                chunk.zPosition * CHUNK_Z_SIZE, CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);

        for (int i = 0; i < chunkBiomeArray.length; i++)
        {
            int generationId = this.biomeIntArray[i];
               
            // For forge make sure all dimensions are queried since the biome we're looking for may be owned by another dimension
            LocalBiome biome = TerrainControl.isForge ? TerrainControl.getBiomeAllWorlds(generationId) : configProvider.getBiomeByIdOrNull(generationId);
        	
        	chunkBiomeArray[i] = (byte) biome.getIds().getSavedId();
        }
    }

    @Override
    public void populate(int chunkX, int chunkZ)
    {      
    	if(this.TestMode || !world.IsInsideWorldBorder(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), true))
        {
            return;
        }
        
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        
        BlockSand.fallInstantly = true;
        BlockGravel.fallInstantly = true;        
        
        this.spawner.populate(chunkCoord);       
        
        BlockSand.fallInstantly = false;
        BlockGravel.fallInstantly = false;
    }    

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType paramaca, BlockPos blockPos)
    {
        WorldConfig worldConfig = this.world.getConfigs().getWorldConfig();
        Biome biomeBase = this.worldHandle.getBiomeForCoordsBody(blockPos);

        if (worldConfig.rareBuildingsEnabled)
        {
            if (paramaca == EnumCreatureType.MONSTER && this.world.rareBuildingGen.isSwampHutAtLocation(blockPos))
            {
                return this.world.rareBuildingGen.getMonsterSpawnList();
            }
        }
        if (worldConfig.oceanMonumentsEnabled)
        {
            if (paramaca == EnumCreatureType.MONSTER && this.world.oceanMonumentGen.isPositionInStructure(this.worldHandle, blockPos))
            {
                return this.world.oceanMonumentGen.getMonsterSpawnList();
            }
        }
        return biomeBase.getSpawnableList(paramaca);
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos blockPos)
    {
        // Gets the nearest stronghold
        if (("Stronghold".equals(structureName)) && (this.world.strongholdGen != null))
        {
            return this.world.strongholdGen.getClosestStrongholdPos(worldIn, blockPos);
        }
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int chunkX, int chunkZ)
    {
        // recreateStructures
        WorldConfig worldConfig = this.world.getConfigs().getWorldConfig();
           
        if (worldConfig.mineshaftsEnabled)
        {
            this.world.mineshaftGen.generate(this.world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.villagesEnabled)
        {
            this.world.villageGen.generate(this.world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.strongholdsEnabled)
        {
            this.world.strongholdGen.generate(this.world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.rareBuildingsEnabled)
        {
            this.world.rareBuildingGen.generate(this.world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.netherFortressesEnabled)
        {
            this.world.netherFortressGen.generate(this.world.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.oceanMonumentsEnabled)
        {
            this.world.oceanMonumentGen.generate(this.world.getWorld(), chunkX, chunkZ, null);
        }
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        // retroGen -> generated ocean monument in existing chunks in vanilla
        // Disabled, as it's not enabled in the Bukkit version, as Spigot's
        // generator API doesn't support it people updating to 1.8 might be surprised why this monument
        // spawns in existing chunks of their customized ocean biome changing the spawn settings of ocean monuments makes them spawn
        // at different positions, so extra monuments will be spawned in old chunks
        return false;
    }    
}
