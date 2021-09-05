package com.pg85.otg.paper.gen;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Random;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import org.bukkit.HeightMap;
import org.bukkit.block.data.BlockData;

import com.google.gson.JsonSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.IEntityFunction;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.paper.materials.PaperMaterialData;
import com.pg85.otg.paper.util.JsonToNBT;
import com.pg85.otg.paper.util.PaperNBTHelper;
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

import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Features;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent;

// TODO: Split up worldgenregion into separate classes, one for decoration/worldgen, one for non-worldgen.
public class PaperWorldGenRegion extends LocalWorldGenRegion
{
	protected final WorldGenLevel worldGenRegion;
	private final OTGNoiseChunkGenerator chunkGenerator;

	// BO4 plotting may call hasDefaultStructures on chunks outside the area being decorated, in order to plot large structures.
	// It may query the same chunk multiple times, so use a fixed size cache.
	private final FifoMap<ChunkCoordinate, Boolean> cachedHasDefaultStructureChunks = new FifoMap<>(2048);

	/** Creates a LocalWorldGenRegion to be used during decoration for OTG worlds. */
	public PaperWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, WorldGenRegion worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger(), worldGenRegion.getCenter().x, worldGenRegion.getCenter().z, chunkGenerator.getCachedBiomeProvider());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}

	/** Creates a LocalWorldGenRegion to be used for OTG worlds outside of decoration, only used for /otg spawn/edit/export. */
	public PaperWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, WorldGenLevel worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}
	
	/** Creates a LocalWorldGenRegion to be used for non-OTG worlds outside of decoration, only used for /otg spawn/edit/export. */
	public PaperWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, WorldGenLevel worldGenRegion)
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
		BlockPos spawnPos = this.worldGenRegion.getMinecraftWorld().getSharedSpawnPos();
		return ChunkCoordinate.fromBlockCoords(spawnPos.getX(), spawnPos.getZ());
	}

	public LevelAccessor getInternal()
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
		return PaperMaterialData.ofBlockData(this.worldGenRegion.getBlockState(new BlockPos(x, y, z)));
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
		ChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if (this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.getChunkIfLoadedImmediately(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
		}

		// Tried to query an unloaded chunk outside the area being decorated
		if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS))
		{
			return null;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		return PaperMaterialData.ofBlockData(chunk.getType(internalX, y, internalZ));
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
		ChunkAccess chunk = null;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.		
		if (this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.getChunkIfLoadedImmediately(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
		}

		// Tried to query an unloaded chunk outside the area being decorated
		if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS))
		{
			return -1;
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		int heightMapY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, internalX, internalZ);
		return getHighestBlockYAt(chunk, internalX, heightMapY, internalZ, findSolid, findLiquid, ignoreLiquid, ignoreSnow, ignoreLeaves);		
	}

	protected int getHighestBlockYAt(ChunkAccess chunk, int internalX, int heightMapY, int internalZ, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		LocalMaterialData material;
		boolean isSolid;
		boolean isLiquid;
		BlockState blockState;
		Block block;

		for (int i = heightMapY; i >= 0; i--)
		{
			blockState = chunk.getBlockState(new BlockPos(internalX, i, internalZ));
			block = blockState.getBlock();
			material = PaperMaterialData.ofBlockData(blockState);
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
		return this.worldGenRegion.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
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
		ChunkAccess chunk = this.worldGenRegion.getChunkIfLoadedImmediately(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
		if (chunk != null && chunk.getStatus().isOrAfter(ChunkStatus.LIGHT))
		{
			// This fetches the block and skylight as if it were day.
			return this.worldGenRegion.getLightEmission(new BlockPos(x, y, z));
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
		this.worldGenRegion.setBlock(new BlockPos(x, y, z), ((PaperMaterialData)material).internalBlock(), 3);
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

			BlockPos pos = new BlockPos(x, y, z);
			// Notify world: (2 | 16) == update client, don't update observers
			this.worldGenRegion.setBlock(pos, ((PaperMaterialData) material).internalBlock(), 2 | 16);

			if (material.isLiquid())
			{
				this.worldGenRegion.getLiquidTicks().scheduleTick(pos, ((PaperMaterialData)material).internalBlock().getFluidState().getType(), 0);
			}
			else if (material.isMaterial(LocalMaterials.COMMAND_BLOCK))
			{
				this.worldGenRegion.getBlockTicks().scheduleTick(pos, ((PaperMaterialData)material).internalBlock().getBlock(), 0);
			}

			if (nbt != null)
			{
				this.attachNBT(x, y, z, nbt);
			}
		}
	}

	protected void attachNBT(int x, int y, int z, NamedBinaryTag nbt)
	{
		CompoundTag nms = PaperNBTHelper.getNMSFromNBTTagCompound(nbt);
		nms.put("x", IntTag.valueOf(x));
		nms.put("y", IntTag.valueOf(y));
		nms.put("z", IntTag.valueOf(z));

		BlockEntity tileEntity = this.worldGenRegion.getBlockEntity(new BlockPos(x, y, z));
		if (tileEntity != null)
		{
			try {
				// TODO: Check that this doesn't break anything
				//tileEntity.load(state, nms);
				tileEntity.load(nms);
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
	
	public BlockEntity getTileEntity(BlockPos blockPos)
	{
		return worldGenRegion.getBlockEntity(blockPos);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
	{
		if (y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)
		{
			return false;
		}
		BlockPos blockPos = new BlockPos(x, y, z);
		try
		{
			// Features -> BiomeDecoratorGroups
			// ConfiguredFeature.feature -> WorldGenFeatureConfigured.e
			// ConfiguredFeature.config -> WorldGenFeatureConfigured.f
			ConfiguredFeature<TreeConfiguration, ?> tree = null;
			ConfiguredFeature<FeatureConfiguration, ?> other = null;
			switch (type)
			{
				case Acacia:
					tree = Features.ACACIA;
					break;
				case BigTree:
					tree = Features.FANCY_OAK;
					break;
				case Forest:
				case Birch:
					tree = Features.BIRCH;
					break;
				case JungleTree:
					tree = Features.MEGA_JUNGLE_TREE;
					break;
				case CocoaTree:
					tree = Features.JUNGLE_TREE;
					break;
				case DarkOak:
					tree = Features.DARK_OAK;
					break;
				case GroundBush:
					tree = Features.JUNGLE_BUSH;
					break;
				case HugeMushroom:
					if (rand.nextBoolean())
					{
						other = (ConfiguredFeature<FeatureConfiguration, ?>) Features.HUGE_BROWN_MUSHROOM;
					} else {
						other = (ConfiguredFeature<FeatureConfiguration, ?>) Features.HUGE_RED_MUSHROOM;
					}
					break;
				case HugeRedMushroom:
					other = (ConfiguredFeature<FeatureConfiguration, ?>) Features.HUGE_RED_MUSHROOM;
					break;
				case HugeBrownMushroom:
					other = (ConfiguredFeature<FeatureConfiguration, ?>) Features.HUGE_BROWN_MUSHROOM;
					break;
				case SwampTree:
					tree = Features.SWAMP_OAK;
					break;
				case Taiga1:
					tree = Features.PINE;
					break;
				case Taiga2:
					tree = Features.SPRUCE;
					break;
				case HugeTaiga1:
					tree = Features.MEGA_PINE;
					break;
				case HugeTaiga2:
					tree = Features.MEGA_SPRUCE;
					break;
				case TallBirch:
					tree = Features.SUPER_BIRCH_BEES_0002;
					break;
				case Tree:
					tree = Features.OAK;
					break;
				default:
					throw new RuntimeException("Failed to handle tree of type " + type);
			}
			if (tree != null)
			{
				tree.feature.place(new FeaturePlaceContext<>(this.worldGenRegion, this.chunkGenerator, rand, blockPos, tree.config));
			}
			else if (other != null)
			{
				other.feature.place(new FeaturePlaceContext<>(this.worldGenRegion, this.chunkGenerator, rand, blockPos, other.config));
			} else {
				throw new RuntimeException("Incorrect handling of tree of type " + type);
			}
			return true;
		}
		catch (NullPointerException ex)
		{
			if(this.logger.getLogCategoryEnabled(LogCategory.DECORATION))
			{
				this.logger.log(LogLevel.ERROR, LogCategory.DECORATION, "Treegen caused an error: ");
				this.logger.printStackTrace(LogLevel.ERROR, LogCategory.DECORATION, ex);
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
		Optional<EntityType<?>> optionalType = EntityType.byString(entityData.getResourceLocation());
		EntityType<?> type;
		if(optionalType.isPresent())
		{
			type = optionalType.get();
		} else {
			if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not parse mob for Entity() " + entityData.makeString() + ", mob type could not be found.");
			}
			return;
		}
		
		// Check for any .txt or .nbt file containing nbt data for the entity
		CompoundTag compoundTag = null;
		if(
			entityData.getNameTagOrNBTFileName() != null &&
			(
				entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".txt")
				|| entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".nbt")
			)
		)
		{
			compoundTag = new CompoundTag();
			if(entityData.getNameTagOrNBTFileName().toLowerCase().trim().endsWith(".txt"))
			{

				compoundTag = JsonToNBT.getTagFromJson(entityData.getMetaData());
				if (compoundTag == null)
				{
					if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not parse nbt for Entity() " + entityData.makeString() + ", file: " + entityData.getNameTagOrNBTFileName());
					}
					return;
				}
				// Specify which type of entity to spawn
				compoundTag.putString("id", entityData.getResourceLocation());
			}
			else if (entityData.getNBTTag() != null)
			{
				compoundTag = PaperNBTHelper.getNMSFromNBTTagCompound(entityData.getNBTTag());
			}
		}

		// Create and spawn entities according to group size
		for (int r = 0; r < entityData.getGroupSize(); r++)
		{
			Entity entity = type.create(this.worldGenRegion.getMinecraftWorld());
			if (entity == null)
			{
				if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Failed to make basic entity for " + entityData.makeString());
				}
				return;
			}
			if (compoundTag != null)
			{
				entity.load(compoundTag);
			}
			entity.setRot(this.getWorldRandom().nextFloat() * 360.0F, 0.0F);
			entity.setPos(entityData.getX(), entityData.getY(), entityData.getZ());

			// Attach nametag if one was provided via Entity()
			String nameTag = entityData.getNameTagOrNBTFileName();
			if (nameTag != null && !nameTag.toLowerCase().trim().endsWith(".txt") && !nameTag.toLowerCase().trim().endsWith(".nbt"))
			{
				entity.setCustomName(new TextComponent(nameTag));
			}
		
			// TODO: Non-mob entities, aren't those handled via Block(nbt), chests, armor stands etc?
			if (entity instanceof LivingEntity)
			{
				// If the block is a solid block or entity is a fish out of water, cancel
				LocalMaterialData block = PaperMaterialData.ofBlockData(this.worldGenRegion.getBlockState(new BlockPos(entityData.getX(), entityData.getY(), entityData.getZ())));
				if (
					block.isSolid() ||
					(
						((LivingEntity) entity).getMobType() == MobType.WATER
						&& !block.isLiquid()
					)
				)
				{
					if (this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						this.logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not spawn entity at " + entityData.getX() + " " + entityData.getY() + " " + entityData.getZ() + " for Entity() " + entityData.makeString() + ", a solid block was found or a water mob tried to spawn outside of water.");
					}
					continue;
				}

			}

			if (entity instanceof Mob mobEntity)
			{
				// Make sure Entity() mobs don't de-spawn, regardless of nbt data
				mobEntity.setPersistenceRequired();
				mobEntity.finalizeSpawn(this.worldGenRegion, this.worldGenRegion.getCurrentDifficultyAt(new BlockPos(entityData.getX(), entityData.getY(), entityData.getZ())), MobSpawnType.CHUNK_GENERATION, null, compoundTag);
			}
			this.worldGenRegion.addEntity(entity, CreatureSpawnEvent.SpawnReason.DEFAULT);
		}
	}

	@Override
	public void placeDungeon (Random random, int x, int y, int z)
	{
		Feature.MONSTER_ROOM.place(new FeaturePlaceContext<>(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(x, y, z), FeatureConfiguration.NONE));
	}

	@Override
	public void placeFossil(Random random, int x, int y, int z)
	{
		// TODO: Add customization to fossile configuration?
		Features.FOSSIL.place(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(x, y, z));
	}

	@Override
	public void placeFromRegistry(Random random, ChunkCoordinate chunkCoord, String id)
	{
		RegistryAccess registries = this.worldGenRegion.getMinecraftWorld().registryAccess();
		Registry<ConfiguredFeature<?, ?>> registry = registries.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
		Optional<ConfiguredFeature<?, ?>> feature = registry.getOptional(new ResourceLocation(id));

		if (feature.isPresent())
		{
			feature.get().place(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ()));
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

	public BlockState getBlockData(BlockPos blockpos)
	{
		return this.worldGenRegion.getBlockState(blockpos);
	}

	public void setBlockState(BlockPos blockpos, BlockState blockstate1, int i)
	{
		this.worldGenRegion.setBlock(blockpos, blockstate1, i);
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
		if (this.decorationArea != null && this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.getChunkIfLoadedImmediately(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
		}
		// isAtLeast() -> b()
		if ((chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS)))
		{
			return this.chunkGenerator.getMaterialInUnloadedChunk(this.getWorldRandom(), x, y, z);
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		return PaperMaterialData.ofBlockData(chunk.getType(internalX, y, internalZ));
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
		if (this.decorationArea != null && this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.getChunkIfLoadedImmediately(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
		}

		// If the chunk doesn't exist and we're doing something outside the
		// decoration sequence, return the material without loading the chunk.
		if ((chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS)))
		{
			return this.chunkGenerator.getHighestBlockYInUnloadedChunk(this.getWorldRandom(), x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		int heightMapY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, internalX, internalZ);
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
