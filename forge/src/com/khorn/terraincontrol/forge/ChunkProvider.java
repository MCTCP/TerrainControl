package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;

import java.util.List;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.BlockSand;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkPosition;
import net.minecraft.src.EnumCreatureType;
import net.minecraft.src.ExtendedBlockStorage;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.IProgressUpdate;
import net.minecraft.src.World;

public class ChunkProvider implements IChunkProvider
{

	private SingleWorld world;
	private World worldHandle;
	private boolean TestMode = false;

	private ChunkProviderTC generator;
	private ObjectSpawner spawner;

	public ChunkProvider(SingleWorld _world)
	{
		// super(_world.getWorld(), _world.getSeed());

		this.world = _world;
		this.worldHandle = _world.getWorld();

		this.TestMode = world.getSettings().ModeTerrain == WorldConfig.TerrainMode.TerrainTest;

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
		Chunk chunk = new Chunk(this.worldHandle, chunkX, chunkZ);

		byte[] BlockArray = this.generator.generate(chunkX, chunkZ);
		ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();

		int i1 = BlockArray.length / 256;
		for (int blockX = 0; blockX < 16; blockX++)
			for (int blockZ = 0; blockZ < 16; blockZ++)
				for (int blockY = 0; blockY < i1; blockY++)
				{
					int block = BlockArray[(blockX << world.getHeightBits() + 4 | blockZ << world.getHeightBits() | blockY)];
					if (block != 0)
					{
						int sectionId = blockY >> 4;
						if (sections[sectionId] == null)
						{
							sections[sectionId] = new ExtendedBlockStorage(sectionId << 4);
						}
						sections[sectionId].setExtBlockID(blockX, blockY & 0xF, blockZ, block);
					}
				}
		world.FillChunkForBiomes(chunk, chunkX, chunkZ);

		chunk.generateSkylightMap();
		return chunk;
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
		this.world.LoadChunk(x, z);
		this.spawner.populate(x, z);
		BlockSand.fallInstantly = false;
	}

	@Override
	public boolean saveChunks(boolean b, IProgressUpdate il)
	{
		return true;
	}
	
	@Override
	public boolean unload100OldestChunks()
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
	public List getPossibleCreatures(EnumCreatureType paramaca, int paramInt1, int paramInt2, int paramInt3)
	{
		BiomeGenBase Biome = this.worldHandle.getBiomeGenForCoords(paramInt1, paramInt3);
		if (Biome == null)
		{
			return null;
		}
		return Biome.getSpawnableList(paramaca);
	}

	@Override
	public ChunkPosition findClosestStructure(World world, String s, int x, int y, int z)
	{
		if (("Stronghold".equals(s)) && (this.world.strongholdGen != null))
		{
			return this.world.strongholdGen.getNearestInstance(world, x, y, z);
		}
		return null;
	}

	@Override
	public int getLoadedChunkCount()
	{
		return 0;
	}

	@Override
	public void func_82695_e(int var1, int var2)
	{
		// TODO What should this method do?
		// System.out.println("TerrainControl: com.khorn.terraincontrol.forge.ChunkProvider.func_82695_e(" + var1 + "," + var2 + ") is called.");
	}
}
