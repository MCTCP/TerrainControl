package com.pg85.otg.forge.gen;

import java.text.MessageFormat;
import java.util.Random;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonSyntaxException;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.nbt.NamedBinaryTag;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.TreeType;

import net.minecraft.nbt.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class MCWorldGenRegion extends ForgeWorldGenRegion
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

		ChunkAccess chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		
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
		ChunkAccess chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		
		// Tried to query an unloaded chunk outside the area being decorated
		if(chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS))
		{
			return -1;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;	
		int heightMapy = chunk.getHeight(Types.WORLD_SURFACE, internalX, internalZ);
		
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
			this.worldGenRegion.scheduleTick(pos, ((ForgeMaterialData)material).internalBlock().getFluidState().getType(), 0);
		}

		if (nbt != null)
		{
			this.attachNBT(x, y, z, nbt);
		}
	}

	private void attachNBT(int x, int y, int z, NamedBinaryTag nbt)
	{
		CompoundTag nms = ForgeNBTHelper.getNMSFromNBTTagCompound(nbt);
		nms.put("x", IntTag.valueOf(x));
		nms.put("y", IntTag.valueOf(y));
		nms.put("z", IntTag.valueOf(z));

		BlockEntity tileEntity = this.worldGenRegion.getBlockEntity(new BlockPos(x, y, z));
		if (tileEntity != null)
		{
			try {
				tileEntity.deserializeNBT(nms);
			} catch (JsonSyntaxException e) {
				if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					this.logger.log(
						LogLevel.ERROR,
						LogCategory.CUSTOM_OBJECTS,
						MessageFormat.format(
							"Badly formatted json for tile entity with id '{0}' at {1},{2},{3}", 
							nms.getString("id"), 
							x, y, z
						)
					);
				}
			}
		} else {
			if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				this.logger.log(
					LogLevel.ERROR,
					LogCategory.CUSTOM_OBJECTS,
					MessageFormat.format(
						"Skipping tile entity with id {0}, cannot be placed at {1},{2},{3}", 
						nms.getString("id"), 
						x, y, z
					)
				);
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
	public boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate)
	{
		throw new NotImplementedException("This method is not available for non-OTG worlds, you're trying to use an unsupported feature.");
	}
}
