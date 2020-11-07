package com.pg85.otg.forge.gen;

import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.standard.PluginStandardValues;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.customobjects.SpawnableObject;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.gen.ChunkBuffer;
import com.pg85.otg.gen.biome.BiomeGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.TreeType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;

public class ForgeWorldGenRegion extends LocalWorldGenRegion
{
	private final WorldGenRegion worldGenRegion;
	private final OTGNoiseChunkGenerator chunkGenerator;
	private final String worldName;
	
	public ForgeWorldGenRegion(String worldName, long worldSeed, WorldConfig worldConfig, WorldGenRegion worldGenRegion, OTGNoiseChunkGenerator chunkGenerator)
	{
		super(worldConfig, worldSeed);
		this.worldName = worldName;
		this.worldGenRegion = worldGenRegion;
		this.chunkGenerator = chunkGenerator;
	}

	@Override
	public String getWorldName()
	{
		return this.worldName;
	}
	
	@Override
	public LocalBiome getBiome(int x, int z) // TODO: Implement 3d biomes
	{
		Biome biome = this.worldGenRegion.getBiome(new BlockPos(x, 1, z));		
		if(biome != null)
		{
			// TODO: Fetch biomeConfig via Biome once layers and registries are fixed?
			//ResourceLocation resourceLocation = ForgeRegistries.BIOMES.getKey(biome);
			//BiomeConfig biomeConfig = OTG.getEngine().getPresetLoader().getBiomeConfig(resourceLocation.toString());
			RegistryKey<Biome> key = ((OTGBiomeProvider)this.chunkGenerator.getBiomeProvider()).getBiomeRegistryKey(x, 1, z);
			BiomeConfig biomeConfig = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeConfig(key.func_240901_a_().toString());
			if(biomeConfig != null)
			{
				return new ForgeBiome(biome, biomeConfig);
			}
		}
		return null;
	}
	
	@Override
	public BiomeConfig getBiomeConfig(int x, int z) throws BiomeNotFoundException // TODO: Implement 3d biomes
	{
		Biome biome = this.worldGenRegion.getBiome(new BlockPos(x, 1, z));		
		if(biome != null)
		{
			// TODO: Fetch biomeConfig via Biome once layers and registries are fixed?
			//ResourceLocation resourceLocation = ForgeRegistries.BIOMES.getKey(biome);
			//BiomeConfig biomeConfig = OTG.getEngine().getPresetLoader().getBiomeConfig(resourceLocation.toString());
			RegistryKey<Biome> key = ((OTGBiomeProvider)this.chunkGenerator.getBiomeProvider()).getBiomeRegistryKey(x, 1, z);
			BiomeConfig biomeConfig = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeConfig(key.func_240901_a_().toString());
			return biomeConfig;
		}
		return null;
	}

	// TODO: Create tree configs that don't do rarity/chance rolls (we've already done that),
	// and make sure they look more or less the same as 1.12.2.
	@Override
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
	{
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
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

	@Override
	public LocalMaterialData getMaterial(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
	{
		return ForgeMaterialData.ofMinecraftBlockState(this.worldGenRegion.getBlockState(new BlockPos(x, y, z)));
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
        // If the chunk exists or is inside the area being populated, fetch it normally.
        //Chunk chunk = null;
    	//if(
			//(chunkBeingPopulated != null && OTG.IsInAreaBeingPopulated(x, z, chunkBeingPopulated))
			//|| getChunkGenerator().chunkExists(x, z)
		//)
    	//{
    		//chunk = getChunkGenerator().getChunk(x, z);
    	//}
    	
		// Get internal coordinates for block in chunk
        //int internalX = x & 0xF;
        //int internalZ = z & 0xF;

        int heightMapy = this.worldGenRegion.getHeight(Type.MOTION_BLOCKING, x, z);
        
        ForgeMaterialData material;
        boolean isSolid;
        boolean isLiquid;
        BlockState blockState;
        Block block;
        
        for(int i = heightMapy; i >= 0; i--)
        {
        	blockState = this.worldGenRegion.getBlockState(new BlockPos(x, i, z));
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
            		// Found an illegal block (liquid when looking for solid, or vice-versa)
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
		return this.worldGenRegion.getHeight(Type.MOTION_BLOCKING, x, z);
	}

	@Override
	public int getLightLevel(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
	{
		return this.worldGenRegion.getLight(new BlockPos(x, y, z));
	}

	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks)
	{
		setBlock(x, y, z, material, metaDataTag, chunkBeingPopulated, null, replaceBlocks);
	}

	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, BiomeConfig biomeConfig, boolean replaceBlocks)
	{
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
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
    	// If a chunk was passed, only spawn in the area being populated, or existing chunks.
    	if(
			chunkBeingPopulated == null ||
			(
				OTG.IsInAreaBeingPopulated(x, z, chunkBeingPopulated) //|| 
				//getChunkGenerator().chunkExists(x, z)
			)
		)
    	{
    		/*
    		if(replaceBlocks)
    		{
        		if(biomeConfig == null)
        		{
        			if(chunkBeingPopulated == null)
        			{
        				biomeConfig = this.getBiome(x, z).getBiomeConfig();
        			} else {
        				biomeConfig = this.getBiomeForPopulation(x, z, chunkBeingPopulated).getBiomeConfig();
        			}
        		}
    			material = material.parseWithBiomeAndHeight(this, biomeConfig, y);
    		}
    		*/
    		this.worldGenRegion.setBlockState(new BlockPos(x, y, z), ((ForgeMaterialData)material).internalBlock(), 3);    		
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
	public boolean generateModdedCaveGen(long worldSeed, int x, int z, ChunkBuffer chunkBuffer)
	{
		// TODO: Implement this.
		return false;
	}	
	
	@Override
	public void spawnEntity(EntityFunction<?> newEntityData, ChunkCoordinate chunkCoordinate)
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
		
	//
	
	@Override
	public SpawnableObject getMojangStructurePart(String path)
	{
		// TODO: Do we still need this?
		return null;
	}
	
	@Override
	public BiomeGenerator getBiomeGenerator()
	{
		// TODO: Do we still need this with the new biome/chunkgen?
		return null;
	}

	@Override
	public double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld)
	{
		// TODO: Implement this when 1.16 noisegen is done.
		return 0;
	}
}
