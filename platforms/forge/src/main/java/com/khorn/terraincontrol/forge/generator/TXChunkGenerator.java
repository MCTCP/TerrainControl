package com.khorn.terraincontrol.forge.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Z_SIZE;

import java.util.List;

import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

public class TXChunkGenerator implements IChunkGenerator
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

    public TXChunkGenerator(ForgeWorld _world)
    {
        this.world = _world;
        this.worldHandle = _world.getWorld();

        this.TestMode = this.world.getConfigs().getWorldConfig().ModeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.generator = new ChunkProviderTC(this.world.getConfigs(), this.world);
        this.spawner = new ObjectSpawner(this.world.getConfigs(), this.world);

    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ)
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
        ConfigProvider configProvider = this.world.getConfigs();
        this.biomeIntArray = this.world.getBiomeGenerator().getBiomes(this.biomeIntArray,
                chunk.x * CHUNK_X_SIZE, chunk.z * CHUNK_Z_SIZE,
                CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);

        for (int i = 0; i < chunkBiomeArray.length; i++)
        {
            int generationId = this.biomeIntArray[i];
            chunkBiomeArray[i] = (byte) configProvider.getBiomeByIdOrNull(generationId).getIds().getSavedId();
        }
    }

    @Override
    public void populate(int chunkX, int chunkZ)
    {
        if (this.TestMode)
            return;
        BlockSand.fallInstantly = true;
        this.spawner.populate(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));
        BlockSand.fallInstantly = false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType paramaca, BlockPos blockPos)
    {
        WorldConfig worldConfig = this.world.getConfigs().getWorldConfig();
        Biome biomeBase = this.worldHandle.getBiomeForCoordsBody(blockPos);

        if (worldConfig.rareBuildingsEnabled)
        {
            if (paramaca == EnumCreatureType.MONSTER && this.world.rareBuildingGen.isSwampHut(blockPos))
            {
                return this.world.rareBuildingGen.getMonsters();
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
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean flag)
    {
        // Gets the nearest stronghold
        System.out.println("Structure Name: " + worldIn + " / " + structureName + " / " + flag);

        if (("Stronghold".equals(structureName)) && (this.world.strongholdGen != null))
        {
            return this.world.strongholdGen.getNearestStructurePos(worldIn, position, flag);
        }

        if (("Mansion".equals(structureName)) && (this.world.mansionGen != null))
        {
            return this.world.mansionGen.getNearestStructurePos(worldIn, position, flag);
        }

        if (("Temple".equals(structureName)) && (this.world.rareBuildingGen != null))
        {
            return this.world.rareBuildingGen.getNearestStructurePos(worldIn, position, flag);
        }

        if (("TCTemple".equals(structureName)) && (this.world.rareBuildingGen != null))
        {
            return this.world.rareBuildingGen.getNearestStructurePos(worldIn, position, flag);
        }

        if (("Monument".equals(structureName)) && (this.world.oceanMonumentGen != null))
        {
            return this.world.oceanMonumentGen.getNearestStructurePos(worldIn, position, flag);
        }

        if (("Village".equals(structureName)) && (this.world.villageGen != null))
        {
            return this.world.villageGen.getNearestStructurePos(worldIn, position, flag);
        }

        if (("Mineshaft".equals(structureName)) && (this.world.mineshaftGen != null))
        {
            return this.world.mineshaftGen.getNearestStructurePos(worldIn, position, flag);
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
        if (worldConfig.mansionsEnabled)
        {
            this.world.mansionGen.generate(this.world.getWorld(), chunkX, chunkZ, null);
        }
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        WorldConfig worldConfig = this.world.getConfigs().getWorldConfig();

        if (worldConfig.mineshaftsEnabled)
        {
            return this.world.mineshaftGen.isInsideStructure(pos);
        }
        if (worldConfig.villagesEnabled)
        {
            return this.world.villageGen.isInsideStructure(pos);
        }
        if (worldConfig.strongholdsEnabled)
        {
            return this.world.strongholdGen.isInsideStructure(pos);
        }
        if (worldConfig.rareBuildingsEnabled)
        {
            return this.world.rareBuildingGen.isInsideStructure(pos);
        }
        if (worldConfig.netherFortressesEnabled)
        {
            return this.world.netherFortressGen.isInsideStructure(pos);
        }
        if (worldConfig.oceanMonumentsEnabled)
        {
            return this.world.oceanMonumentGen.isInsideStructure(pos);
        }
        if (worldConfig.mansionsEnabled)
        {
            return this.world.mansionGen.isInsideStructure(pos);
        }
        return false;
    }

}
