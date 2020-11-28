package com.pg85.otg.forge.gen;

import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.gen.biome.BiomeInterpolator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
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
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.gen.WorldGenRegion;

class ForgeWorldGenRegion extends LocalWorldGenRegion
{
	private final WorldGenRegion worldGenRegion;
	private final OTGNoiseChunkGenerator chunkGenerator;
	
    // 32x32 biomes cache for fast lookups during population
    private IBiome[][] cachedBiomeConfigs;
    private boolean cacheIsValid;

	ForgeWorldGenRegion(String presetName, IWorldConfig worldConfig, WorldGenRegion worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(presetName, worldConfig);
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}

	@Override
	public String getWorldName()
	{
		return ((IServerWorldInfo)this.worldGenRegion.getWorldInfo()).getWorldName();
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
	public IBiome getBiome(int x, int z) // TODO: Implement 3d biomes
	{
		Biome biome = this.worldGenRegion.getBiome(new BlockPos(x, 1, z));		
		if(biome != null)
		{
			// TODO: Pass preset or biome list with worldgenregion, so no lookups by preset name needed?
			int id = BiomeInterpolator.getId(getSeed(), x, 0, z, (OTGBiomeProvider)this.chunkGenerator.getBiomeProvider());
			BiomeConfig biomeConfig = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeConfig(this.presetName, id);
			if(biomeConfig != null)
			{
				return new ForgeBiome(biome, biomeConfig);
			}
		}
		return null;
	}

	@Override
	public BiomeConfig getBiomeConfig(int x, int z) // TODO: Implement 3d biomes
	{
		Biome biome = this.worldGenRegion.getBiome(new BlockPos(x, 1, z));		
		if(biome != null)
		{
			// TODO: Pass preset or biome list with worldgenregion, so no lookups by preset name needed?
			int id = BiomeInterpolator.getId(getSeed(), x, 0, z, (OTGBiomeProvider)this.chunkGenerator.getBiomeProvider());
			BiomeConfig biomeConfig = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeConfig(this.presetName, id);
			return biomeConfig;
		}
		return null;
	}
	
	// A 32x32 cache of biomes is filled when population starts for each chunk, any resource 
	// spawning during population should use getBiomeForPopulation/getBiomeConfigForPopulation 
	// for any operation that is intended to stay within population bounds.

    @Override
    public IBiome getBiomeForPopulation(int worldX, int worldZ, ChunkCoordinate chunkBeingPopulated)
    {
    	// Cache is invalidated when cascading chunkgen happens.
    	return !cacheIsValid ? getBiome(worldZ, worldX) : this.cachedBiomeConfigs[worldX - chunkBeingPopulated.getBlockX()][worldZ - chunkBeingPopulated.getBlockZ()];
    }
	
    @Override
    public IBiomeConfig getBiomeConfigForPopulation(int worldX, int worldZ, ChunkCoordinate chunkBeingPopulated)
    {
    	// Cache is invalidated when cascading chunkgen happens.
    	return !cacheIsValid ? getBiome(worldZ, worldX).getBiomeConfig() : this.cachedBiomeConfigs[worldX - chunkBeingPopulated.getBlockX()][worldZ - chunkBeingPopulated.getBlockZ()].getBiomeConfig();
    }

	@Override
	public void cacheBiomesForPopulation(ChunkCoordinate chunkCoord)
	{
		this.cachedBiomeConfigs = new IBiome[32][32];
		
		int areaSize = 32; 
		for(int x = 0; x < areaSize; x++)
		{
			for(int z = 0; z < areaSize; z++)
			{
				this.cachedBiomeConfigs[x][z] = getBiome(chunkCoord.getBlockX() + x, chunkCoord.getBlockZ() + z);
			}
		}
		this.cacheIsValid = true;
	}
	
	// Population biome cache is invalidated when cascading chunkgen happens
	@Override
	public void invalidatePopulationBiomeCache()
	{
		this.cacheIsValid = false;
	}

	@Override
    public double getBiomeBlocksNoiseValue(int blockX, int blockZ)
    {
    	return this.chunkGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
    }	
	
	// TODO: Create tree configs that don't do rarity/chance rolls (we've already done that),
	// and make sure they look more or less the same as 1.12.2.
	@Override
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
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> oak = Features.field_243862_bH;
	            	oak.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, oak.config);
	            	return true;
	            case BigTree:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> fancy_oak = Features.field_243869_bO;
	            	fancy_oak.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, fancy_oak.config);
	            	return true;
	            case Forest:
	            case Birch:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> birch = Features.field_243864_bJ;
	            	birch.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, birch.config);
	            	return true;
	            case TallBirch:
	                //return this.longBirchTree.generate(this.world, rand, blockPos);
	            	return true; // TODO: Implement this
	            case HugeMushroom:	            	
	                if (rand.nextBoolean())
	                {
	                	ConfiguredFeature<IFeatureConfig, ?> huge_brown_mushroom = (ConfiguredFeature<IFeatureConfig, ?>) Features.field_243860_bF;
	                	huge_brown_mushroom.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, huge_brown_mushroom.config);
	                } else {
	                	ConfiguredFeature<IFeatureConfig, ?> huge_red_mushroom = (ConfiguredFeature<IFeatureConfig, ?>) Features.field_243861_bG;
	                	huge_red_mushroom.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, huge_red_mushroom.config);
	                }
	                return true;
	            case HugeRedMushroom:
	            	ConfiguredFeature<IFeatureConfig, ?> huge_red_mushroom = (ConfiguredFeature<IFeatureConfig, ?>) Features.field_243861_bG;
	            	huge_red_mushroom.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, huge_red_mushroom.config);
	            	return true;
	            case HugeBrownMushroom:
	            	ConfiguredFeature<IFeatureConfig, ?> huge_brown_mushroom = (ConfiguredFeature<IFeatureConfig, ?>) Features.field_243860_bF;
	            	huge_brown_mushroom.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, huge_brown_mushroom.config);
	            	return true;
	            case SwampTree:
	            	ConfiguredFeature<IFeatureConfig, ?> swamp_tree = (ConfiguredFeature<IFeatureConfig, ?>) Features.field_243875_bU;
	            	swamp_tree.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, swamp_tree.config);
	            	return true;
	            case Taiga1:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> spruce = Features.field_243866_bL;
	            	spruce.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, spruce.config);
	            	return true;
	            case Taiga2:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> pine = Features.field_243867_bM;
	            	pine.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, pine.config);
	            	return true;
	            case JungleTree:
	            case CocoaTree:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> jungle_tree = Features.field_243868_bN;
	            	jungle_tree.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, jungle_tree.config);
	            	return true;
	            case GroundBush:
	            	ConfiguredFeature<IFeatureConfig, ?> jungle_bush = (ConfiguredFeature<IFeatureConfig, ?>) Features.field_243876_bV;
	            	jungle_bush.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, jungle_bush.config);
	            	return true;
	            case Acacia:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> acacia = Features.field_243865_bK;
	            	acacia.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, acacia.config);
	            	return true;
	            case DarkOak:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> dark_oak = Features.field_243863_bI;
	            	dark_oak.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, dark_oak.config);
	            	return true;
	            case HugeTaiga1:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> mega_spruce = Features.field_243872_bR;
	            	mega_spruce.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, mega_spruce.config);
	            	return true;
	            case HugeTaiga2:
	            	ConfiguredFeature<BaseTreeFeatureConfig, ?> mega_pine = Features.field_243873_bS;
	            	mega_pine.feature.func_241855_a(this.worldGenRegion, this.chunkGenerator, rand, blockPos, mega_pine.config);
	            	return true;
	            default:
	                throw new RuntimeException("Failed to handle tree of type " + type.toString());
	        }
        }
        catch(NullPointerException ex)
        {
        	OTG.log(LogMarker.WARN, "Treegen caused a non-fatal exception: ");
        	ex.printStackTrace();
        	return true; // Return true to prevent further attempts.
        }
	}

	// Used by ChunkGenerator for BO4's requesting data
	// in chunks outside the area being populated.
	IChunk getChunk(ChunkCoordinate chunkCoord)
	{
		return this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
	}
	
	@Override
	public LocalMaterialData getMaterial(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
	{
        if (y >= Constants.WORLD_HEIGHT || y < Constants.WORLD_DEPTH)
        {
        	return null;
        }
     
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
        
        // If the chunk exists or is inside the area being populated, fetch it normally.
        IChunk chunk = null;
    	if(chunkBeingPopulated != null && ChunkCoordinate.IsInAreaBeingPopulated(x, z, chunkBeingPopulated))
    	{
    		chunk = this.worldGenRegion.chunkExists(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
    	}
    	
		// If the chunk doesn't exist so we're doing something outside the
    	// population sequence, return the material without loading the chunk.
    	if((chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.LIQUID_CARVERS)) && chunkBeingPopulated == null)
		{
    		// If the chunk has already been loaded, no need to use fake chunks.
    		if(
				!(
					chunk == null && 
					this.worldGenRegion.chunkExists(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) && 
					(chunk = this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ())).getStatus().isAtLeast(ChunkStatus.LIQUID_CARVERS)
				)
			)
    		{
       			// Calculate the material without loading the chunk.
       			return this.chunkGenerator.getMaterialInUnloadedChunk(this, x , y, z);
    		}
    	}
    	
		// Tried to query an unloaded chunk outside the area being populated
    	if(chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.LIQUID_CARVERS))
    	{
            return null;
    	}
    	
		// Get internal coordinates for block in chunk
        int internalX = x & 0xF;
        int internalZ = z & 0xF;
        return ForgeMaterialData.ofMinecraftBlockState(chunk.getBlockState(new BlockPos(internalX, y, internalZ)));
	}
	
	@Override
	public int getBlockAboveLiquidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
        int highestY = getHighestBlockYAt(x, z, false, true, false, false, false, chunkBeingPopulated);
        if(highestY > 0)
        {
        	highestY += 1;
        } else {
        	highestY = -1;
        }
		return highestY;
	}

	@Override
	public int getBlockAboveSolidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
        int highestY = getHighestBlockYAt(x, z, true, false, true, true, false, chunkBeingPopulated);
        if(highestY > 0)
        {
        	highestY += 1;
        } else {
        	highestY = -1;
        }
		return highestY;
	}

	@Override
	public int getHighestBlockAboveYAt(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		return getHighestBlockYAt(x, z, true, true, false, false, false, chunkBeingPopulated) + 1;
	}

	@Override
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves, ChunkCoordinate chunkBeingPopulated)
	{
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
        
        // If the chunk exists or is inside the area being populated, fetch it normally.
        IChunk chunk = null;
    	if(chunkBeingPopulated != null && ChunkCoordinate.IsInAreaBeingPopulated(x, z, chunkBeingPopulated))
    	{
    		chunk = this.worldGenRegion.chunkExists(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
    	}
    	
		// If the chunk doesn't exist and we're doing something outside the
    	// population sequence, return the material without loading the chunk.
    	if((chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.LIQUID_CARVERS)) && chunkBeingPopulated == null)
		{
    		// If the chunk has already been loaded, no need to use fake chunks.
    		if(
				!(
					chunk == null && 
					this.worldGenRegion.chunkExists(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) && 
					(chunk = this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ())).getStatus().isAtLeast(ChunkStatus.LIQUID_CARVERS)
				)
			)
    		{
       			// Calculate the material without loading the chunk.
       			return this.chunkGenerator.getHighestBlockYInUnloadedChunk(this, x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
    		}
    	}
    	
		// Tried to query an unloaded chunk outside the area being populated
    	if(chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.LIQUID_CARVERS))
    	{
    		return -1;
    	}
    	
		// Get internal coordinates for block in chunk
        int internalX = x & 0xF;
        int internalZ = z & 0xF;
        
        // TODO: For some reason, on rare occasions WORLD_SURFACE_WG heightmap returns 0 for chunks
        // with status LIQUID_CARVERS, while the chunk does already have base terrain blocks filled.
        // If we use a later status like FEATURES though, resource population may have problems 
        // fetching chunks.
		int heightMapy = chunk.getHeightmap(Type.WORLD_SURFACE_WG).getHeight(internalX, internalZ);
		if(heightMapy == 0)
		{
			heightMapy = Constants.WORLD_HEIGHT - 1;
		}

        ForgeMaterialData material;
        boolean isSolid;
        boolean isLiquid;
        BlockState blockState;
        Block block;
        
        for(int i = heightMapy; i >= 0; i--)
        {
        	blockState = chunk.getBlockState(new BlockPos(internalX, i, internalZ));
        	block = blockState.getBlock();
    		material = ForgeMaterialData.ofMinecraftBlockState(blockState);
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
	public int getHeightMapHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		return this.worldGenRegion.getHeight(Type.WORLD_SURFACE_WG, x, z); 
	}

	@Override
	public int getLightLevel(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
	{
    	if(y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)
    	{
    		return -1;
    	}

    	// Check if the chunk has been lit, otherwise cancel.
    	// TODO: Check if this causes problems with BO3 LightChecks.
    	// TODO: Make a getLight method based on world.getLight that uses unloaded chunks.
    	ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
    	IChunk chunk = this.worldGenRegion.chunkExists(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) ? this.worldGenRegion.getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) : null;
    	if(chunkBeingPopulated == null && chunk.getStatus().isAtLeast(ChunkStatus.LIGHT))
    	{
	        // This fetches the block and skylight as if it were day.
    		return this.worldGenRegion.getLight(new BlockPos(x, y, z));
    	}
		return -1;
	}

	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks)
	{
		setBlock(x, y, z, material, metaDataTag, chunkBeingPopulated, null, replaceBlocks);
	}

	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, ReplacedBlocksMatrix replaceBlocksMatrix, boolean replaceBlocks)
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
    	
    	// If no chunk was passed, we're doing something outside of the population cycle.
    	// If a chunk was passed, only spawn in the area being populated.
    	if(chunkBeingPopulated == null || ChunkCoordinate.IsInAreaBeingPopulated(x, z, chunkBeingPopulated))
    	{
    		if(replaceBlocks)
    		{
        		if(replaceBlocksMatrix == null)
        		{
        			if(chunkBeingPopulated == null)
        			{
        				replaceBlocksMatrix = this.getBiomeConfig(x, z).getReplaceBlocks();
        			} else {
        				replaceBlocksMatrix = this.getBiomeConfigForPopulation(x, z, chunkBeingPopulated).getReplaceBlocks();
        			}
        		}
    			material = material.parseWithBiomeAndHeight(this.getWorldConfig().getBiomeConfigsHaveReplacement(), replaceBlocksMatrix, y);
    		}
    		this.worldGenRegion.setBlockState(new BlockPos(x, y, z), ((ForgeMaterialData)material).internalBlock(), 2 | 16);    		
    	}
	}
	
	//

	@Override
	public boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate)
	{
		// TODO: Implement this.
		return false;
	}	
	
	@Override
	public void spawnEntity(IEntityFunction<?> newEntityData, ChunkCoordinate chunkCoordinate)
	{
		// TODO: Implement this.
	}

	@Override
	public void placeDungeon(Random random, int x, int y, int z)
	{
		// TODO: Implement this.
	}

	@Override
	public void placeFossil(Random random, ChunkCoordinate chunkCoord)
	{
		// TODO: Implement this.
	}

	@Override
	public void prepareDefaultStructures(long worldSeed, int x, int z, boolean dry)
	{
		// TODO: Implement this.
	}

	@Override
	public boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate)
	{
		// TODO: Implement this.
		return true;
	}

	@Override
	public ChunkCoordinate getSpawnChunk()
	{
		// TODO: Implement this.
		return null;
	}
}
