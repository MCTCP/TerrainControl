package com.pg85.otg.paper.gen;

import java.util.Random;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.ScheduledTick;
import org.apache.commons.lang.NotImplementedException;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.paper.materials.PaperMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.TreeType;
import com.pg85.otg.util.nbt.NamedBinaryTag;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class MCWorldGenRegion extends PaperWorldGenRegion
{
	/** 
	 * Creates a LocalWorldGenRegion to be used for non-OTG worlds, used for /otg spawn/edit/export.
	 * Cannot use any functionality requiring OTGNoiseChhunkGenerator or OTGBiomeProvider 
	 * */
	public MCWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, WorldGenLevel worldGenRegion)
	{
		super(presetFolderName, worldConfig, worldGenRegion);
	}

	@Override
	public IBiome getBiomeForDecoration(int worldX, int worldZ)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");		
	}
	
	@Override
	public IBiomeConfig getBiomeConfigForDecoration(int worldX, int worldZ)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}

	@Override
	public double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}

	@Override
	public LocalMaterialData getMaterial(int x, int y, int z)
	{
		if (y >= Constants.WORLD_HEIGHT || y < Constants.WORLD_DEPTH)
		{
			return null;
		}

		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
		ChunkAccess chunk = this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ(), ChunkStatus.EMPTY, false);

		// Tried to query an unloaded chunk outside the area being decorated
		if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS))
		{
			return null;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		return PaperMaterialData.ofBlockData(chunk.getBlockState(internalX, y, internalZ));
	}


	@Override
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		// If the chunk exists or is inside the area being decorated, fetch it normally.
		ChunkAccess chunk = this.worldGenRegion.getChunkIfLoadedImmediately(chunkCoord.getChunkX(), chunkCoord.getChunkZ());

		// Tried to query an unloaded chunk outside the area being decorated
		if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS))
		{
			return -1;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		int heightMapY = chunk.getHeight(Types.WORLD_SURFACE, internalX, internalZ);
		return getHighestBlockYAt(chunk, internalX, heightMapY, internalZ, findSolid, findLiquid, ignoreLiquid, ignoreSnow, ignoreLeaves);		
	}
	
	@Override
	public void setBlock (int x, int y, int z, LocalMaterialData material, NamedBinaryTag nbt, ReplaceBlockMatrix replaceBlocksMatrix)
	{
		if (y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)
		{
			return;
		}

		if (material.isEmpty())
		{
			// Happens when configs contain blocks that don't exist.
			// TODO: Catch this earlier up the chain, avoid doing work?
			return;
		}

		BlockPos pos = new BlockPos(x, y, z);
		this.worldGenRegion.setBlock(pos, ((PaperMaterialData) material).internalBlock(), 2 | 16);

		if (material.isLiquid())
		{
			this.worldGenRegion.getFluidTicks().schedule(new ScheduledTick<Fluid>(((PaperMaterialData)material).internalBlock().getFluidState().getType(), pos, 0L, 0L));
		}

		if (nbt != null)
		{
			this.attachNBT(x, y, z, nbt);
		}
	}

	@Override
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}	
	
	@Override
	public void placeDungeon (Random random, int x, int y, int z)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}

	@Override
	public void placeFossil(Random random, int x, int y, int z)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}
	
	// Shadowgen

	@Override
	public LocalMaterialData getMaterialWithoutLoading(int x, int y, int z)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}	
	
	@Override
	public int getHighestBlockYAtWithoutLoading(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature."); 
	}
	
	@Override
	public boolean chunkHasDefaultStructure (Random worldRandom, ChunkCoordinate chunkCoordinate)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}
	@Override
	public long getSeed()
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}
}
