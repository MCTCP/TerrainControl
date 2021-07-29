package com.pg85.otg.spigot.gen;

import com.google.gson.JsonSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.IEntityFunction;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.spigot.util.JsonToNBT;
import com.pg85.otg.spigot.util.SpigotNBTHelper;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.minecraft.TreeType;
import com.pg85.otg.util.nbt.NamedBinaryTag;

import net.minecraft.server.v1_16_R3.*;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Random;

// TODO: Split up worldgenregion into separate classes, one for decoration/worldgen, one for non-worldgen.
public class SpigotWorldGenRegion extends LocalWorldGenRegion
{
	protected final GeneratorAccessSeed worldGenRegion;
	private final OTGNoiseChunkGenerator chunkGenerator;

	// BO4 plotting may call hasDefaultStructures on chunks outside the area being decorated, in order to plot large structures.
	// It may query the same chunk multiple times, so use a fixed size cache.
	private FifoMap<ChunkCoordinate, Boolean> cachedHasDefaultStructureChunks = new FifoMap<ChunkCoordinate, Boolean>(2048);

	/** Creates a LocalWorldGenRegion to be used during decoration for OTG worlds. */
	public SpigotWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, RegionLimitedWorldAccess worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger(), worldGenRegion.a(), worldGenRegion.b(), chunkGenerator.getCachedBiomeProvider());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}

	/** Creates a LocalWorldGenRegion to be used for OTG worlds outside of decoration, only used for /otg spawn/edit/export. */
	public SpigotWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, GeneratorAccessSeed worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}
	
	/** Creates a LocalWorldGenRegion to be used for non-OTG worlds outside of decoration, only used for /otg spawn/edit/export. */
	public SpigotWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, GeneratorAccessSeed worldGenRegion)
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
	public ICachedBiomeProvider getCachedBiomeProvider()
	{
		return this.chunkGenerator.getCachedBiomeProvider();
	}
	
	@Override
	public ChunkCoordinate getSpawnChunk()
	{
		BlockPosition spawnPos = this.worldGenRegion.getMinecraftWorld().getSpawn();
		return ChunkCoordinate.fromBlockCoords(spawnPos.getX(), spawnPos.getZ());
	}

	public GeneratorAccessSeed getInternal()
	{
		return this.worldGenRegion;
	}

	@Override
	public IBiome getBiomeForDecoration(int x, int z)
	{
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		return this.decorationArea != null ? this.decorationBiomeCache.getBiome(x, z) : this.getCachedBiomeProvider().getBiome(x, z);
	}

	@Override
	public IBiomeConfig getBiomeConfigForDecoration(int x, int z)
	{
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		return this.decorationArea != null ? this.decorationBiomeCache.getBiomeConfig(x, z) : this.getCachedBiomeProvider().getBiomeConfig(x, z);
	}

	@Override
	public double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld)
	{
		return this.chunkGenerator.getBiomeBlocksNoiseValue(xInWorld, zInWorld);
	}

	// TODO: Only used by resources using 3x3 decoration atm (so icebergs). Align all resources
	// to use 3x3, make them use the decoration cache and remove this method.
	@Override
	public LocalMaterialData getMaterialDirect(int x, int y, int z)
	{
		return SpigotMaterialData.ofBlockData(this.worldGenRegion.getType(new BlockPosition(x, y, z)));
	}
	
	@Override
	public LocalMaterialData getMaterial(int x, int y, int z)
	{
		if (y >= Constants.WORLD_HEIGHT || y < Constants.WORLD_DEPTH)
		{
			return null;
		}

		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		// If the chunk exists or is inside the area being decorated, fetch it normally.
		IChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if (this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}

		// Tried to query an unloaded chunk outside the area being decorated
		if (chunk == null || !chunk.getChunkStatus().b(ChunkStatus.LIQUID_CARVERS))
		{
			return null;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		return SpigotMaterialData.ofBlockData(chunk.getType(new BlockPosition(internalX, y, internalZ)));
	}

	@Override
	public int getBlockAboveLiquidHeight (int x, int z)
	{
		int highestY = getHighestBlockYAt(x, z, false, true, false, false, false);
		if (highestY >= 0)
		{
			return highestY + 1;
		} else {
			return -1;
		}
	}

	@Override
	public int getBlockAboveSolidHeight (int x, int z)
	{
		int highestY = getHighestBlockYAt(x, z, true, false, true, true, false);
		if (highestY >= 0)
		{
			return highestY + 1;
		} else {
			return -1;
		}
	}

	@Override
	public int getHighestBlockAboveYAt (int x, int z)
	{
		int highestY = getHighestBlockYAt(x, z, true, true, false, false, false);
		if (highestY >= 0)
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
		IChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if (this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}

		// Tried to query an unloaded chunk outside the area being decorated
		if (chunk == null || !chunk.getChunkStatus().b(ChunkStatus.LIQUID_CARVERS))
		{
			return -1;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		int heightMapY = chunk.getHighestBlock(HeightMap.Type.WORLD_SURFACE, internalX, internalZ);
		return getHighestBlockYAt(chunk, internalX, heightMapY, internalZ, findSolid, findLiquid, ignoreLiquid, ignoreSnow, ignoreLeaves);		
	}

	protected int getHighestBlockYAt(IChunkAccess chunk, int internalX, int heightMapY, int internalZ, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		LocalMaterialData material;
		boolean isSolid;
		boolean isLiquid;
		IBlockData blockState;
		Block block;

		for (int i = heightMapY; i >= 0; i--)
		{
			blockState = chunk.getType(new BlockPosition(internalX, i, internalZ));
			block = blockState.getBlock();
			material = SpigotMaterialData.ofBlockData(blockState);
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
			if (!(ignoreLiquid && isLiquid))
			{
				if ((findSolid && isSolid) || (findLiquid && isLiquid))
				{
					return i;
				}
				if ((findSolid && isLiquid) || (findLiquid && isSolid))
				{
					return -1;
				}
			}
		}

		// Can happen if this is a chunk filled with air
		return -1;
	}	

	@Override
	public int getHeightMapHeight (int x, int z)
	{
		return this.worldGenRegion.a(HeightMap.Type.WORLD_SURFACE_WG, x, z);
	}

	@Override
	public int getLightLevel (int x, int y, int z)
	{
		if (y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)
		{
			return -1;
		}

		// Check if the chunk has been lit, otherwise cancel.
		// TODO: Check if this causes problems with BO3 LightChecks.
		// TODO: Make a getLight method based on world.getLight that uses unloaded chunks.
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
		IChunkAccess chunk = this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		if (chunk != null && chunk.getChunkStatus().b(ChunkStatus.LIGHT))
		{
			// This fetches the block and skylight as if it were day.
			return this.worldGenRegion.getLightLevel(new BlockPosition(x, y, z));
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
		this.worldGenRegion.setTypeAndData(new BlockPosition(x, y, z), ((SpigotMaterialData)material).internalBlock(), 3);
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

			BlockPosition pos = new BlockPosition(x, y, z);
			// Notify world: (2 | 16) == update client, don't update observers
			this.worldGenRegion.setTypeAndData(pos, ((SpigotMaterialData) material).internalBlock(), 2 | 16);

			if (material.isLiquid())
			{
				this.worldGenRegion.getFluidTickList().a(pos, ((SpigotMaterialData)material).internalBlock().getFluid().getType(), 0);
			}
			else if (material.isMaterial(LocalMaterials.COMMAND_BLOCK))
			{
				this.worldGenRegion.getBlockTickList().a(pos, ((SpigotMaterialData)material).internalBlock().getBlock(), 0);
			}

			if (nbt != null)
			{
				this.attachNBT(x, y, z, nbt, worldGenRegion.getType(pos));
			}
		}
	}

	protected void attachNBT(int x, int y, int z, NamedBinaryTag nbt, IBlockData state)
	{
		NBTTagCompound nms = SpigotNBTHelper.getNMSFromNBTTagCompound(nbt);
		nms.set("x", NBTTagInt.a(x));
		nms.set("y", NBTTagInt.a(y));
		nms.set("z", NBTTagInt.a(z));

		TileEntity tileEntity = this.worldGenRegion.getTileEntity(new BlockPosition(x, y, z));
		if (tileEntity != null)
		{
			try {
				tileEntity.load(state, nms);
			}
			catch (JsonSyntaxException e)
			{
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
	
	public TileEntity getTileEntity(BlockPosition blockPos)
	{
		return worldGenRegion.getTileEntity(blockPos);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
	{
		if (y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)
		{
			return false;
		}
		BlockPosition blockPos = new BlockPosition(x, y, z);
		try
		{
			// Features -> BiomeDecoratorGroups
			// ConfiguredFeature.feature -> WorldGenFeatureConfigured.e
			// ConfiguredFeature.config -> WorldGenFeatureConfigured.f
			WorldGenFeatureConfigured<WorldGenFeatureTreeConfiguration, ?> tree = null;
			WorldGenFeatureConfigured<WorldGenFeatureConfiguration, ?> other = null;
			switch (type)
			{
				case Acacia:
					tree = BiomeDecoratorGroups.ACACIA;
					break;
				case BigTree:
					tree = BiomeDecoratorGroups.FANCY_OAK;
					break;
				case Forest:
				case Birch:
					tree = BiomeDecoratorGroups.BIRCH;
					break;
				case JungleTree:
					tree = BiomeDecoratorGroups.MEGA_JUNGLE_TREE;
					break;
				case CocoaTree:
					tree = BiomeDecoratorGroups.JUNGLE_TREE;
					break;
				case DarkOak:
					tree = BiomeDecoratorGroups.DARK_OAK;
					break;
				case GroundBush:
					other = (WorldGenFeatureConfigured<WorldGenFeatureConfiguration, ?>) BiomeDecoratorGroups.JUNGLE_BUSH;
					break;
				case HugeMushroom:
					if (rand.nextBoolean())
					{
						other = (WorldGenFeatureConfigured<WorldGenFeatureConfiguration, ?>) BiomeDecoratorGroups.HUGE_BROWN_MUSHROOM;
					}
					else
					{
						other = (WorldGenFeatureConfigured<WorldGenFeatureConfiguration, ?>) BiomeDecoratorGroups.HUGE_RED_MUSHROOM;
					}
					break;
				case HugeRedMushroom:
					other = (WorldGenFeatureConfigured<WorldGenFeatureConfiguration, ?>) BiomeDecoratorGroups.HUGE_RED_MUSHROOM;
					break;
				case HugeBrownMushroom:
					other = (WorldGenFeatureConfigured<WorldGenFeatureConfiguration, ?>) BiomeDecoratorGroups.HUGE_BROWN_MUSHROOM;
					break;
				case SwampTree:
					other = (WorldGenFeatureConfigured<WorldGenFeatureConfiguration, ?>) BiomeDecoratorGroups.SWAMP_TREE;
					break;
				case Taiga1:
					tree = BiomeDecoratorGroups.PINE;
					break;
				case Taiga2:
					tree = BiomeDecoratorGroups.SPRUCE;
					break;
				case HugeTaiga1:
					tree = BiomeDecoratorGroups.MEGA_PINE;
					break;
				case HugeTaiga2:
					tree = BiomeDecoratorGroups.MEGA_SPRUCE;
					break;
				case TallBirch:
					tree = BiomeDecoratorGroups.SUPER_BIRCH_BEES_0002;
					break;
				case Tree:
					tree = BiomeDecoratorGroups.OAK;
					break;
				default:
					throw new RuntimeException("Failed to handle tree of type " + type.toString());
			}
			if (tree != null)
			{
				tree.e.generate(this.worldGenRegion, this.chunkGenerator, rand, blockPos, tree.f);
			}
			else if (other != null)
			{
				other.e.generate(this.worldGenRegion, this.chunkGenerator, rand, blockPos, other.f);
			} else {
				throw new RuntimeException("Incorrect handling of tree of type " + type.toString());
			}
			return true;
		}
		catch (NullPointerException ex)
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
	public void spawnEntity (IEntityFunction entityData)
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
		Optional<EntityTypes<?>> type1 = EntityTypes.a(entityData.getResourceLocation().toString());
		EntityTypes<?> type2 = null;
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
		NBTTagCompound nbtTagCompound = null;
		if(
			entityData.getNameTagOrNBTFileName() != null &&
			(
				entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".txt")
				|| entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".nbt")
			)
		)
		{
			nbtTagCompound = new NBTTagCompound();
			if(entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".txt"))
			{
				try {
					nbtTagCompound = JsonToNBT.getTagFromJson(entityData.getMetaData());
				} catch (Exception e) {
					if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not parse nbt for Entity() " + entityData.makeString() + ", file: " + entityData.getNameTagOrNBTFileName());
					}
					return;
				}
				// Specify which type of entity to spawn
				nbtTagCompound.setString("id", entityData.getResourceLocation());
			}
			else if (entityData.getNBTTag() != null)
			{
				nbtTagCompound = SpigotNBTHelper.getNMSFromNBTTagCompound(entityData.getNBTTag());
			}
		}
		
		if(nbtTagCompound == null)
		{
			// Create entity without nbt data
			try {
				entity = type2.a(this.worldGenRegion.getMinecraftWorld());
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
				entity.setPositionRotation(entityData.getX(), entityData.getY(), entityData.getZ(), this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
			}
		} else {
			// Create entity with nbt data
			entity = EntityTypes.a(nbtTagCompound, this.worldGenRegion.getMinecraftWorld(), (entity1) ->
			{
				entity1.setPositionRotation(entityData.getX(), entityData.getY(), entityData.getZ(), this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
				return entity1;
			});
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
						entity = type2.a(this.worldGenRegion.getMinecraftWorld());
					} catch (Exception exception) {
						return;
					}
					if (entity == null)
					{
						return;
					} else {
						entity.setPositionRotation(entityData.getX(), entityData.getY(), entityData.getZ(), this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
					}
				} else {
					// Create entity with nbt data
					entity = EntityTypes.a(nbtTagCompound, this.worldGenRegion.getMinecraftWorld(), (entity1) ->
					{
						entity1.setPositionRotation(entityData.getX(), entityData.getY(), entityData.getZ(), this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
						return entity1;
					});
				}
				if (entity == null)
				{
					return;
				}
			}
		
			// TODO: Non-mob entities, aren't those handled via Block(nbt), chests, armor stands etc?
			if (entity instanceof EntityInsentient)
			{
				// If the block is a solid block or entity is a fish out of water, cancel
				LocalMaterialData block = SpigotMaterialData.ofBlockData(this.worldGenRegion.getType(new BlockPosition(entityData.getX(), entityData.getY(), entityData.getZ())));
				if (
					block.isSolid() ||
					(
						(
							((EntityInsentient)entity).getMonsterType() == EnumMonsterType.WATER_MOB
							|| entity instanceof EntityGuardian
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
				
				EntityInsentient mobEntity = (EntityInsentient)entity;
				
				// Attach nametag if one was provided via Entity()
				String nameTag = entityData.getNameTagOrNBTFileName();
				if (nameTag != null && !nameTag.toLowerCase().trim().endsWith(".txt") && !nameTag.toLowerCase().trim().endsWith(".nbt"))
				{
					entity.setCustomName(new ChatComponentText(nameTag));
				}
				// Make sure Entity() mobs don't de-spawn, regardless of nbt data
				mobEntity.setPersistent();

				GroupDataEntity ilivingentitydata = null;
				ilivingentitydata = mobEntity.prepare(this.worldGenRegion, this.worldGenRegion.getDamageScaler(new BlockPosition(entityData.getX(), entityData.getY(), entityData.getZ())), EnumMobSpawn.CHUNK_GENERATION, ilivingentitydata, nbtTagCompound);
				this.worldGenRegion.addAllEntities(mobEntity);
			}
		}
	}

	@Override
	public void placeDungeon (Random random, int x, int y, int z)
	{
		WorldGenerator.MONSTER_ROOM.b(WorldGenFeatureConfiguration.k).a(this.worldGenRegion, this.chunkGenerator, random, new BlockPosition(x, y, z));		
	}

	@Override
	public void placeFossil(Random random, int x, int y, int z)
	{
		WorldGenerator.FOSSIL.b(WorldGenFeatureConfiguration.k).a(this.worldGenRegion, this.chunkGenerator, random, new BlockPosition(x, y, z));
	}

	@Override
	public void placeFromRegistry(Random random, ChunkCoordinate chunkCoord, String id)
	{
		IRegistryCustom registries = this.worldGenRegion.getMinecraftWorld().r();
		IRegistry<WorldGenFeatureConfigured<?, ?>> registry = registries.b(IRegistry.au);
		Optional<WorldGenFeatureConfigured<?, ?>> feature = registry.getOptional(new MinecraftKey(id));

		if (feature.isPresent())
		{
			feature.get().a(this.worldGenRegion, this.chunkGenerator, random, new BlockPosition(chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ()));
		} else {
			if(this.logger.getLogCategoryEnabled(LogCategory.DECORATION))
			{
				this.logger.log(LogLevel.ERROR, LogCategory.DECORATION, "Unable to find registry object " + id);
			}
		}
	}

	@Override
	public boolean isInsideWorldBorder (ChunkCoordinate chunkCoordinate)
	{
		// TODO: Implement this.
		return true;
	}
	
	// Edit command
	// TODO: We already have getMaterial/setBlock, rename/refactor these
	// so it's clear they are/should be used only in a specific context.	

	public IBlockData getBlockData(BlockPosition blockpos)
	{
		return this.worldGenRegion.getType(blockpos);
	}

	public void setBlockState(BlockPosition blockpos, IBlockData blockstate1, int i)
	{
		this.worldGenRegion.setTypeAndData(blockpos, blockstate1, i);
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
		IChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if (this.decorationArea != null && this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}
		// isAtLeast() -> b()
		if ((chunk == null || !chunk.getChunkStatus().b(ChunkStatus.LIQUID_CARVERS)))
		{
			return this.chunkGenerator.getMaterialInUnloadedChunk(this.getWorldRandom(), x, y, z);
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		return SpigotMaterialData.ofBlockData(chunk.getType(new BlockPosition(internalX, y, internalZ)));
	}	
	
	@Override
	public int getHighestBlockYAtWithoutLoading(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		// If the chunk exists or is inside the area being decorated, fetch it normally.
		IChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if (this.decorationArea != null && this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}

		// If the chunk doesn't exist and we're doing something outside the
		// decoration sequence, return the material without loading the chunk.
		if ((chunk == null || !chunk.getChunkStatus().b(ChunkStatus.LIQUID_CARVERS)))
		{
			return this.chunkGenerator.getHighestBlockYInUnloadedChunk(this.getWorldRandom(), x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		int heightMapY = chunk.getHighestBlock(HeightMap.Type.WORLD_SURFACE_WG, internalX, internalZ);
		return getHighestBlockYAt(chunk, internalX, heightMapY, internalZ, findSolid, findLiquid, ignoreLiquid, ignoreSnow, ignoreLeaves);
	}

	@Override
	public boolean chunkHasDefaultStructure (Random worldRandom, ChunkCoordinate chunkCoordinate)
	{
		Boolean hasDefaultStructure = cachedHasDefaultStructureChunks.get(chunkCoordinate);
		if(hasDefaultStructure != null)
		{
			return hasDefaultStructure;
		}
		hasDefaultStructure = this.chunkGenerator.checkHasVanillaStructureWithoutLoading(this.worldGenRegion.getMinecraftWorld(), chunkCoordinate);
		cachedHasDefaultStructureChunks.put(chunkCoordinate, hasDefaultStructure);
		return hasDefaultStructure;
	}
}
