package com.pg85.otg.forge.gen;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Random;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.IEntityFunction;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.nbt.NamedBinaryTag;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.minecraft.TreeType;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.data.worldgen.placement.NetherPlacements;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.WorldGenRegion;

// TODO: Split up worldgenregion into separate classes, one for decoration/worldgen, one for non-worldgen.
public class ForgeWorldGenRegion extends LocalWorldGenRegion
{
	protected final WorldGenLevel worldGenRegion;
	private final OTGNoiseChunkGenerator chunkGenerator;

	// BO4 plotting may call hasDefaultStructures on chunks outside the area being decorated, in order to plot large structures.
	// It may query the same chunk multiple times, so use a fixed size cache.
	private final FifoMap<ChunkCoordinate, Boolean> cachedHasDefaultStructureChunks = new FifoMap<ChunkCoordinate, Boolean>(2048);

	/** Creates a LocalWorldGenRegion to be used during decoration for OTG worlds. */
	public ForgeWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, WorldGenRegion worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger(), worldGenRegion.getCenter().x, worldGenRegion.getCenter().z, chunkGenerator.getCachedBiomeProvider());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}
	
	/** Creates a LocalWorldGenRegion to be used for OTG worlds outside of decoration, only used for /otg spawn/edit/export. */
	public ForgeWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, WorldGenLevel worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}
	
	/** Creates a LocalWorldGenRegion to be used for non-OTG worlds outside of decoration, only used for /otg spawn/edit/export. */
	public ForgeWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, WorldGenLevel worldGenRegion)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = null;
	}
	
	@Override
	public ILogger getLogger()
	{
		return OTG.getEngine().getLogger();
	}	
	
	@Override
	public long getSeed()
	{
		return this.worldGenRegion.getSeed();
	}	

	@Override
	public Random getWorldRandom()
	{
		return this.worldGenRegion.getRandom();
	}

	@Override
	public ChunkCoordinate getSpawnChunk()
	{
		if(this.getWorldConfig().getSpawnPointSet())
		{
			return ChunkCoordinate.fromBlockCoords(this.getWorldConfig().getSpawnPointX(), this.getWorldConfig().getSpawnPointZ());
		} else {
			BlockPos spawnPos = this.worldGenRegion.getLevel().getSharedSpawnPos();
			return ChunkCoordinate.fromBlockCoords(spawnPos.getX(), spawnPos.getZ());
		}
	}
	
	public WorldGenLevel getInternal()
	{
		return worldGenRegion;
	}
	
	@Override
	public ICachedBiomeProvider getCachedBiomeProvider()
	{
		return this.chunkGenerator.getCachedBiomeProvider();
	}
	
	@Override
	public IBiome getBiomeForDecoration(int x, int z)
	{
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration. 
		return this.decorationBiomeCache != null ? this.decorationBiomeCache.getBiome(x, z) : this.getCachedBiomeProvider().getBiome(x, z);		
	}

	@Override
	public IBiomeConfig getBiomeConfigForDecoration(int x, int z)
	{
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		return this.decorationBiomeCache != null ? this.decorationBiomeCache.getBiomeConfig(x, z) : this.getCachedBiomeProvider().getBiomeConfig(x, z);
	}

	@Override
	public double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		return this.chunkGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}

	// TODO: Only used by resources using 3x3 decoration atm (so icebergs). Align all resources
	// to use 3x3, make them use the decoration cache and remove this method.
	@Override
	public LocalMaterialData getMaterialDirect(int x, int y, int z)
	{
		return ForgeMaterialData.ofBlockState(this.worldGenRegion.getBlockState(new BlockPos(x, y, z)));
	}
	
	@Override
	public LocalMaterialData getMaterial(int x, int y, int z)
	{
		if (y >= Constants.WORLD_HEIGHT || y < Constants.WORLD_DEPTH)
		{
			return null;
		}

		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		ChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if(this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}

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
	public int getBlockAboveLiquidHeight(int x, int z)
	{
		int highestY = getHighestBlockYAt(x, z, false, true, false, false, false);
		if(highestY >= 0)
		{
			return highestY + 1;
		} else {
			return -1;
		}
	}

	@Override
	public int getBlockAboveSolidHeight(int x, int z)
	{
		int highestY = getHighestBlockYAt(x, z, true, false, true, true, false);
		if(highestY >= 0)
		{
			return highestY + 1;
		} else {
			return -1;
		}
	}

	@Override
	public int getHighestBlockAboveYAt(int x, int z)
	{
		int highestY = getHighestBlockYAt(x, z, true, true, false, false, false);
		if(highestY >= 0)
		{
			return highestY + 1;
		} else {
			return -1;
		}
	}
	
	@Override
	public int getHighestBlockAboveYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		int highestY = getHighestBlockYAt(x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow, ignoreLeaves);
		if(highestY >= 0)
		{
			return highestY + 1;
		} else {
			return -1;
		}
	}
	
	@Override
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
		
		// If the chunk exists or is inside the area being decorated, fetch it normally.
		ChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if(this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}
		
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

	protected int getHighestBlockYAt(ChunkAccess chunk, int internalX, int heightMapY, int internalZ, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		LocalMaterialData material;
		boolean isSolid;
		boolean isLiquid;
		BlockState blockState;
		Block block;
		
		for(int i = heightMapY; i >= 0; i--)
		{
			blockState = chunk.getBlockState(new BlockPos(internalX, i, internalZ));
			block = blockState.getBlock();
			material = ForgeMaterialData.ofBlockState(blockState);
			isLiquid = material.isLiquid();
			isSolid =
			(
				(
					material.isSolid() &&
					(
						!ignoreLeaves || 
						(
							block != Blocks.ACACIA_LOG &&
							block != Blocks.BIRCH_LOG &&
							block != Blocks.DARK_OAK_LOG &&
							block != Blocks.JUNGLE_LOG &&
							block != Blocks.OAK_LOG &&
							block != Blocks.SPRUCE_LOG &&
							block != Blocks.STRIPPED_ACACIA_LOG &&
							block != Blocks.STRIPPED_BIRCH_LOG &&
							block != Blocks.STRIPPED_DARK_OAK_LOG &&
							block != Blocks.STRIPPED_JUNGLE_LOG &&
							block != Blocks.STRIPPED_OAK_LOG &&
							block != Blocks.STRIPPED_SPRUCE_LOG
						)
					)
				)
				||
				(
					!ignoreLeaves &&
					(
						block == Blocks.ACACIA_LEAVES ||
						block == Blocks.BIRCH_LEAVES ||
						block == Blocks.DARK_OAK_LEAVES ||
						block == Blocks.JUNGLE_LEAVES ||
						block == Blocks.OAK_LEAVES ||
						block == Blocks.SPRUCE_LEAVES
					)
				) || (
					!ignoreSnow && 
					block == Blocks.SNOW
				)
			);
			if(!(ignoreLiquid && isLiquid))
			{
				if((findSolid && isSolid) || (findLiquid && isLiquid))
				{
					return i;
				}
				if((findSolid && isLiquid) || (findLiquid && isSolid))
				{
					return -1;
				}
			}
		}
		
		// Can happen if this is a chunk filled with air
		return -1;
	}	
	
	@Override
	public int getHeightMapHeight(int x, int z)
	{
		return this.worldGenRegion.getHeight(Types.WORLD_SURFACE_WG, x, z); 
	}

	@Override
	public int getLightLevel(int x, int y, int z)
	{
		if(y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)
		{
			return -1;
		}

		// Check if the chunk has been lit, otherwise cancel.
		// TODO: Check if this causes problems with BO3 LightChecks.
		// TODO: Make a getLight method based on world.getLight that uses unloaded chunks.
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
		ChunkAccess chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		if(chunk != null && chunk.getStatus().isOrAfter(ChunkStatus.LIGHT))
		{
			// This fetches the block and skylight as if it were day.
			return this.worldGenRegion.getMaxLocalRawBrightness(new BlockPos(x, y, z));
		}
		return -1;
	}

	// TODO: Only used by resources using 3x3 decoration atm (so icebergs). Align all resources
	// to use 3x3, make them use the decoration cache and remove this method.
	@Override
	public void setBlockDirect(int x, int y, int z, LocalMaterialData material)
	{
		IBiomeConfig biomeConfig = this.getCachedBiomeProvider().getBiomeConfig(x, z, true);
		if(biomeConfig.getReplaceBlocks() != null)
		{
			material = material.parseWithBiomeAndHeight(this.getWorldConfig().getBiomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), y);
		}
		this.worldGenRegion.setBlock(new BlockPos(x, y, z), ((ForgeMaterialData)material).internalBlock(), 3);
	}

	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material)
	{
		setBlock(x, y, z, material, null, null);
	}
	
	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag nbt)
	{
		setBlock(x, y, z, material, nbt, null);
	}
	
	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, ReplaceBlockMatrix replaceBlocksMatrix)
	{
		setBlock(x, y, z, material, null, replaceBlocksMatrix);
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

		// If no decorationArea is present, we're doing something outside of the decoration cycle.
		// If a decorationArea exists, only spawn in the area being decorated.
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if(this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			if(replaceBlocksMatrix != null)
			{
				material = material.parseWithBiomeAndHeight(this.getWorldConfig().getBiomeConfigsHaveReplacement(), replaceBlocksMatrix, y);
			}

			BlockPos pos = new BlockPos(x, y, z);
			// Notify world: (2 | 16) == update client, don't update observers
			this.worldGenRegion.setBlock(pos, ((ForgeMaterialData)material).internalBlock(), 2 | 16);

			if (material.isLiquid())
			{
				this.worldGenRegion.scheduleTick(pos, ((ForgeMaterialData)material).internalBlock().getFluidState().getType(), 0);
			}
			else if (material.isMaterial(LocalMaterials.COMMAND_BLOCK))
			{
				this.worldGenRegion.scheduleTick(pos, ((ForgeMaterialData) material).internalBlock().getBlock(), 0);
			}

			if (nbt != null)
			{
				this.attachNBT(x, y, z, nbt);
			}
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
	
	public BlockEntity getTileEntity(BlockPos blockPos)
	{
		return worldGenRegion.getBlockEntity(blockPos);
	}

	// TODO: Make sure tree spawning looks more or less the same as 1.12.2.
	@Override
	@SuppressWarnings("unchecked")	
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
	{
		if(y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)
		{
			return false;
		}
		BlockPos blockPos = new BlockPos(x, y, z);
		
		// For <1.18, trees used their sapling block to check for valid spawn
		// spots. For 1.18 the code still makes it look like they do (see PlacedFeatures)
		// but instead of not placing, trees place in things like leaves but add their own block of dirt.
		// To emulate <1.18 behaviour, make sure the block is actually the highest solid block.
		// TODO: We may have to rethink non-otg tree spawning, or figure out why the sapling  
		// checks don't appear to be working.
		// TODO: Trees can get half cut off when spawning next to walls, check if vanilla also does that..
		int validY = this.getHighestBlockAboveYAt(x, z, true, false, false, true, true);
		if(validY != y)
		{
			return true;
		}
		
		try
		{
			switch (type)
			{
				case Tree:
					PlacedFeature oak = TreePlacements.OAK_CHECKED;					
					oak.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case BigTree:
					PlacedFeature fancy_oak = TreePlacements.FANCY_OAK_CHECKED;
					fancy_oak.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case Forest:
				case Birch:
					PlacedFeature birch = TreePlacements.BIRCH_CHECKED;
					birch.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case TallBirch:
					PlacedFeature tall_birch = TreePlacements.SUPER_BIRCH_BEES_0002;
					tall_birch.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case HugeMushroom:
					if (rand.nextBoolean())
					{
						ConfiguredFeature<?, ?> huge_brown_mushroom = TreeFeatures.HUGE_BROWN_MUSHROOM;
						huge_brown_mushroom.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					} else {
						ConfiguredFeature<?, ?> huge_red_mushroom = TreeFeatures.HUGE_RED_MUSHROOM;
						huge_red_mushroom.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					}
					return true;
				case HugeRedMushroom:
					ConfiguredFeature<?, ?> huge_red_mushroom = TreeFeatures.HUGE_RED_MUSHROOM;
					huge_red_mushroom.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case HugeBrownMushroom:
					ConfiguredFeature<?, ?> huge_brown_mushroom = TreeFeatures.HUGE_BROWN_MUSHROOM;
					huge_brown_mushroom.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case SwampTree:
					ConfiguredFeature<TreeConfiguration, ?> swamp_tree = TreeFeatures.SWAMP_OAK;
					swamp_tree.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case Taiga1:
					PlacedFeature pine = TreePlacements.PINE_CHECKED;
					pine.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case Taiga2:
					PlacedFeature spruce = TreePlacements.SPRUCE_CHECKED;
					spruce.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case JungleTree:
					// TODO: Apparently there's another jungle tree (non-mega)
					PlacedFeature mega_jungle_tree = TreePlacements.MEGA_JUNGLE_TREE_CHECKED;
					mega_jungle_tree.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case CocoaTree:
					PlacedFeature jungle_tree = TreePlacements.JUNGLE_TREE_CHECKED;
					jungle_tree.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case GroundBush:
					PlacedFeature jungle_bush = TreePlacements.JUNGLE_BUSH;
					jungle_bush.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case Acacia:
					PlacedFeature acacia = TreePlacements.ACACIA_CHECKED;
					acacia.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case DarkOak:
					PlacedFeature dark_oak = TreePlacements.DARK_OAK_CHECKED;
					dark_oak.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case HugeTaiga1:
					PlacedFeature mega_pine = TreePlacements.MEGA_PINE_CHECKED;
					mega_pine.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case HugeTaiga2:
					PlacedFeature mega_spruce = TreePlacements.MEGA_SPRUCE_CHECKED;
					mega_spruce.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case CrimsonFungi:
					PlacedFeature crimson_fungi = NetherPlacements.CRIMSON_FOREST_VEGETATION;
					crimson_fungi.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case WarpedFungi:
					PlacedFeature warped_fungi = NetherPlacements.WARPED_FOREST_VEGETATION;
					warped_fungi.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;
				case ChorusPlant:
					PlacedFeature chorus_plant = EndPlacements.CHORUS_PLANT;
					chorus_plant.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos);
					return true;					
				default:
					throw new RuntimeException("Failed to handle tree of type " + type.toString());
			}
		}
		catch(NullPointerException ex)
		{
			if(this.logger.getLogCategoryEnabled(LogCategory.DECORATION))
			{
				this.logger.log(LogLevel.ERROR, LogCategory.DECORATION, String.format("Treegen caused an error: ", (Object[])ex.getStackTrace()));
			}
			// Return true to prevent further attempts.
			return true;
		}
	}

	@Override
	public void spawnEntity(IEntityFunction entityData)
	{
		if (entityData.getY() < Constants.WORLD_DEPTH || entityData.getY() >= Constants.WORLD_HEIGHT)
		{
			if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Failed to spawn mob for Entity() " + entityData.makeString() + ", y position out of bounds");
			}
			return;
		}
		
		// Fetch entity type for Entity() mob name
		Entity entity = null;
		Optional<EntityType<?>> type1 = EntityType.byString(entityData.getResourceLocation().toString());
		EntityType<?> type2 = null;
		if(type1.isPresent())
		{
			type2 = type1.get();
		} else {
			if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not parse mob for Entity() " + entityData.makeString() + ", mob type could not be found.");
			}
			return;
		}
		
		// Check for any .txt or .nbt file containing nbt data for the entity
		CompoundTag nbtTagCompound = null;
		if(
			entityData.getNameTagOrNBTFileName() != null &&
			(
				entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".txt")
				|| entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".nbt")
			)
		)
		{
			nbtTagCompound = new CompoundTag();
			if (entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".txt"))
			{
				try {
					nbtTagCompound = TagParser.parseTag(entityData.getMetaData());
				} catch (CommandSyntaxException e) {
					if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not parse nbt for Entity() " + entityData.makeString() + ", file: " + entityData.getNameTagOrNBTFileName());
					}
					return;
				}
				// Specify which type of entity to spawn
				nbtTagCompound.putString("id", entityData.getResourceLocation());
			}
			else if (entityData.getNBTTag() != null)
			{
				nbtTagCompound = ForgeNBTHelper.getNMSFromNBTTagCompound(entityData.getNBTTag());
			}
		}
		
		if(nbtTagCompound == null)
		{
			// Create entity without nbt data
			try {
				entity = type2.create(this.worldGenRegion.getLevel());
			} catch (Exception exception) {
				if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not create entity for Entity() " + entityData.makeString() + ", exception: " + exception.getMessage());
				}
				return;
			}
			if (entity == null)
			{
				if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not create entity for Entity() " + entityData.makeString() + ", MC returned null.");
				}
				return;
			} else {
				entity.moveTo(entityData.getX(), entityData.getY(), entityData.getZ(), this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
			}
		} else {
			// Create entity with nbt data
			try
			{
				entity = EntityType.loadEntityRecursive(nbtTagCompound, this.worldGenRegion.getLevel(), (entity1) ->
				{
					entity1.moveTo(entityData.getX(), entityData.getY(), entityData.getZ(), this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
					return entity1;
				});
			}
			catch(Exception ex) { }
			if (entity == null)
			{
				if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not create entity for Entity() " + entityData.makeString() + ", MC returned null.");
				}
				return;
			}
		}
		// Create and spawn entities according to group size
		for (int r = 0; r < entityData.getGroupSize(); r++)
		{
			if(r != 0)
			{
				if(nbtTagCompound == null)
				{
					// Create entity without nbt data
					try {
						entity = type2.create(this.worldGenRegion.getLevel());
					} catch (Exception exception) {
						return;
					}
					if (entity == null)
					{
						return;
					} else {
						entity.moveTo(entityData.getX(), entityData.getY(), entityData.getZ(), this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
					}
				} else {
					// Create entity with nbt data
					entity = EntityType.loadEntityRecursive(nbtTagCompound, this.worldGenRegion.getLevel(), (entity1) -> {
						entity1.moveTo(entityData.getX(), entityData.getY(), entityData.getZ(), this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
						return entity1;
					});
				}
				if (entity == null)
				{
					return;
				}
			}
			
			// TODO: Non-mob entities, aren't those handled via Block(nbt), chests, armor stands etc?
			if (entity instanceof Mob)
			{
				// If the block is a solid block or entity is a fish out of water, cancel
				LocalMaterialData block = ForgeMaterialData.ofBlockState(this.worldGenRegion.getBlockState(new BlockPos(entityData.getX(), entityData.getY(), entityData.getZ())));
				if (
					block.isSolid() ||
					(
						(
							entity.getClassification(false) == MobCategory.WATER_CREATURE
							|| entity instanceof Guardian
						)
						&& !block.isLiquid()
					)
				)
				{
					if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not spawn entity at " + entityData.getX() + " " + entityData.getY() + " " + entityData.getZ() + " for Entity() " + entityData.makeString() + ", a solid block was found or a water mob tried to spawn outside of water.");
					}
					continue;
				}
				
				// Appease Forge
				Mob mobentity = (Mob)entity;
				if (net.minecraftforge.common.ForgeHooks.canEntitySpawn(mobentity, this.worldGenRegion, entityData.getX(), entityData.getY(), entityData.getZ(), null, MobSpawnType.CHUNK_GENERATION) == -1)
				{
					if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Forge prevented spawning for Entity() " + entityData.makeString() + ", a mod or setting is likely preventing mob spawns.");
					}
					continue;
				}
				
				// Attach nametag if one was provided via Entity()
				String nameTag = entityData.getNameTagOrNBTFileName();
				if (nameTag != null && !nameTag.toLowerCase().trim().endsWith(".txt") && !nameTag.toLowerCase().trim().endsWith(".nbt"))
				{
					entity.setCustomName(new TextComponent(nameTag));
				}
				// Make sure Entity() mobs don't de-spawn, regardless of nbt data
				mobentity.setPersistenceRequired();
				
				SpawnGroupData ilivingentitydata = null;
				ilivingentitydata = mobentity.finalizeSpawn(this.worldGenRegion, this.worldGenRegion.getCurrentDifficultyAt(new BlockPos(entityData.getX(), entityData.getY(), entityData.getZ())), MobSpawnType.CHUNK_GENERATION, ilivingentitydata, nbtTagCompound);
				this.worldGenRegion.addFreshEntityWithPassengers(mobentity);
			}
		}
	}

	@Override
	public void placeDungeon(Random random, int x, int y, int z)
	{
		Feature.MONSTER_ROOM.configured(FeatureConfiguration.NONE).place(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(x, y, z));
	}

	@Override
	public void placeFossil(Random random, int x, int y, int z)
	{
		if(y >= 0)
		{
			CavePlacements.FOSSIL_UPPER.place(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(x, y, z));
		} else {
			CavePlacements.FOSSIL_LOWER.place(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(x, y, z));
		}
	}
	
	@Override
	public boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate)
	{
		// TODO: Implement this.
		return true;
	}

	// Edit command
	// TODO: We already have getMaterial/setBlock, rename/refactor these
	// so it's clear they are/should be used only in a specific context.

	public void setBlockState(BlockPos blockpos, BlockState blockstate1, int i)
	{
		worldGenRegion.setBlock(blockpos, blockstate1, i);
	}

	public BlockState getBlockState(BlockPos blockPos)
	{
		return worldGenRegion.getBlockState(blockPos);
	}	
	
	// Shadowgen
	
	@Override
	public LocalMaterialData getMaterialWithoutLoading(int x, int y, int z)
	{
		if (y >= Constants.WORLD_HEIGHT || y < Constants.WORLD_DEPTH)
		{
			return null;
		}

		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		// If the chunk exists or is inside the area being decorated, fetch it normally.
		ChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if(this.decorationArea != null && this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}

		// If the chunk doesn't exist so we're doing something outside the
		// decoration sequence, return the material without loading the chunk.
		if((chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS)))
		{
			return this.chunkGenerator.getMaterialInUnloadedChunk(this.getWorldRandom(), x , y, z, this.worldGenRegion.getLevel());
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		return ForgeMaterialData.ofBlockState(chunk.getBlockState(new BlockPos(internalX, y, internalZ)));
	}	
	
	@Override
	public int getHighestBlockYAtWithoutLoading(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
		
		// If the chunk exists or is inside the area being decorated, fetch it normally.
		ChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if(this.decorationArea != null && this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}
		
		// If the chunk doesn't exist and we're doing something outside the
		// decoration sequence, return the material without loading the chunk.
		if((chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS)))
		{
			return this.chunkGenerator.getHighestBlockYInUnloadedChunk(this.getWorldRandom(), x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow, this.worldGenRegion.getLevel());
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		int heightMapy = chunk.getHeight(Types.WORLD_SURFACE_WG, internalX, internalZ);	
		return getHighestBlockYAt(chunk, internalX, heightMapy, internalZ, findSolid, findLiquid, ignoreLiquid, ignoreSnow, ignoreLeaves);
	}	
	
	@Override
	public boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate)
	{
		Boolean hasDefaultStructure = cachedHasDefaultStructureChunks.get(chunkCoordinate);
		if(hasDefaultStructure != null)
		{
			return hasDefaultStructure.booleanValue();
		}
		hasDefaultStructure = this.chunkGenerator.checkHasVanillaStructureWithoutLoading(this.worldGenRegion.getLevel(), chunkCoordinate);
		cachedHasDefaultStructureChunks.put(chunkCoordinate, hasDefaultStructure);
		return hasDefaultStructure;
	}
}
