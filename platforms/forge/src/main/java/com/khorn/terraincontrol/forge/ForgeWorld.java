package com.khorn.terraincontrol.forge;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.configuration.*;
import com.khorn.terraincontrol.configuration.BiomeConfigFinder.BiomeConfigStub;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.MojangSettings.EntityCategory;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.customobjects.bo3.EntityFunction;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.generator.TXBiome;
import com.khorn.terraincontrol.forge.generator.TXChunkGenerator;
import com.khorn.terraincontrol.forge.generator.structure.*;
import com.khorn.terraincontrol.forge.util.MobSpawnGroupHelper;
import com.khorn.terraincontrol.forge.util.NBTHelper;
import com.khorn.terraincontrol.generator.SpawnableObject;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.*;

public class ForgeWorld implements LocalWorld
{
	public int clientDimensionId = 0;
	
    private TXChunkGenerator generator;
    public World world;
    private ConfigProvider settings;
    private CustomObjectStructureCache structureCache;
    private String name;
    public long seed;
    private BiomeGenerator biomeGenerator;
    private	DataFixer dataFixer;

    private static final int MAX_BIOMES_COUNT = 1024;
    private static final int MAX_SAVED_BIOMES_COUNT = 255;
    private static final int STANDARD_WORLD_HEIGHT = 128;

    public HashMap<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();

    public TXStrongholdGen strongholdGen;
    public TXVillageGen villageGen;
    public TXMineshaftGen mineshaftGen;
    public TXRareBuildingGen rareBuildingGen;
    public TXNetherFortressGen netherFortressGen;
    public TXOceanMonumentGen oceanMonumentGen;
    public TXWoodLandMansionGen woodLandMansionGen;

    private WorldGenDungeons dungeonGen;
    private WorldGenFossils fossilGen;

    private WorldGenTrees tree;
    private WorldGenSavannaTree acaciaTree;
    private WorldGenBigTree bigTree;
    private WorldGenBirchTree birchTree;
    private WorldGenTrees cocoaTree;
    private WorldGenCanopyTree darkOakTree;
    private WorldGenShrub groundBush;
    private WorldGenBigMushroom hugeRedMushroom;
    private WorldGenBigMushroom hugeBrownMushroom;
    private WorldGenMegaPineTree hugeTaigaTree1;
    private WorldGenMegaPineTree hugeTaigaTree2;
    private WorldGenMegaJungle jungleTree;
    private WorldGenBirchTree longBirchTree;
    private WorldGenSwamp swampTree;
    private WorldGenTaiga1 taigaTree1;
    private WorldGenTaiga2 taigaTree2;

    private Chunk[] chunkCache;

    public static HashMap<Integer, ResourceLocation> vanillaResouceLocations = new HashMap<Integer, ResourceLocation>();   
    public static Biome[] vanillaBiomes = new Biome[MAX_BIOMES_COUNT];    
    public static boolean vanillaBiomesCached = false;
    
    public boolean isMainWorld = false;
    
    public ForgeWorld(String _name, boolean isMainWorld)
    {
    	TerrainControl.log(LogMarker.INFO, "Creating world \"" + _name + "\"");
    	
        this.name = _name;
        this.isMainWorld = isMainWorld;

        cacheVanillaBiomes();
        
        // If this is the main world (which should be the first world to be generated)
        // cache the vanilla biomes and clear the biome registry and dictionary
        if(isMainWorld) 
        {	         	
        	// Default settings are not restored on world unload / server quit because this was causing problems 
        	// (unloading dimensions while their worlds were still ticking etc)
        	// Unload all world and biomes here instead.
        	
        	TXDimensionManager.UnloadAllCustomDimensionData();       	
        	((ForgeEngine)TerrainControl.getEngine()).worldLoader.unloadAllWorlds();       	
	        // Clear the BiomeDictionary (it will be refilled when biomes are loaded in createBiomeFor)
	    	((ForgeEngine)TerrainControl.getEngine()).worldLoader.clearBiomeDictionary(null);	    	
	        ((ForgeEngine)TerrainControl.getEngine()).worldLoader.unRegisterDefaultBiomes();	        
	        ((ForgeEngine)TerrainControl.getEngine()).worldLoader.unRegisterTCBiomes();
	    		        
	    	//TCDimensionManager.LoadCustomDimensionData();

	    	TXDimensionManager.RemoveTCDims();
        }
    }
          
    @Override
    public LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds, ConfigProvider configProvider)
    {  	    	
    	// When creating custom dimensions don't override biomes that already exist in other worlds
        if(!isMainWorld)
        {
        	//BiomeConfig existingBiomeConfig = ((ForgeEngine)TerrainControl.getEngine()).worldLoader.getConfig(biomeConfig.getName());
        	LocalBiome existingBiome = TerrainControl.getBiomeAllWorlds(biomeConfig.getName());
        	if(existingBiome != null && existingBiome.getBiomeConfig() != null)
        	{
        		biomeConfig = existingBiome.getBiomeConfig();
        	}
        }        
    	
		// Make an exception for the hell and sky biomes. 
		// The hell and end chunk providers refer specifically to 
		// Biomes.HELL and Biomes.SKY and query the biome registry
		// for them. Other biomes are not referred to in this way.
    	if(biomeConfig.getName().equals("Hell"))
    	{
    		ForgeBiome forgeBiome = new ForgeBiome(Biomes.HELL, biomeConfig, new BiomeIds(8,8));
    		this.biomeNames.put("Hell", forgeBiome);    		
    		return forgeBiome;
		}
    	if(biomeConfig.getName().equals("Sky"))
    	{
    		ForgeBiome forgeBiome = new ForgeBiome(Biomes.SKY, biomeConfig, new BiomeIds(9,9));
    		this.biomeNames.put("Sky", forgeBiome);    		
    		return forgeBiome;
		}
    	if(biomeConfig.getName().equals("The Void"))
    	{
    		ForgeBiome forgeBiome = new ForgeBiome(Biomes.VOID, biomeConfig, new BiomeIds(127,127));
    		this.biomeNames.put("The Void", forgeBiome);            
    		return forgeBiome;
		}
    	
    	ForgeBiome forgeBiome = (ForgeBiome)TerrainControl.getBiomeAllWorlds(biomeConfig.getName());

    	Biome biome = TXBiome.getOrCreateBiome(biomeConfig, biomeIds, isMainWorld);    
    	
    	if(forgeBiome == null)
    	{
	    	// Always try to register biomes and create Biome Configs. Biomes with id's > 255 are registered
	    	// only for biome -> id queries, any (saved)id -> biome query will return the ReplaceToBiomeName biome.
	        
	        Biome existingBiome = Biome.getBiome(biomeIds.getSavedId());              
	    	//Biome biome = BiomeGenCustom.getOrCreateBiome(biomeConfig, biomeIds);
	        int requestedGenerationId = biomeIds.getGenerationId();
	        int allocatedGenerationId = Biome.REGISTRY.underlyingIntegerMap.getId(biome);       
	        
	        if (requestedGenerationId != allocatedGenerationId)
	        {
	        	// When creating the ForgeBiome later in this method use the actual id's
	        	biomeIds = new BiomeIds(requestedGenerationId, allocatedGenerationId);
	        	
	            if (requestedGenerationId < 256 && allocatedGenerationId >= 256)
	            {
	                throw new RuntimeException("Could not allocate the requested id " + requestedGenerationId + " for biome " + biomeConfig.getName() + ". All available id's under 256 have been allocated\n"
	                    + ". To proceed, adjust your WorldConfig or use the ReplaceToBiomeName feature to make the biome virtual.");
	            }
	            TerrainControl.log(LogMarker.TRACE, "Asked to register {} with id {}, but succeeded with id {}", biomeConfig.getName(), requestedGenerationId, allocatedGenerationId);
	        } else {
	            TerrainControl.log(LogMarker.TRACE, "Registered {} with id {}", biomeConfig.getName(), allocatedGenerationId);
	        }
	
	        forgeBiome = new ForgeBiome(biome, biomeConfig, biomeIds);
	        
	        registerBiomeInBiomeDictionary(biome, existingBiome, biomeConfig, configProvider);
    	}
        
        this.biomeNames.put(biome.getBiomeName(), forgeBiome);
        
        return forgeBiome;
    }
    
    private void registerBiomeInBiomeDictionary(Biome biome, Biome sourceBiome, BiomeConfig biomeConfig, ConfigProvider configProvider)
    {        	
        // Add inherited BiomeDictId's for replaceToBiomeName. Biome dict id's are stored twice, 
        // there is 1 list of biomedict types per biome id and one list of biomes (not id's) per biome dict type.
    	
        ArrayList<Type> types = new ArrayList<Type>();
        if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0)
        {
        	// Inherit from an existing biome        	
    		LocalBiome replaceToBiome = configProvider.getBiomeByIdOrNull(Biome.getIdForBiome(sourceBiome != null ? sourceBiome : biome));
            // For forge make sure all dimensions are queried since the biome we're looking for may be owned by another dimension
        	if(replaceToBiome == null)
        	{
        		int replaceToBiomeId = Biome.getIdForBiome(sourceBiome != null ? sourceBiome : biome);
        		replaceToBiome = TerrainControl.getBiomeAllWorlds(replaceToBiomeId);
        	}
    		if(replaceToBiome != null && replaceToBiome.getBiomeConfig().biomeDictId != null)
    		{
    			types = getTypesList(replaceToBiome.getBiomeConfig().biomeDictId.split(","));
    		}
        } else {
        	// If not replaceToBiomeName then attach BiomeDictId
	        if(biomeConfig.biomeDictId != null && biomeConfig.biomeDictId.trim().length() > 0)
	        {
	        	types = getTypesList(biomeConfig.biomeDictId.split(","));			  
	        }	       	       
        }
        
    	Type[] typeArr = new Type[types.size()];
		types.toArray(typeArr);
		
		if(!ForgeRegistries.BIOMES.containsValue(biome))
		{
			TerrainControl.log(LogMarker.WARN, "Biome " + biome.getBiomeName() + " could not be found in the registry. This could be because it is a virtual biome (id > 255) but does not have a ReplaceToBiomeName configured.");
		}
		
    	BiomeDictionary.addTypes(biome, typeArr);
    }
    
    private ArrayList<Type> getTypesList(String[] typearr)
    {
    	ArrayList<Type> types = new ArrayList<Type>();
		for(String typeString : typearr)
		{
			if(typeString != null && typeString.trim().length() > 0)
			{
		        Type type = null;
				typeString = typeString.trim();
		        try
		        {
		        	type = Type.getType(typeString, null);
		        }
		        catch(Exception ex)
		        {
		        	TerrainControl.log(LogMarker.WARN, "Can't find BiomeDictId: \"" + typeString + "\".");
		        }
		        if(type != null)
		        {
		        	types.add(type);
		        }
			}
		}
		return types;
    }
    
    @Override
    public int getMaxBiomesCount()
    {
        return MAX_BIOMES_COUNT;
    }

    @Override
    public int getMaxSavedBiomesCount()
    {
        return MAX_SAVED_BIOMES_COUNT;
    }

    @Override
    public int getFreeBiomeId()
    {
    	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
        //return nextBiomeId++;
    }
    
    @Override
    public ArrayList<LocalBiome> getAllBiomes()
    {
    	ArrayList<LocalBiome> biomes = new ArrayList<LocalBiome>();
		for(LocalBiome biome : this.settings.getBiomeArray())
		{
			biomes.add(biome);
		}
    	return biomes;
    }
    
    @Override
    public ForgeBiome getBiomeById(int id) throws BiomeNotFoundException
    {
    	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
    }

    @Override
    public ForgeBiome getBiomeByIdOrNull(int id)
    {
        return (ForgeBiome) this.settings.getBiomeByIdOrNull(id);
    }
    
    @Override
    public LocalBiome getBiomeByNameOrNull(String name)
    {     
        return this.biomeNames.get(name);
    }
    
    @Override
    public Collection<BiomeLoadInstruction> getDefaultBiomes()
    {
        // Loop through all default biomes and create the default
        // settings for them
        List<BiomeLoadInstruction> standardBiomes = new ArrayList<BiomeLoadInstruction>();
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            int id = defaultBiome.Id;
            BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(ForgeMojangSettings.fromId(id), STANDARD_WORLD_HEIGHT);
            standardBiomes.add(instruction);
        }

        return standardBiomes;
    }

    @Override
    public void prepareDefaultStructures(int chunkX, int chunkZ, boolean dry)
    {
        WorldConfig worldConfig = this.settings.getWorldConfig();
        if (worldConfig.strongholdsEnabled)
            this.strongholdGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.mineshaftsEnabled)
            this.mineshaftGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.villagesEnabled && dry)
            this.villageGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.rareBuildingsEnabled)
            this.rareBuildingGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.netherFortressesEnabled)
            this.netherFortressGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.oceanMonumentsEnabled)
            this.oceanMonumentGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.woodLandMansionsEnabled)
            this.woodLandMansionGen.generate(this.world, chunkX, chunkZ, null);
    }

    @Override
    public boolean placeDungeon(Random rand, int x, int y, int z)
    {
        return this.dungeonGen.generate(this.world, rand, new BlockPos(x, y, z));
    }

    @Override
    public boolean placeFossil(Random rand, ChunkCoordinate chunkCoord)
    {
        return this.fossilGen.generate(this.world, rand, new BlockPos(chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ()));
    }

    @Override
    public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
    {
        BlockPos blockPos = new BlockPos(x, y, z);
        switch (type)
        {
            case Tree:
                return this.tree.generate(this.world, rand, blockPos);
            case BigTree:
                return this.bigTree.generate(this.world, rand, blockPos);
            case Forest:
            case Birch:
                return this.birchTree.generate(this.world, rand, blockPos);
            case TallBirch:
                return this.longBirchTree.generate(this.world, rand, blockPos);
            case HugeMushroom:
                if (rand.nextBoolean())
                {
                    return this.hugeBrownMushroom.generate(this.world, rand, blockPos);
                } else
                {
                    return this.hugeRedMushroom.generate(this.world, rand, blockPos);
                }
            case HugeRedMushroom:
                return this.hugeRedMushroom.generate(this.world, rand, blockPos);
            case HugeBrownMushroom:
                return this.hugeBrownMushroom.generate(this.world, rand, blockPos);
            case SwampTree:
                return this.swampTree.generate(this.world, rand, blockPos);
            case Taiga1:
                return this.taigaTree1.generate(this.world, rand, blockPos);
            case Taiga2:
                return this.taigaTree2.generate(this.world, rand, blockPos);
            case JungleTree:
                return this.jungleTree.generate(this.world, rand, blockPos);
            case GroundBush:
                return this.groundBush.generate(this.world, rand, blockPos);
            case CocoaTree:
                return this.cocoaTree.generate(this.world, rand, blockPos);
            case Acacia:
                return this.acaciaTree.generate(this.world, rand, blockPos);
            case DarkOak:
                return this.darkOakTree.generate(this.world, rand, blockPos);
            case HugeTaiga1:
                return this.hugeTaigaTree1.generate(this.world, rand, blockPos);
            case HugeTaiga2:
                return this.hugeTaigaTree2.generate(this.world, rand, blockPos);
            default:
                throw new RuntimeException("Failed to handle tree of type " + type.toString());
        }
    }

    @Override
    public boolean placeDefaultStructures(Random rand, ChunkCoordinate chunkCoord)
    {
        ChunkPos chunkCoordIntPair = new ChunkPos(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        WorldConfig worldConfig = this.settings.getWorldConfig();

        boolean isVillagePlaced = false;
        if (worldConfig.strongholdsEnabled)
            this.strongholdGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.mineshaftsEnabled)
            this.mineshaftGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.villagesEnabled)
            isVillagePlaced = this.villageGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.rareBuildingsEnabled)
            this.rareBuildingGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.netherFortressesEnabled)
            this.netherFortressGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.oceanMonumentsEnabled)
            this.oceanMonumentGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.woodLandMansionsEnabled)
            this.woodLandMansionGen.generateStructure(this.world, rand, chunkCoordIntPair);

        return isVillagePlaced;
    }

    @Override
    public void replaceBlocks(ChunkCoordinate chunkCoord)
    {
        if (!this.settings.getWorldConfig().BiomeConfigsHaveReplacement)
        {
            // Don't waste time here, ReplacedBlocks is empty everywhere
            return;
        }

        // Get cache
        Chunk[] cache = getChunkCache(chunkCoord);

        // Replace the blocks
        for (int i = 0; i < 4; i++)
        {
            replaceBlocks(cache[i], 0, 0, 16);
        }
    }

    private void replaceBlocks(Chunk rawChunk, int startXInChunk, int startZInChunk, int size)
    {
        int endXInChunk = startXInChunk + size;
        int endZInChunk = startZInChunk + size;
        int worldStartX = rawChunk.xPosition * 16;
        int worldStartZ = rawChunk.zPosition * 16;

        ExtendedBlockStorage[] sectionsArray = rawChunk.getBlockStorageArray();

        for (ExtendedBlockStorage section : sectionsArray)
        {
            if (section == null)
                continue;

            for (int sectionX = startXInChunk; sectionX < endXInChunk; sectionX++)
            {
                for (int sectionZ = startZInChunk; sectionZ < endZInChunk; sectionZ++)
                {
                    LocalBiome biome = this.getBiome(worldStartX + sectionX, worldStartZ + sectionZ);
                    if (biome != null && biome.getBiomeConfig().replacedBlocks.hasReplaceSettings())
                    {
                        LocalMaterialData[][] replaceArray = biome.getBiomeConfig().replacedBlocks.compiledInstructions;
                        for (int sectionY = 0; sectionY < 16; sectionY++)
                        {
                            IBlockState block = section.getData().get(sectionX, sectionY, sectionZ);
                            int blockId = Block.getIdFromBlock(block.getBlock());
                            if (replaceArray[blockId] == null)
                                continue;

                            int y = section.getYLocation() + sectionY;
                            if (y >= replaceArray[blockId].length)
                                break;

                            ForgeMaterialData replaceTo = (ForgeMaterialData) replaceArray[blockId][y];
                            if (replaceTo == null || replaceTo.getBlockId() == blockId)
                                continue;

                            section.set(sectionX, sectionY, sectionZ, replaceTo.internalBlock());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void placePopulationMobs(LocalBiome biome, Random random, ChunkCoordinate chunkCoord)
    {
        WorldEntitySpawner.performWorldGenSpawning(this.getWorld(), ((ForgeBiome) biome).getHandle(),
                chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter(), ChunkCoordinate.CHUNK_X_SIZE,
                ChunkCoordinate.CHUNK_Z_SIZE, random);
    }

    private Chunk getChunk(int x, int y, int z)
    {
        if (y < TerrainControl.WORLD_DEPTH || y >= TerrainControl.WORLD_HEIGHT)
            return null;

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if (this.chunkCache == null)
        {
            // Blocks requested outside population step
            // (Tree growing, /otg spawn, etc.)
            return this.world.getChunkFromChunkCoords(chunkX, chunkZ);
        }

        // Restrict to chunks we are currently populating
        Chunk topLeftCachedChunk = this.chunkCache[0];
        int indexX = (chunkX - topLeftCachedChunk.xPosition);
        int indexZ = (chunkZ - topLeftCachedChunk.zPosition);
        if ((indexX == 0 || indexX == 1) && (indexZ == 0 || indexZ == 1))
        {
            return this.chunkCache[indexX | (indexZ << 1)];
        } else
        {
            // Outside area
            if (this.settings.getWorldConfig().populationBoundsCheck)
            {
                return null;
            }

            return this.getLoadedChunkWithoutMarkingActive(chunkX, chunkZ);
        }
    }

    @Override
    public int getLiquidHeight(int x, int z)
    {
        for (int y = getHighestBlockYAt(x, z) - 1; y > 0; y--)
        {
            LocalMaterialData material = getMaterial(x, y, z);
            if (material.isLiquid())
            {
                return y + 1;
            } else if (material.isSolid())
            {
                // Failed to find a liquid
                return -1;
            }
        }
        return -1;
    }

    @Override
    public int getSolidHeight(int x, int z)
    {
        for (int y = getHighestBlockYAt(x, z) - 1; y > 0; y--)
        {
            LocalMaterialData material = getMaterial(x, y, z);
            if (material.isSolid())
            {
                return y + 1;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return true;
        }
        return chunk.getBlockState(x & 0xF, y, z & 0xF).getMaterial().equals(Material.AIR);
    }

    @Override
    public LocalMaterialData getMaterial(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null || y < TerrainControl.WORLD_DEPTH || y >= TerrainControl.WORLD_HEIGHT)
        {
            return ForgeMaterialData.ofMinecraftBlock(Blocks.AIR);
        }

        // There's no chunk.getType(x,y,z), only chunk.getType(BlockPosition)
        // so we use this little hack.
        // Creating a block position for every block lookup is expensive and
        // a major cause of Minecraft 1.8's performance degradation:
        // http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1272953-optifine?comment=43757
        ExtendedBlockStorage section = chunk.getBlockStorageArray()[y >> 4];
        if (section == null)
        {
            return ForgeMaterialData.ofMinecraftBlock(Blocks.AIR);
        }

        IBlockState blockState = section.get(x & 0xF, y & 0xF, z & 0xF);
        return ForgeMaterialData.ofMinecraftBlockState(blockState);
    }

    @Override
    public void setBlock(int x, int y, int z, LocalMaterialData material)
    {      	
        /*
         * This method usually breaks on every Minecraft update. Always check
         * whether the names are still correct. Often, you'll also need to
         * rewrite parts of this method for newer block place logic.
         */

        if (y < TerrainControl.WORLD_DEPTH || y >= TerrainControl.WORLD_HEIGHT)
        {
            return;
        }

        DefaultMaterial defaultMaterial = material.toDefaultMaterial();
        
        // TODO: Fix this
        if(defaultMaterial.equals(DefaultMaterial.DIODE_BLOCK_ON))
        {
        	material = ForgeMaterialData.ofDefaultMaterial(DefaultMaterial.DIODE_BLOCK_OFF, material.getBlockData());
        }
        else if(defaultMaterial.equals(DefaultMaterial.REDSTONE_COMPARATOR_ON))
        {
        	material = ForgeMaterialData.ofDefaultMaterial(DefaultMaterial.REDSTONE_COMPARATOR_OFF, material.getBlockData());
        }
        //else if(defaultMaterial.equals(DefaultMaterial.REDSTONE_LAMP_ON))
        {
        	//material = ForgeMaterialData.ofDefaultMaterial(DefaultMaterial.REDSTONE_LAMP_OFF, material.getBlockData());
        }
        //else if(defaultMaterial.equals(DefaultMaterial.REDSTONE_TORCH_ON))
        {
        	//material = ForgeMaterialData.ofDefaultMaterial(DefaultMaterial.REDSTONE_TORCH_OFF, material.getBlockData());
        }
        
        IBlockState newState = ((ForgeMaterialData) material).internalBlock();
        
        BlockPos pos = new BlockPos(x, y, z);
        
        //DefaultMaterial defaultMaterial = material.toDefaultMaterial();
                
        // Get chunk from (faster) custom cache
        Chunk chunk = this.getChunk(x, y, z);

        if (chunk == null)
        {
            // Chunk is unloaded
            //return;
        	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
        }

        IBlockState oldState = this.world.getBlockState(pos);
        int oldLight = oldState.getLightValue(this.world, pos);
        int oldOpacity = oldState.getLightOpacity(this.world, pos);
        
        IBlockState iblockstate = chunk.setBlockState(pos, newState);
       
        if (iblockstate == null)
        {
        	return; // Happens when block to place is the same as block being placed? TODO: Is that the only time this happens?
        }
        
        // Relight and update players
        if (newState.getLightOpacity(this.world, pos) != oldOpacity || newState.getLightValue(this.world, pos) != oldLight)
        {
            this.world.profiler.startSection("checkLight");
            this.world.checkLight(pos);
            this.world.profiler.endSection();
        }
        
        this.world.markAndNotifyBlock(pos, chunk, iblockstate, newState, 2 | 16);
    }
        
    @Override
    public int getHighestBlockYAt(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
        {
            return -1;
        }

        int y = chunk.getHeightValue(x & 0xf, z & 0xf);

        // Fix for incorrect light map
        // TODO: Fix this properly?
        boolean incorrectHeightMap = false;
        while (y < getHeightCap() && chunk.getBlockState(x, y, z).getMaterial().blocksLight())
        {
            y++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap)
        {
            // Let Minecraft know that it made an error
            this.world.checkLight(new BlockPos(x, y, z));
        }

        return y;
    }

    @Override
    public void startPopulation(ChunkCoordinate chunkCoord)
    {
        if (this.chunkCache != null && this.settings.getWorldConfig().populationBoundsCheck)
        {
            throw new IllegalStateException("Chunk is already being populated." + " This may be a bug in Open Terrain Generator, but it may also be" + " another mod that is poking in unloaded chunks. Set" + " PopulationBoundsCheck to false in the WorldConfig to" + " disable this error.");
        }

        // Initialize cache
        this.chunkCache = loadFourChunks(chunkCoord);
    }

    private Chunk[] getChunkCache(ChunkCoordinate topLeft)
    {
        if (this.chunkCache == null || !topLeft.coordsMatch(this.chunkCache[0].xPosition, this.chunkCache[0].zPosition))
        {
            // Cache is invalid, most likely because two chunks are being populated at once
            if (this.settings.getWorldConfig().populationBoundsCheck)
            {
                // ... but this can never happen, as startPopulation() checks for this if populationBoundsCheck is set
                // to true. So we must have a bug.
                throw new IllegalStateException("chunkCache is null! You've got a bug!");
            } else
            {
                // Use a temporary cache, best we can do
                return this.loadFourChunks(topLeft);
            }
        }
        return this.chunkCache;
    }

    private Chunk[] loadFourChunks(ChunkCoordinate topLeft)
    {
        Chunk[] chunkCache = new Chunk[4];
        for (int indexX = 0; indexX <= 1; indexX++)
        {
            for (int indexZ = 0; indexZ <= 1; indexZ++)
            {
                chunkCache[indexX | (indexZ << 1)] = this.world.getChunkFromChunkCoords(
                        topLeft.getChunkX() + indexX,
                        topLeft.getChunkZ() + indexZ
                );
            }
        }
        return chunkCache;
    }

    @Override
    public void endPopulation()
    {
        if (this.chunkCache == null && this.settings.getWorldConfig().populationBoundsCheck)
        {
            throw new IllegalStateException("Chunk is not being populated."
                    + " This may be a bug in Open Terrain Generator, but it may also be"
                    + " another mod that is poking in unloaded chunks. Set"
                    + " PopulationBoundsCheck to false in the WorldConfig to"
                    + " disable this error.");
        }
        this.chunkCache = null;
    }

    @Override
    public int getLightLevel(int x, int y, int z)
    {
        // Actually, this calculates the block and skylight as it were day.
        return this.world.getLight(new BlockPos(x, y, z));
    }

    @Override
    public boolean isLoaded(int x, int y, int z)
    {
        return getChunk(x, y, z) != null;
    }

    @Override
    public ConfigProvider getConfigs()
    {
        return this.settings;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public long getSeed()
    {
        return this.seed;
    }

    @Override
    public int getHeightCap()
    {
        return this.settings.getWorldConfig().worldHeightCap;
    }

    @Override
    public int getHeightScale()
    {
        return this.settings.getWorldConfig().worldHeightScale;
    }

    public TXChunkGenerator getChunkGenerator()
    {
        return this.generator;
    }

    @SideOnly(Side.CLIENT)
    public void provideClientConfigsBukkit(WorldClient world, ClientConfigProvider config)
    {
        this.settings = config;
        this.world = world;
        this.seed = world.getSeed();
    }
    
    @SideOnly(Side.CLIENT)
    public void provideClientConfigs(ClientConfigProvider config)
    {
        this.settings = config;
    }

    @SideOnly(Side.CLIENT)
    public void provideClientWorld(World world)
    {
        this.world = world;
        this.seed = world.getSeed();
    }
    
    /**
     * Call this method when the configs are loaded.
     * @param configs The configs.
     */
    public void provideConfigs(ServerConfigProvider configs)
    {
        Preconditions.checkNotNull(configs, "configs");
        this.settings = configs;
    }

    /**
     * Call this method when the Minecraft world is loaded. Call this method
     * after {@link #provideConfigs(ServerConfigProvider)} has been called.
     * @param world The Minecraft world.
     */
    public void provideWorldInstance(WorldServer world)
    {
        Preconditions.checkNotNull(world, "world");
        Preconditions.checkState(this.world == null, "world was already initialized");
        Preconditions.checkState(this.settings instanceof ServerConfigProvider,
                "server configs must be provided first");

        ServerConfigProvider configs = (ServerConfigProvider) this.settings;
        
        // Custom dimension settings
        // world is unique for this dimension however its worldinfo 
        // was derived from the main world and has the same seed
        // configure the correct seed for this dimension.
		// If the world is a TCWorldServerMulti then it was created
		// by a console command and has already had its seed set
        if(!isMainWorld && !(world instanceof TXWorldServerMulti))
        {
        	// TODO: Use seed from Dimensions.txt instead <-- WHy, seeds in dims seem to be working fine?
            long seedIn = (long) Math.floor((Math.random() * Long.MAX_VALUE));
            if(configs.getWorldConfig().worldSeed != null && configs.getWorldConfig().worldSeed.trim().length() > 0)
            {
                try
                {
                	seedIn = configs.getWorldConfig().worldSeed == null || configs.getWorldConfig().worldSeed.trim().length() == 0 ? (long) Math.floor((Math.random() * Long.MAX_VALUE)) : Long.parseLong(configs.getWorldConfig().worldSeed);
                }
            	catch(NumberFormatException ex)
                {
            		TerrainControl.log(LogMarker.ERROR, "WorldConfig for world \"" + world.getWorldInfo().getWorldName() + "\" has value \"" + configs.getWorldConfig().worldSeed + "\" for worldSeed which cannot be parsed as a number. Using a random seed instead.");
                }
            }
            
    		GameType gameType = world.getWorldInfo().getGameType();
    		boolean enableMapFeatures = world.getWorldInfo().isMapFeaturesEnabled(); // Whether the map features (e.g. strongholds) generation is enabled or disabled.
    		boolean hardcoreMode = world.getWorldInfo().isHardcoreModeEnabled();
    		WorldType worldTypeIn = world.getWorldType();
    		
    		String generatorOptions = world.getWorldInfo().getGeneratorOptions();
    		boolean enableCommands = world.getWorldInfo().areCommandsAllowed();
    	
    		WorldSettings settings = new WorldSettings(seedIn, gameType, enableMapFeatures, hardcoreMode, worldTypeIn);
    		settings.setGeneratorOptions(generatorOptions);
    		if(enableCommands) { settings.enableCommands(); }
    		
    		WorldInfo worldInfo = new WorldInfo(settings, world.getWorldInfo().getWorldName());        	
        	
    		try {
    			Field[] fields = World.class.getDeclaredFields();
    			for(Field field : fields)
    			{
    				Class<?> fieldClass = field.getType();  				
    				if(fieldClass.equals(net.minecraft.world.storage.WorldInfo.class))
    				{
    			        field.setAccessible(true);			        
    			        field.set(world, worldInfo);
    			        break;
    				}    				
    			}
    		} catch (SecurityException e) {
    			e.printStackTrace();
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    		}
        }
        //
        
        this.world = world;
        this.seed = world.getWorldInfo().getSeed();
        world.setSeaLevel(configs.getWorldConfig().waterLevelMax);

        this.structureCache = new CustomObjectStructureCache(this);
        this.dataFixer = DataFixesManager.createFixer();

        this.dungeonGen = new WorldGenDungeons();
        this.fossilGen = new WorldGenFossils();
        this.strongholdGen = new TXStrongholdGen(configs);

        this.villageGen = new TXVillageGen(configs);
        this.mineshaftGen = new TXMineshaftGen();
        this.rareBuildingGen = new TXRareBuildingGen(configs);
        this.netherFortressGen = new TXNetherFortressGen();
        this.oceanMonumentGen = new TXOceanMonumentGen(configs);
        this.woodLandMansionGen = new TXWoodLandMansionGen(configs);

        IBlockState jungleLog = Blocks.LOG.getDefaultState()
                .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
        IBlockState jungleLeaves = Blocks.LEAVES.getDefaultState()
                .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
                .withProperty(BlockLeaves.CHECK_DECAY, false);

        this.tree = new WorldGenTrees(false);
        this.acaciaTree = new WorldGenSavannaTree(false);
        this.cocoaTree = new WorldGenTrees(false, 5, jungleLog, jungleLeaves, true);
        this.bigTree = new WorldGenBigTree(false);
        this.birchTree = new WorldGenBirchTree(false, false);
        this.darkOakTree = new WorldGenCanopyTree(false);
        this.longBirchTree = new WorldGenBirchTree(false, true);
        this.swampTree = new WorldGenSwamp();
        this.taigaTree1 = new WorldGenTaiga1();
        this.taigaTree2 = new WorldGenTaiga2(false);
        this.hugeRedMushroom = new WorldGenBigMushroom(Blocks.RED_MUSHROOM_BLOCK);
        this.hugeBrownMushroom = new WorldGenBigMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
        this.hugeTaigaTree1 = new WorldGenMegaPineTree(false, false);
        this.hugeTaigaTree2 = new WorldGenMegaPineTree(false, true);
        this.jungleTree = new WorldGenMegaJungle(false, 10, 20, jungleLog, jungleLeaves);
        this.groundBush = new WorldGenShrub(jungleLog, jungleLeaves);

        this.generator = new TXChunkGenerator(this);
    }

    public void setBiomeGenerator(BiomeGenerator generator)
    {
        this.biomeGenerator = generator;
    }

    public World getWorld()
    {
        return this.world;
    }

    @Override
    public LocalBiome getCalculatedBiome(int x, int z)
    {
    	return TerrainControl.isForge ? TerrainControl.getBiomeAllWorlds(this.biomeGenerator.getBiome(x, z)) : getBiomeById(this.biomeGenerator.getBiome(x, z));
    }

    @Override
    public LocalBiome getBiome(int x, int z)
    {    	   	
        if (this.settings.getWorldConfig().populateUsingSavedBiomes)
        {
            return getSavedBiome(x, z);
        } else
        {
            return getCalculatedBiome(x, z);
        }            
    }

    @Override
    public LocalBiome getSavedBiome(int x, int z) throws BiomeNotFoundException
    {
    	BlockPos pos = new BlockPos(x, 0, z);
    	Biome biome = this.world.getBiome(pos);
    	int biomeId;
    	if(biome instanceof TXBiome)
    	{
    		biomeId = ((TXBiome)biome).generationId;
    	} else {
    		biomeId = Biome.getIdForBiome(biome); // Non-TC biomes don't have a generationId, only a saved id
    	}
    	//ForgeBiome forgeBiome = getBiomeById(biomeId);
    	ForgeBiome forgeBiome = (ForgeBiome) TerrainControl.getBiomeAllWorlds(biomeId);
    	
        return forgeBiome;
    }

    @Override
    public void attachMetadata(int x, int y, int z, NamedBinaryTag tag)
    {
        // Convert Tag to a native nms tag
        NBTTagCompound nmsTag = NBTHelper.getNMSFromNBTTagCompound(tag);
        // Add the x, y and z position to it
        nmsTag.setInteger("x", x);
        nmsTag.setInteger("y", y);
        nmsTag.setInteger("z", z);
        // Update to current Minecraft format (maybe we want to do this at
        // server startup instead, and then save the result?)       
        // TODO: Use datawalker instead
        //nmsTag = this.dataFixer.process(FixTypes.BLOCK_ENTITY, nmsTag, -1);
        nmsTag = this.dataFixer.process(FixTypes.BLOCK_ENTITY, nmsTag);
        
        // Add that data to the current tile entity in the world
        TileEntity tileEntity = this.world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity != null)
        {
            tileEntity.readFromNBT(nmsTag);
        } else {
            TerrainControl.log(LogMarker.DEBUG,
                    "Skipping tile entity with id {}, cannot be placed at {},{},{} on id {}", nmsTag.getString("id"), x,
                    y, z, getMaterial(x, y, z));
        }
    }

    @Override
    public NamedBinaryTag getMetadata(int x, int y, int z)
    {
        TileEntity tileEntity = this.world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity == null)
        {
            return null;
        }
        NBTTagCompound nmsTag = new NBTTagCompound();
        tileEntity.writeToNBT(nmsTag);
        nmsTag.removeTag("x");
        nmsTag.removeTag("y");
        nmsTag.removeTag("z");
        return NBTHelper.getNBTFromNMSTagCompound(null, nmsTag);
    }

    @Override
    public CustomObjectStructureCache getStructureCache()
    {
        return this.structureCache;
    }

    @Override
    public BiomeGenerator getBiomeGenerator()
    {
        return this.biomeGenerator;
    }

    @Override
    public SpawnableObject getMojangStructurePart(String name)
    {
        ResourceLocation resourceLocation = new ResourceLocation(name);
        TemplateManager mojangStructureParts = this.world.getSaveHandler().getStructureTemplateManager();
        Template mojangStructurePart = mojangStructureParts.getTemplate(this.world.getMinecraftServer(), resourceLocation);
        if (mojangStructurePart == null)
        {
            return null;
        }
        return new MojangStructurePart(name, mojangStructurePart);
    }

    public Chunk getLoadedChunkWithoutMarkingActive(int chunkX, int chunkZ)
    {
        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) this.world.getChunkProvider();
        long i = ChunkPos.asLong(chunkX, chunkZ);
        return (Chunk) chunkProviderServer.id2ChunkMap.get(i);
    }
    
    private void cacheVanillaBiomes()
    {
        if(!vanillaBiomesCached)
        {        	
	        // Cache original vanilla biomes, they will be replaced
        	// in the biome registry with TC biomes so we will keep
        	// a cache of them to use as default values for new worlds
        	// (the vanilla biomes include stuff added by mods such as mobs)
	        for (DefaultBiome defaultBiome : DefaultBiome.values())
	        {
	            int biomeId = defaultBiome.Id;
	            Biome oldBiome = Biome.getBiome(biomeId);
	            vanillaBiomes[biomeId] = oldBiome;
	            
	            // Cache resource locations for default/vanilla biomes so we can use these 
	            // to replace the biomes in the biome registry later.
	    		for(ResourceLocation ob : Biome.REGISTRY.registryObjects.keySet())
	    		{
	    			int vanillaBiomeId = Biome.getIdForBiome(Biome.REGISTRY.getObject(ob));
	    			if(biomeId == vanillaBiomeId)
	    			{
	    				vanillaResouceLocations.put(vanillaBiomeId, ob);
	    				break;
	    			}	    			
	    		}
	        }
	        vanillaBiomesCached = true;
        }
    }
     
    /**
     * Used by mob inheritance code. Used to inherit default mob spawning settings (including those added by other mods)
     * @param biomeConfigStub
     */
	public void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub)
	{
    	Biome biome = null;
    	String biomeName = biomeConfigStub.getBiomeName();

    	for (Biome vanillaBiome : vanillaBiomes)
        {
        	if (vanillaBiome != null && vanillaBiome.getBiomeName().equals(biomeName) && !(vanillaBiome instanceof TXBiome))
            {
            	biome = vanillaBiome;
            	break;
            }
        }
    	if(biome != null)
    	{
			// Merge the vanilla biome's mob spawning lists with the mob spawning lists from the BiomeConfig. 
    		// Mob spawning settings for the same creature will not be inherited (so BiomeConfigs can override vanilla mob spawning settings).
			// We also inherit any mobs that have been added to vanilla biomes' mob spawning lists by other mods.
			biomeConfigStub.spawnMonstersMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnMonstersMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityCategory.MONSTER));
			biomeConfigStub.spawnCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityCategory.CREATURE));
			biomeConfigStub.spawnAmbientCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnAmbientCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityCategory.AMBIENT_CREATURE));
			biomeConfigStub.spawnWaterCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnWaterCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityCategory.WATER_CREATURE));						
    	}
	}
	
	public void unRegisterBiomes()
	{		
		// Unregister only the biomes registered by this world
		for(LocalBiome localBiome : this.biomeNames.values())
		{				
			// Make an exception for the hell and sky biomes. 
			// The hell and end chunk providers refer specifically to 
			// Biomes.HELL and Biomes.SKY and query the biome registry
			// for them. Other biomes are not referred to in this way.
			if(localBiome.getName().equals("The Void") || localBiome.getName().equals("The End") || localBiome.getName().equals("Hell") || localBiome.getName().equals("Sky")) { continue; }			
			
			if(((ForgeEngine)TerrainControl.getEngine()).worldLoader.isConfigUnique(localBiome.getBiomeConfig().getName()))
			{			
		        //int generationId = localBiome.getIds().getGenerationId();
		        
		        // 0-39 and 127-167 are vanilla biomes   
		        //if((generationId >= 0 && generationId <= 39) || (generationId >= 127 && generationId <= 167))
		        {
		        	// Don't unregister vanilla biomes (there will always be a dimension that uses them) 
		        	//continue;
		        }
		        
		        String biomeNameForRegistry = StringHelper.toComputerFriendlyName(localBiome.getName());                      
		        String resourceDomain = PluginStandardValues.PLUGIN_NAME.toLowerCase();		        
		        ResourceLocation registryKey = new ResourceLocation(resourceDomain, biomeNameForRegistry);
		        
		        ((ForgeEngine)TerrainControl.getEngine()).unRegisterForgeBiome(registryKey);
			}
		}
		
		((ForgeEngine)TerrainControl.getEngine()).worldLoader.clearBiomeDictionary(this);
	}
	
    @Override
    public void SpawnEntity(EntityFunction entityData)
    {
    	if(TerrainControl.getPluginConfig().SpawnLog)
    	{
    		TerrainControl.log(LogMarker.INFO, "Attempting to spawn BO3 Entity() " + entityData.groupSize + " x " + entityData.mobName + " at " + entityData.x + " " + entityData.y + " " + entityData.z);
    	}
    	
    	Random rand = new Random();
    	
		String mobTypeName = entityData.mobName;
		int groupSize = entityData.groupSize;
		String nameTag = entityData.nameTagOrNBTFileName;
		ResourceLocation entityResourceLocation = null;
		
        Class<?> entityClass = null;
        
        for(ResourceLocation entry : EntityList.getEntityNameList())
        {
        	if(entry.getResourcePath().toLowerCase().trim().replace("entity", "").replace("_", "").equals(mobTypeName.toLowerCase().replace("entity", "").replace("_", "")))
        	{
        		entityResourceLocation = entry;
            	entityClass = EntityList.getClass(entry);
        		break;
        	}
        }
        	                                
        if(entityClass == null)
        {            
        	TerrainControl.log(LogMarker.WARN, "Could not find entity: " + mobTypeName);
        	return;
        }

        Entity entityliving = null;        		
        
        if(entityData.nameTagOrNBTFileName != null && (entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") || entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt")))
        {        	        
        	NBTTagCompound nbttagcompound = new NBTTagCompound();
	        
	        try
	        {	        	
	            NBTBase nbtbase = JsonToNBT.getTagFromJson(entityData.getMetaData());
	
	            if (!(nbtbase instanceof NBTTagCompound))
	            {
		        	TerrainControl.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
		        	return;
	            }
	
	            nbttagcompound = (NBTTagCompound)nbtbase;
	        }
	        catch (NBTException nbtexception)
	        {
	        	TerrainControl.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	        	return;
	        }
	        
	        nbttagcompound.setString("id", entityResourceLocation.getResourcePath());        
	        entityliving = EntityList.createEntityFromNBT(nbttagcompound, world);
        } else {
	        try
	        {
	            entityliving = (Entity) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { world });
	        }
	        catch (Exception exception)
	        {	                                    		
	            exception.printStackTrace();
	            return;
	        }	    
        }
        
        if(entityliving != null)
        {
            EnumCreatureType creatureType = EnumCreatureType.MONSTER;
            if(!entityliving.isCreatureType(creatureType, false))
            {
            	creatureType = EnumCreatureType.CREATURE;
            	if(!entityliving.isCreatureType(creatureType, false))
            	{
            		creatureType = EnumCreatureType.AMBIENT;
            		if(!entityliving.isCreatureType(creatureType, false))
            		{
                		creatureType = EnumCreatureType.WATER_CREATURE;
                		if(!entityliving.isCreatureType(creatureType, false))
                		{
                        	creatureType = EnumCreatureType.CREATURE;
                		}
            		}
            	}                                    	
            }
            
            int j1 = entityData.x;
            int k1 = entityData.y;
            int l1 = entityData.z;
            
            boolean isWaterMob = entityliving instanceof EntityGuardian;
            
            Material material = world.getBlockState(new BlockPos(j1, k1, l1)).getMaterial();
            if (!world.isBlockNormalCube(new BlockPos(j1, k1, l1), false) && (((creatureType == EnumCreatureType.WATER_CREATURE || isWaterMob) && material == Material.WATER) || material == Material.AIR))
            {					                            						                            	                                  			                                    	
	            float f = (float)j1 + 0.5F;
	            float f1 = (float)k1;
	            float f2 = (float)l1 + 0.5F;
	            
	            entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2, rand.nextFloat() * 360.0F, 0.0F);                               
	           
	            if(entityliving instanceof EntityLiving)
	            {	
	            	for(int r = 0; r < groupSize; r++)
	            	{                                    		
	            		if(r != 0)
	            		{
	            	        if(entityData.nameTagOrNBTFileName != null && (entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") || entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt")))
	            	        {        	        
	            	        	NBTTagCompound nbttagcompound = new NBTTagCompound();			                                   
	            		        
	            		        try
	            		        {
	            		            NBTBase nbtbase = JsonToNBT.getTagFromJson(entityData.getMetaData());
	            		
	            		            if (!(nbtbase instanceof NBTTagCompound))
	            		            {
		            		        	TerrainControl.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
		            		        	return;
	            		            }
	            		
	            		            nbttagcompound = (NBTTagCompound)nbtbase;
	            		        }
	            		        catch (NBTException nbtexception)
	            		        {
	            		        	TerrainControl.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	            		        	return;
	            		        }		                                            
	            		        
	            		        nbttagcompound.setString("id", entityResourceLocation.getResourcePath());
	            		        entityliving = EntityList.createEntityFromNBT(nbttagcompound, world);
	            	        } else {        
	            		        try
	            		        {
	            		            entityliving = (Entity) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { world });
	            		        }
	            		        catch (Exception exception)
	            		        {	                                    		
	            		            exception.printStackTrace();
	            		            return;
	            		        }	    
	            	        }                                			                                                                                        
	                        entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2, rand.nextFloat() * 360.0F, 0.0F);
	            		}	

	            		if(entityData.nameTagOrNBTFileName != null && !entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") && !entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt"))
	            		{
	            			if(nameTag != null && nameTag.length() > 0)
	        				{
	        					((EntityLiving) entityliving).setCustomNameTag(nameTag);
	        				}
	            		}
	            		
    					((EntityLiving) entityliving).enablePersistence(); // <- makes sure mobs don't de-spawn
	            		
    			    	if(TerrainControl.getPluginConfig().SpawnLog)
    			    	{
    			    		TerrainControl.log(LogMarker.INFO, "Spawned OK");
    			    	}
    					
    					world.spawnEntity(entityliving);
	            	}
	            } else {                    					                                                						                                                                                    					                                                
	            	for(int r = 0; r < groupSize; r++)
	            	{                                    		
	            		if(r != 0)
	            		{	            			
	                        try
	                        {
	                        	entityliving = (Entity) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { world });
	                        }
	                        catch (Exception exception)
	                        {
	                            exception.printStackTrace();
	                            return;
	                        }
	                        entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2, rand.nextFloat() * 360.0F, 0.0F);                      
	            		}		
	            		
    			    	if(TerrainControl.getPluginConfig().SpawnLog)
    			    	{
    			    		TerrainControl.log(LogMarker.INFO, "Spawned OK");
    			    	}
	            		
	            		world.spawnEntity(entityliving);
	            	}						                                                	
	            }
            }
		}
    }
    
    @Override
    public ChunkCoordinate getSpawnChunk()
    {
    	BlockPos spawnPos = getSpawnPoint();
    	return ChunkCoordinate.fromBlockCoords(spawnPos.getX(), spawnPos.getZ());
    }
    
    public BlockPos getSpawnPoint()
    {    	
    	BlockPos spawnPos = world.provider.getSpawnPoint(); 
    	return new BlockPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    }
    
	public boolean IsInsideWorldBorder(ChunkCoordinate chunk, boolean spawningResources)
	{
		BlockPos spawnPoint = getSpawnPoint();
    	ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());		
		return
			((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius == 0 ||
			(
				chunk.getChunkX() >= spawnChunk.getChunkX() - (((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius - 1)
				&& chunk.getChunkX() <= spawnChunk.getChunkX() + (((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius - 1) - (spawningResources ? 1 : 0) // Resources are spawned at an offset of + half a chunk so stop 1 chunk short of the border
				&& chunk.getChunkZ() >= spawnChunk.getChunkZ() - (((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius - 1)
				&& chunk.getChunkZ() <= spawnChunk.getChunkZ() + (((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius - 1) - (spawningResources ? 1 : 0) // Resources are spawned at an offset of + half a chunk so stop 1 chunk short of the border
			);
	}	
}
