package com.pg85.otg.forge.gen;

import java.util.Optional;
import java.util.Random;

import com.google.gson.JsonSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.logging.LogCategory;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.interfaces.IBiome;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IEntityFunction;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.TreeType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class ForgeWorldGenRegion extends LocalWorldGenRegion
{
	protected final ISeedReader worldGenRegion;
	private final OTGNoiseChunkGenerator chunkGenerator;

	// BO4 plotting may call hasDefaultStructures on chunks outside the area being decorated, in order to plot large structures.
	// It may query the same chunk multiple times, so use a fixed size cache.
	private final FifoMap<ChunkCoordinate, Boolean> cachedHasDefaultStructureChunks = new FifoMap<ChunkCoordinate, Boolean>(2048);

	/** Creates a LocalWorldGenRegion to be used during decoration for OTG worlds. */
	public ForgeWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, WorldGenRegion worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger(), worldGenRegion.getCenterX(), worldGenRegion.getCenterZ());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}
	
	/** Creates a LocalWorldGenRegion to be used for OTG worlds outside of decoration, only used for /otg spawn/edit/export. */
	public ForgeWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, ISeedReader worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}
	
	/** Creates a LocalWorldGenRegion to be used for non-OTG worlds outside of decoration, only used for /otg spawn/edit/export. */
	public ForgeWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, ISeedReader worldGenRegion)
	{
		super(presetFolderName, OTG.getEngine().getPluginConfig(), worldConfig, OTG.getEngine().getLogger());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = null;
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
	
	public ISeedReader getInternal()
	{
		return worldGenRegion;
	}
	
	@Override
	public IBiome getBiome(int x, int z) // TODO: Implement 3d biomes
	{
		// TODO: Do we need to use BiomeInterpolator here? getBiome(BlockPos()) appears
		// to do magnification correctly by itself? Test and verify.		
		Biome biome = this.worldGenRegion.getBiome(new BlockPos(x, 1, z));
		BiomeConfig biomeConfig = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeConfig(biome.getRegistryName().toString());
		if(biomeConfig != null)
		{
			return new ForgeBiome(biome, biomeConfig);
		}
		return null;
	}

	@Override
	public BiomeConfig getBiomeConfig(int x, int z) // TODO: Implement 3d biomes
	{
		// TODO: Do we need to use BiomeInterpolator here? getBiome(BlockPos()) appears
		// to do magnification correctly by itself? Test and verify.		
		Biome biome = this.worldGenRegion.getBiome(new BlockPos(x, 1, z));
		return ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeConfig(biome.getRegistryName().toString());
	}

	@Override
	public IBiome getBiomeForDecoration(int worldX, int worldZ)
	{
		if(this.decorationBiomeCache != null)
		{
			return this.decorationBiomeCache.getBiome(worldX, worldZ, this);
		}
		return this.getBiome(worldX, worldZ);
	}
	
	@Override
	public IBiomeConfig getBiomeConfigForDecoration(int worldX, int worldZ)
	{
		if(this.decorationBiomeCache != null)
		{
			return this.decorationBiomeCache.getBiome(worldX, worldZ, this).getBiomeConfig();
		}
		return this.getBiome(worldX, worldZ).getBiomeConfig();
	}

	@Override
	public double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		return this.chunkGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}

	@Override
	public LocalMaterialData getMaterial(int x, int y, int z)
	{
		if (y >= Constants.WORLD_HEIGHT || y < Constants.WORLD_DEPTH)
		{
			return null;
		}

		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		IChunk chunk = null;
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
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
		
		// If the chunk exists or is inside the area being decorated, fetch it normally.
		IChunk chunk = null;
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
		int heightMapy = chunk.getHeight(Type.WORLD_SURFACE, internalX, internalZ);
		
		return getHighestBlockYAt(chunk, internalX, heightMapy, internalZ, findSolid, findLiquid, ignoreLiquid, ignoreSnow, ignoreLeaves);
	}	

	protected int getHighestBlockYAt(IChunk chunk, int internalX, int heightMapY, int internalZ, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
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
		return this.worldGenRegion.getHeight(Type.WORLD_SURFACE_WG, x, z); 
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
		IChunk chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		if(chunk != null && chunk.getStatus().isOrAfter(ChunkStatus.LIGHT))
		{
			// This fetches the block and skylight as if it were day.
			return this.worldGenRegion.getMaxLocalRawBrightness(new BlockPos(x, y, z));
		}
		return -1;
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
		if(this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			if(replaceBlocksMatrix != null)
			{
				material = material.parseWithBiomeAndHeight(this.getWorldConfig().getBiomeConfigsHaveReplacement(), replaceBlocksMatrix, y);
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
			} catch (JsonSyntaxException e) {
				if(this.logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{				
					this.logger.log(
						LogMarker.WARN,
						LogCategory.CUSTOM_OBJECTS,
						String.format(
							"Badly formatted json for tile entity with id '{}' at {},{},{}", 
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
					LogMarker.WARN,
					LogCategory.CUSTOM_OBJECTS,
					String.format(
						"Skipping tile entity with id {}, cannot be placed at {},{},{}", 
						nms.getString("id"), 
						x, y, z
					)
				);
			}
		}
	}
	
	public TileEntity getTileEntity(BlockPos blockPos)
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
		try
		{
			switch (type)
			{
				case Tree:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> oak = Features.OAK;
					oak.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, oak.config);
					return true;
				case BigTree:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> fancy_oak = Features.FANCY_OAK;
					fancy_oak.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, fancy_oak.config);
					return true;
				case Forest:
				case Birch:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> birch = Features.BIRCH;
					birch.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, birch.config);
					return true;
				case TallBirch:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> tall_birch = Features.SUPER_BIRCH_BEES_0002;
					tall_birch.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, tall_birch.config);
					return true;
				case HugeMushroom:
					if (rand.nextBoolean())
					{
						ConfiguredFeature<IFeatureConfig, ?> huge_brown_mushroom = (ConfiguredFeature<IFeatureConfig, ?>) Features.HUGE_BROWN_MUSHROOM;
						huge_brown_mushroom.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, huge_brown_mushroom.config);
					} else {
						ConfiguredFeature<IFeatureConfig, ?> huge_red_mushroom = (ConfiguredFeature<IFeatureConfig, ?>) Features.HUGE_RED_MUSHROOM;
						huge_red_mushroom.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, huge_red_mushroom.config);
					}
					return true;
				case HugeRedMushroom:
					ConfiguredFeature<IFeatureConfig, ?> huge_red_mushroom = (ConfiguredFeature<IFeatureConfig, ?>) Features.HUGE_RED_MUSHROOM;
					huge_red_mushroom.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, huge_red_mushroom.config);
					return true;
				case HugeBrownMushroom:
					ConfiguredFeature<IFeatureConfig, ?> huge_brown_mushroom = (ConfiguredFeature<IFeatureConfig, ?>) Features.HUGE_BROWN_MUSHROOM;
					huge_brown_mushroom.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, huge_brown_mushroom.config);
					return true;
				case SwampTree:
					ConfiguredFeature<IFeatureConfig, ?> swamp_tree = (ConfiguredFeature<IFeatureConfig, ?>) Features.SWAMP_TREE;
					swamp_tree.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, swamp_tree.config);
					return true;
				case Taiga1:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> pine = Features.PINE;
					pine.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, pine.config);
					return true;
				case Taiga2:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> spruce = Features.SPRUCE;
					spruce.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, spruce.config);
					return true;
				case JungleTree:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> mega_jungle_tree = Features.MEGA_JUNGLE_TREE;
					mega_jungle_tree.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, mega_jungle_tree.config);
					return true;
				case CocoaTree:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> jungle_tree = Features.JUNGLE_TREE;
					jungle_tree.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, jungle_tree.config);
					return true;
				case GroundBush:
					ConfiguredFeature<IFeatureConfig, ?> jungle_bush = (ConfiguredFeature<IFeatureConfig, ?>) Features.JUNGLE_BUSH;
					jungle_bush.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, jungle_bush.config);
					return true;
				case Acacia:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> acacia = Features.ACACIA;
					acacia.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, acacia.config);
					return true;
				case DarkOak:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> dark_oak = Features.DARK_OAK;
					dark_oak.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, dark_oak.config);
					return true;
				case HugeTaiga1:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> mega_pine = Features.MEGA_PINE;
					mega_pine.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, mega_pine.config);
					return true;
				case HugeTaiga2:
					ConfiguredFeature<BaseTreeFeatureConfig, ?> mega_spruce = Features.MEGA_SPRUCE;
					mega_spruce.feature.place(this.worldGenRegion, this.chunkGenerator, rand, blockPos, mega_spruce.config);
					return true;
				default:
					throw new RuntimeException("Failed to handle tree of type " + type.toString());
			}
		}
		catch(NullPointerException ex)
		{
			if(this.logger.getLogCategoryEnabled(LogCategory.DECORATION))
			{
				this.logger.log(LogMarker.WARN, LogCategory.DECORATION, String.format("Treegen caused a non-fatal exception: ", (Object[])ex.getStackTrace()));
			}
			// Return true to prevent further attempts.
			return true;
		}
	}
	
	@Override
	public void spawnEntity(IEntityFunction<?> newEntityData)
	{
		// TODO: Implement this.
	}

	@Override
	public void placeDungeon(Random random, int x, int y, int z, int range, int count)
	{
		Feature.MONSTER_ROOM.configured(IFeatureConfig.NONE).range(range).squared().count(count).place(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(x, y, z));
	}

	@Override
	public void placeFossil(Random random, int x, int y, int z, int chance)
	{
		Feature.FOSSIL.configured(IFeatureConfig.NONE).chance(chance).place(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(x, y, z));
	}

	@Override
	public void placeFromRegistry(Random random, ChunkCoordinate chunkCoord, String id)
	{
		DynamicRegistries registries = this.worldGenRegion.getLevel().registryAccess();
		Registry<ConfiguredFeature<?, ?>> registry = registries.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);

		Optional<ConfiguredFeature<?, ?>> feature = registry.getOptional(new ResourceLocation(id));

		if (feature.isPresent())
		{
			feature.get().place(this.worldGenRegion, this.chunkGenerator, random, new BlockPos(chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ()));
		} else {
			if(this.logger.getLogCategoryEnabled(LogCategory.DECORATION))
			{
				this.logger.log(LogMarker.ERROR, LogCategory.DECORATION, "Unable to find registry object " + id);
			}
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
		IChunk chunk = null;
		if(this.decorationArea != null && this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}

		// If the chunk doesn't exist so we're doing something outside the
		// decoration sequence, return the material without loading the chunk.
		if((chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS)))
		{
			return this.chunkGenerator.getMaterialInUnloadedChunk(this.getWorldRandom(), x , y, z);
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
		IChunk chunk = null;
		if(this.decorationArea != null && this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}
		
		// If the chunk doesn't exist and we're doing something outside the
		// decoration sequence, return the material without loading the chunk.
		if((chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.LIQUID_CARVERS)))
		{
			return this.chunkGenerator.getHighestBlockYInUnloadedChunk(this.getWorldRandom(), x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
		}

		// Get internal coordinates for block in chunk
		int internalX = x & 0xF;
		int internalZ = z & 0xF;
		int heightMapy = chunk.getHeight(Type.WORLD_SURFACE_WG, internalX, internalZ);	
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
		cachedHasDefaultStructureChunks.put(chunkCoordinate, new Boolean(hasDefaultStructure));
		return hasDefaultStructure;
	}
}
