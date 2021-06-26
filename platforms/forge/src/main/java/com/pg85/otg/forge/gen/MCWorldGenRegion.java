package com.pg85.otg.forge.gen;

import java.util.Random;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.interfaces.IBiome;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.TreeType;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap.Type;

public class MCWorldGenRegion extends ForgeWorldGenRegion
{
	/** 
	 * Creates a LocalWorldGenRegion to be used for non-OTG worlds, used for /otg spawn/edit/export.
	 * Cannot use any functionality requiring OTGNoiseChhunkGenerator or OTGBiomeProvider 
	 * */
	public MCWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, ISeedReader worldGenRegion)
	{
		super(presetFolderName, worldConfig, worldGenRegion);
	}
		
	@Override
	public IBiome getBiome(int x, int z) // TODO: Implement 3d biomes
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}

	@Override
	public BiomeConfig getBiomeConfig(int x, int z) // TODO: Implement 3d biomes
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
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
	public double getBiomeBlocksNoiseValue(int blockX, int blockZ)
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

		IChunk chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		
		// Tried to query an unloaded chunk outside the area being decorated
		if(chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS))
		{
			return null;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		return ForgeMaterialData.ofBlockState(chunk.getBlockState(new BlockPos(internalX, y, internalZ)));
	}
	
	@Override
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
		
		// If the chunk exists or is inside the area being decorated, fetch it normally.
		IChunk chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		
		// Tried to query an unloaded chunk outside the area being decorated
		if(chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS))
		{
			return -1;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;	
		int heightMapy = chunk.getHeight(Type.WORLD_SURFACE, internalX, internalZ);
		
		return getHighestBlockYAt(chunk, internalX, heightMapy, internalZ, findSolid, findLiquid, ignoreLiquid, ignoreSnow, ignoreLeaves);
	}
	
	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag nbt, ReplaceBlockMatrix replaceBlocksMatrix)
	{
		if(y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)
		{
			return;
		}

		if(material.isEmpty())
		{
			// Happens when configs contain blocks that don't exist.
			// TODO: Catch this earlier up the chain, avoid doing work?
			return;
		}

		BlockPos pos = new BlockPos(x, y, z);
		this.worldGenRegion.setBlock(pos, ((ForgeMaterialData)material).internalBlock(), 2 | 16);

		if (material.isLiquid())
		{
			this.worldGenRegion.getLiquidTicks().scheduleTick(pos, ((ForgeMaterialData)material).internalBlock().getFluidState().getType(), 0);
		}

		if (nbt != null)
		{
			this.attachNBT(x, y, z, nbt, worldGenRegion.getBlockState(pos));
		}
	}

	private void attachNBT(int x, int y, int z, NamedBinaryTag nbt, BlockState state)
	{
		CompoundNBT nms = ForgeNBTHelper.getNMSFromNBTTagCompound(nbt);
		nms.put("x", IntNBT.valueOf(x));
		nms.put("y", IntNBT.valueOf(y));
		nms.put("z", IntNBT.valueOf(z));

		TileEntity tileEntity = this.worldGenRegion.getBlockEntity(new BlockPos(x, y, z));
		if (tileEntity != null)
		{
			try {
				tileEntity.deserializeNBT(state, nms);
			} catch (JsonSyntaxException e)
			{
				OTG.log(LogMarker.WARN, "Badly formatted json for tile entity with id '{}' at {},{},{}", nms.getString("id"), x, y, z);
			}
		} else {
			if(OTG.getEngine().getPluginConfig().getSpawnLogEnabled())
			{
				OTG.log(LogMarker.WARN, "Skipping tile entity with id {}, cannot be placed at {},{},{}", nms.getString("id"), x, y, z);
			}
		}
	}
	
	@Override
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}
	
	@Override
	public void placeDungeon(Random random, int x, int y, int z)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");		
	}

	@Override
	public void placeFossil(Random random, ChunkCoordinate chunkCoord)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");		
	}

	@Override
	public void placeFromRegistry(Random random, ChunkCoordinate chunkCoord, String id)
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
	public boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}
}
