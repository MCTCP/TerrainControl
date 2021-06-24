package com.pg85.otg.spigot.gen;

import com.google.gson.JsonSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.gen.biome.BiomeInterpolator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import com.pg85.otg.spigot.biome.SpigotBiome;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.spigot.util.SpigotNBTHelper;
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
import net.minecraft.server.v1_16_R3.*;

import java.util.Optional;
import java.util.Random;

public class SpigotWorldGenRegion extends LocalWorldGenRegion
{
	private final GeneratorAccessSeed worldGenRegion;
	private final ChunkGenerator chunkGenerator;

	// BO4 plotting may call hasDefaultStructures on chunks outside the area being decorated, in order to plot large structures.
	// It may query the same chunk multiple times, so use a fixed size cache.
	private FifoMap<ChunkCoordinate, Boolean> cachedHasDefaultStructureChunks = new FifoMap<ChunkCoordinate, Boolean>(2048);

	/** Creates a LocalWorldGenRegion to be used during chunk decoration */
	public SpigotWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, RegionLimitedWorldAccess worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetFolderName, worldConfig, worldGenRegion.a(), worldGenRegion.b());
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}
	
	/** Creates a LocalWorldGenRegion to be used outside of world generation.
	 * 	Note that it allows you to input ChunkGenerator instead of OTGNoiseChunkGenerator - do so with caution.
	 * 	It may crash if you try to do replaceblocks or use similar otg-specific features. 
	 */
	public SpigotWorldGenRegion(String presetFolderName, IWorldConfig worldConfig, GeneratorAccessSeed worldGenRegion, ChunkGenerator chunkGenerator)
	{
		super(presetFolderName, worldConfig);
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
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

	public GeneratorAccessSeed getInternal()
	{
		return this.worldGenRegion;
	}	
	
	@Override
	public IBiome getBiome(int x, int z)
	{
		BiomeBase biome = this.worldGenRegion.getBiome(new BlockPosition(x, 1, z));
		if (biome != null)
		{
			int id = BiomeInterpolator.getId(getSeed(), x, 0, z, (OTGBiomeProvider) this.chunkGenerator.getWorldChunkManager());
			BiomeConfig biomeConfig = OTG.getEngine().getPresetLoader().getBiomeConfig(this.presetFolderName, id);
			if (biomeConfig != null)
			{
				return new SpigotBiome(biome, biomeConfig);
			}
		}
		return null;
	}

	@Override
	public IBiomeConfig getBiomeConfig(int x, int z)
	{
		BiomeBase biome = this.worldGenRegion.getBiome(new BlockPosition(x, 1, z));
		if (biome != null)
		{
			int id = BiomeInterpolator.getId(getSeed(), x, 0, z, (OTGBiomeProvider) this.chunkGenerator.getWorldChunkManager());
			return OTG.getEngine().getPresetLoader().getBiomeConfig(this.presetFolderName, id);
		}
		return null;
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
	public double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld)
	{
		return ((OTGNoiseChunkGenerator) this.chunkGenerator).getBiomeBlocksNoiseValue(xInWorld, zInWorld);
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

	private int getHighestBlockYAt(IChunkAccess chunk, int internalX, int heightMapY, int internalZ, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
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

		// If no chunk was passed, we're doing something outside of the decoration cycle.
		// If a chunk was passed, only spawn in the area being decorated.
		if(this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			if(replaceBlocksMatrix != null)
			{
				material = material.parseWithBiomeAndHeight(this.getWorldConfig().getBiomeConfigsHaveReplacement(), replaceBlocksMatrix, y);
			}

			BlockPosition pos = new BlockPosition(x, y, z);
			this.worldGenRegion.setTypeAndData(pos, ((SpigotMaterialData) material).internalBlock(), 2 | 16);

			if (material.isLiquid())
			{
				this.worldGenRegion.getFluidTickList().a(pos, ((SpigotMaterialData)material).internalBlock().getFluid().getType(), 0);
			}

			if (nbt != null)
			{
				this.attachNBT(x, y, z, nbt, worldGenRegion.getType(pos));
			}
		}
	}

	private void attachNBT(int x, int y, int z, NamedBinaryTag nbt, IBlockData state)
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
				tree.e.generate(this.worldGenRegion, this.chunkGenerator, rand, blockPos, tree.f);
			else if (other != null)
				other.e.generate(this.worldGenRegion, this.chunkGenerator, rand, blockPos, other.f);
			else throw new RuntimeException("Incorrect handling of tree of type " + type.toString());
			return true;
		}
		catch (NullPointerException ex)
		{
			OTG.log(LogMarker.WARN, "Treegen caused a non-fatal exception: ");
			ex.printStackTrace();
			// Return true to prevent further attempts.
			return true;
		}
	}	

	@Override
	public void spawnEntity (IEntityFunction<?> newEntityData)
	{
		// TODO: Implement this.
	}

	@Override
	public void placeDungeon (Random random, int x, int y, int z)
	{
		BiomeDecoratorGroups.MONSTER_ROOM.a(this.worldGenRegion, this.chunkGenerator, random, new BlockPosition(x, y, z));
	}

	@Override
	public void placeFossil (Random random, ChunkCoordinate chunkCoord)
	{
		BiomeDecoratorGroups.FOSSIL.a(this.worldGenRegion, this.chunkGenerator, random, new BlockPosition(chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ()));
	}

	@Override
	public void placeFromRegistry(Random random, ChunkCoordinate chunkCoord, String id)
	{
		IRegistryCustom registries = this.worldGenRegion.getMinecraftWorld().r();
		IRegistry<WorldGenFeatureConfigured<?, ?>> registry = registries.b(IRegistry.au);

		Optional<WorldGenFeatureConfigured<?, ?>> feature = registry.getOptional(new MinecraftKey(id));

		if (feature.isPresent()) {
			feature.get().a(this.worldGenRegion, this.chunkGenerator, random, new BlockPosition(chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ()));
		} else {
			OTG.log(LogMarker.ERROR, "Unable to find registry object " + id);
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
		return worldGenRegion.getType(blockpos);
	}

	public void setBlockState(BlockPosition blockpos, IBlockData blockstate1, int i)
	{
		worldGenRegion.setTypeAndData(blockpos, blockstate1, i);
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
		if (this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}
		// isAtLeast() -> b()
		if ((chunk == null || !chunk.getChunkStatus().b(ChunkStatus.LIQUID_CARVERS)))
		{
			// If the chunk has already been loaded, no need to use fake chunks.
			if (
				!(
					chunk == null &&
					this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) &&
					(chunk = this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ())).getChunkStatus().b(ChunkStatus.LIQUID_CARVERS)
				)
			)
			{
				// Calculate the material without loading the chunk.
				return ((OTGNoiseChunkGenerator) this.chunkGenerator).getMaterialInUnloadedChunk(this.getWorldRandom(), x, y, z);
			}
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
	public int getHighestBlockYAtWithoutLoading(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		// If the chunk exists or is inside the area being decorated, fetch it normally.
		IChunkAccess chunk = null;
		if (this.decorationArea == null || this.decorationArea.isInAreaBeingDecorated(x, z))
		{
			chunk = this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
		}

		// If the chunk doesn't exist and we're doing something outside the
		// decoration sequence, return the material without loading the chunk.
		if ((chunk == null || !chunk.getChunkStatus().b(ChunkStatus.LIQUID_CARVERS)))
		{
			// If the chunk has already been loaded, no need to use fake chunks.
			if (
				!(
					chunk == null &&
					this.worldGenRegion.isChunkLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) &&
					(chunk = this.worldGenRegion.getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ())).getChunkStatus().b(ChunkStatus.LIQUID_CARVERS)
				)
			)
			{
				// Calculate the material without loading the chunk.
				return ((OTGNoiseChunkGenerator) this.chunkGenerator).getHighestBlockYInUnloadedChunk(this.getWorldRandom(), x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
			}
		}

		// Tried to query an unloaded chunk outside the area being decorated
		if (chunk == null || !chunk.getChunkStatus().b(ChunkStatus.LIQUID_CARVERS))
		{
			return -1;
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
		hasDefaultStructure = ((OTGNoiseChunkGenerator) this.chunkGenerator).checkHasVanillaStructureWithoutLoading(this.worldGenRegion.getMinecraftWorld(), chunkCoordinate);
		cachedHasDefaultStructureChunks.put(chunkCoordinate, hasDefaultStructure);
		return hasDefaultStructure;
	}
}
