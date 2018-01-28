package com.pg85.otg.forge;

import com.pg85.otg.*;
import com.pg85.otg.configuration.*;
import com.pg85.otg.configuration.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.MojangSettings.EntityCategory;
import com.pg85.otg.customobjects.CustomObjectStructureCache;
import com.pg85.otg.customobjects.bo3.BlockFunction;
import com.pg85.otg.customobjects.bo3.EntityFunction;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.generator.OTGBiome;
import com.pg85.otg.forge.generator.OTGChunkGenerator;
import com.pg85.otg.forge.generator.structure.*;
import com.pg85.otg.forge.util.IOHelper;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;
import com.pg85.otg.forge.util.NBTHelper;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.generator.SpawnableObject;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.NamedBinaryTag;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.minecraftTypes.DefaultBiome;
import com.pg85.otg.util.minecraftTypes.TreeType;

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
import net.minecraft.world.EnumSkyBlock;
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
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class ForgeWorld implements LocalWorld
{
	public int clientDimensionId = 0;

    private OTGChunkGenerator generator;
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

    public OTGStrongholdGen strongholdGen;
    public OTGVillageGen villageGen;
    public OTGMineshaftGen mineshaftGen;
    public OTGRareBuildingGen rareBuildingGen;
    public OTGNetherFortressGen netherFortressGen;
    public OTGOceanMonumentGen oceanMonumentGen;
    public OTGWoodLandMansionGen woodLandMansionGen;

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

    public static HashMap<Integer, ResourceLocation> vanillaResouceLocations = new HashMap<Integer, ResourceLocation>();
    public static Biome[] vanillaBiomes = new Biome[MAX_BIOMES_COUNT];
    public static boolean vanillaBiomesCached = false;

    public boolean isMainWorld = false;

    public ForgeWorld() { }

    public ForgeWorld(String _name, boolean isMainWorld)
    {
		OTG.log(LogMarker.INFO, "Creating world \"" + _name + "\"");

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

        	OTGDimensionManager.UnloadAllCustomDimensionData();
        	((ForgeEngine)OTG.getEngine()).worldLoader.unloadAllWorlds();
	        // Clear the BiomeDictionary (it will be refilled when biomes are loaded in createBiomeFor)
	    	((ForgeEngine)OTG.getEngine()).worldLoader.clearBiomeDictionary(null);
	        ((ForgeEngine)OTG.getEngine()).worldLoader.unRegisterDefaultBiomes();
	        ((ForgeEngine)OTG.getEngine()).worldLoader.unRegisterTCBiomes();

	    	OTGDimensionManager.RemoveTCDims();
        }
    }

    @Override
    public LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds, ConfigProvider configProvider)
    {
    	// When creating custom dimensions don't override biomes that already exist in other worlds
        if(!isMainWorld)
        {
        	//BiomeConfig existingBiomeConfig = ((ForgeEngine)OTG.getEngine()).worldLoader.getConfig(biomeConfig.getName());
        	LocalBiome existingBiome = OTG.getBiomeAllWorlds(biomeConfig.getName());
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

    	ForgeBiome forgeBiome = (ForgeBiome)OTG.getBiomeAllWorlds(biomeConfig.getName());

    	Biome biome = OTGBiome.getOrCreateBiome(biomeConfig, biomeIds, isMainWorld);

    	if(forgeBiome == null) // Could be registered already by another world.
    	{
	    	// Always try to register biomes and create Biome Configs. Biomes with id's > 255 are registered
	    	// only for biome -> id queries, any (saved)id -> biome query will return the ReplaceToBiomeName biome.

	        Biome existingBiome = Biome.getBiome(biomeIds.getSavedId());

            if (biomeIds.getSavedId() >= 256 || biomeIds.getSavedId() < 0)
            {
                throw new RuntimeException("Could not allocate the requested id " + biomeIds.getGenerationId() + " for biome " + biomeConfig.getName() + ". All available id's under 256 have been allocated\n" + ". To proceed, adjust your WorldConfig or use the ReplaceToBiomeName feature to make the biome virtual.");
            }

	        forgeBiome = new ForgeBiome(biome, biomeConfig, biomeIds);

	        registerBiomeInBiomeDictionary(biome, existingBiome, biomeConfig, configProvider);
    	}

        this.biomeNames.put(biome.biomeName, forgeBiome);

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
    		LocalBiome replaceToBiome = configProvider.getBiomeByIdOrNull(((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(sourceBiome != null ? sourceBiome : biome));
            // For forge make sure all dimensions are queried since the biome we're looking for may be owned by another dimension
        	if(replaceToBiome == null)
        	{
        		int replaceToBiomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(sourceBiome != null ? sourceBiome : biome);
        		replaceToBiome = OTG.getBiomeAllWorlds(replaceToBiomeId);
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
			OTG.log(LogMarker.WARN, "Biome " + biome.biomeName + " could not be found in the registry. This could be because it is a virtual biome (id > 255) but does not have a ReplaceToBiomeName configured.");
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
		        	OTG.log(LogMarker.WARN, "Can't find BiomeDictId: \"" + typeString + "\".");
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

    	replaceBlocks(this.getChunk(chunkCoord.getBlockX() + 16, chunkCoord.getBlockZ() + 16, false), 0, 0, 16);
    	replaceBlocks(this.getChunk(chunkCoord.getBlockX(), chunkCoord.getBlockZ() + 16, false), 0, 0, 16);
    	replaceBlocks(this.getChunk(chunkCoord.getBlockX() + 16, chunkCoord.getBlockZ(), false), 0, 0, 16);
    	replaceBlocks(this.getChunk(chunkCoord.getBlockX(), chunkCoord.getBlockZ(), false), 0, 0, 16);
    }

    private void replaceBlocks(Chunk rawChunk, int startXInChunk, int startZInChunk, int size)
    {
        int endXInChunk = startXInChunk + size;
        int endZInChunk = startZInChunk + size;
        int worldStartX = rawChunk.x * 16;
        int worldStartZ = rawChunk.z * 16;

        ExtendedBlockStorage[] sectionsArray = rawChunk.getBlockStorageArray();

        for (ExtendedBlockStorage section : sectionsArray)
        {
            if (section == null)
            {
                continue;
            }

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
                            {
                                continue;
                            }

                            int y = section.getYLocation() + sectionY;
                            if (y >= replaceArray[blockId].length)
                            {
                                break;
                            }

                            ForgeMaterialData replaceTo = (ForgeMaterialData) replaceArray[blockId][y];
                            if (replaceTo == null || replaceTo.getBlockId() == blockId)
                            {
                                continue;
                            }

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
        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this.getChunkGenerator(), this.world, random, chunkCoord.getChunkX(), chunkCoord.getChunkZ(), false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS))
        {
	        //WorldEntitySpawner.performWorldGenSpawning(this.world, biome, i + 8, j + 8, 16, 16, this.rand);
	        WorldEntitySpawner.performWorldGenSpawning(this.getWorld(), ((ForgeBiome) biome).getHandle(), chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter(), ChunkCoordinate.CHUNK_X_SIZE, ChunkCoordinate.CHUNK_Z_SIZE, random);
        }
    }

    boolean allowSpawningOutsideBounds = false;
    @Override
	public void setAllowSpawningOutsideBounds(boolean allowSpawningOutsideBounds)
	{
		this.allowSpawningOutsideBounds = allowSpawningOutsideBounds;
	}

    public boolean getAllowSpawningOutsideBounds()
	{
		return this.allowSpawningOutsideBounds;
	}

    // If allowOutsidePopulatinArea then normal OTG rules are used:
    // returns any chunk that is inside the area being populated.
    // returns null for chunks outside the populated area if populationBoundsCheck=true
    // returns any loaded chunk or null if populationBoundsCheck=false and chunk is outside the populated area

    // If !allowOutsidePopulatinArea then OTG+ rules are used:
    // returns any chunk that is inside the area being populated. TODO: Or any chunk that is cached, which technically should only be chunks that are in the populated area. Cached chunks could also be from the previously populated area, fix that?
    // returns any loaded chunk outside the populated area
    // throws an exception if any unloaded chunk outside the populated area is requested or if a loaded chunk could not be queried.

    public Map<ChunkCoordinate,Chunk> chunkCacheOTGPlus = new HashMap<ChunkCoordinate, Chunk>();
    public Chunk lastUsedChunk;
    public int lastUsedChunkX;
    public int lastUsedChunkZ;
    public Chunk getChunk(int x, int z, boolean isOTGPlus)
    {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if(lastUsedChunk != null && lastUsedChunkX == chunkX && lastUsedChunkZ == chunkZ)
        {
        	return lastUsedChunk;
        }

        Chunk chunk = chunkCacheOTGPlus.get(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));
        if(chunk != null)
        {
        	lastUsedChunk = chunk;
        	lastUsedChunkX = chunkX;
        	lastUsedChunkZ = chunkZ;
        	return chunk;
        }

        boolean outsidePopulatingArea =
			(
				chunkX != getObjectSpawner().populatingX &&
				chunkX != getObjectSpawner().populatingX + 1
			)
			||
			(
				chunkZ != getObjectSpawner().populatingZ &&
				chunkZ != getObjectSpawner().populatingZ + 1
			)
		;

		if(
			(
				outsidePopulatingArea &&
				!isOTGPlus
			) ||
			allowSpawningOutsideBounds
		)
		{
			if(!isOTGPlus)
			{
				if(this.getConfigs().getWorldConfig().populationBoundsCheck)
				{
					return null;
				}

				// TODO: Does this return only loaded chunks outside the area being populated, or also unloaded ones?
				Chunk loadedChunk = this.getLoadedChunkWithoutMarkingActive(chunkX, chunkZ);
				if(loadedChunk != null)
				{
					lastUsedChunk = loadedChunk;
		        	lastUsedChunkX = chunkX;
		        	lastUsedChunkZ = chunkZ;
					chunkCacheOTGPlus.put(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), loadedChunk);
				}

				if(!allowSpawningOutsideBounds || loadedChunk != null)
				{
					return loadedChunk;
				}
			}

			// For BO3AtSpawn we may be forced to populate a chunk outside of the chunks being populated.
			if(allowSpawningOutsideBounds)
			{
		        Chunk spawnedChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		        if(spawnedChunk == null)
		        {
		        	OTG.log(LogMarker.INFO, "Chunk request failed X" + chunkX + " Z" + chunkZ);
		        	throw new RuntimeException();
		        }

		        chunkCacheOTGPlus.put(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), spawnedChunk);
				lastUsedChunk = spawnedChunk;
		    	lastUsedChunkX = chunkX;
		    	lastUsedChunkZ = chunkZ;

				return spawnedChunk;
			}
		}

        boolean outsideBorder = false;
    	if(!IsInsideWorldBorder(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), true))
    	{
    		// This can happen when net.minecraft.server.MinecraftServer.updateTimeLightAndEntities() is called
    		//OTG.log(LogMarker.INFO, "Requested chunk outside world border X" + chunkX + " Z" + chunkZ);
    		outsideBorder = true;
    	}

    	// This never happens when we're spawning stuff on neighbouring BO3's inside the 2x2 population area
    	if(
			!outsideBorder && outsidePopulatingArea
		)
    	{
    		if(!((WorldServer)this.getWorld()).isBlockLoaded(new BlockPos(chunkX * 16, 1, chunkZ * 16)))
    		//if(!((WorldServer)this.getWorld()).isChunkGeneratedAt(chunkX, chunkZ))
    		{
    			// Happens when part of a BO3 or smoothing area is spawned and triggers height/material checks in unpopulated chunks.
    			// Also happens when /otg tp requests a block in an unpopulated chunk.
    			return null;
    		} else {
    			// Chunk was provided by chunkprovider
    		}
    	}

        Chunk spawnedChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        if(spawnedChunk == null)
        {
        	OTG.log(LogMarker.INFO, "Chunk request failed X" + chunkX + " Z" + chunkZ);
        	throw new RuntimeException();
        }

        chunkCacheOTGPlus.put(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), spawnedChunk);
		lastUsedChunk = spawnedChunk;
    	lastUsedChunkX = chunkX;
    	lastUsedChunkZ = chunkZ;

		return spawnedChunk;
    }

    public void ClearChunkCache()
    {
    	chunkCacheOTGPlus.clear();
    	lastUsedChunk = null;

    	/*
        // Cache only chunks in the 2x2 chunk area being populated
        Map<ChunkCoordinate,Chunk> chunkCache2 = new HashMap<ChunkCoordinate, Chunk>();
        chunkCache2.putAll(chunkCacheOTGPlus);
        for(Entry<ChunkCoordinate, Chunk> a : chunkCache2.entrySet())
        {
        	if(
    			!(
					a.getKey().getChunkX() == getObjectSpawner().populatingX ||
					a.getKey().getChunkX() == getObjectSpawner().populatingX + 1
				)
				||
				!(
					a.getKey().getChunkZ() == getObjectSpawner().populatingZ ||
					a.getKey().getChunkZ() == getObjectSpawner().populatingZ + 1
				)
			)
        	{
        		chunkCacheOTGPlus.remove(a.getKey());
        	}
        }
        */
    }

    // TODO: This is interesting, could use it more?
    public Chunk getLoadedChunkWithoutMarkingActive(int chunkX, int chunkZ)
    {
        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) this.world.getChunkProvider();
        long i = ChunkPos.asLong(chunkX, chunkZ);
        return (Chunk) chunkProviderServer.id2ChunkMap.get(i);
    }

    @Override
    public int getLiquidHeight(int x, int z)
    {
        int highestY = getHighestBlockYAt(x, z, false, true, false, false);
        if(highestY > 0)
        {
        	highestY += 1;
        } else {
        	highestY = -1;
        }
		return highestY;
    }

    @Override
    public int getSolidHeight(int x, int z)
    {
        int highestY = getHighestBlockYAt(x, z, true, false, true, true);
        if(highestY > 0)
        {
        	highestY += 1;
        } else {
        	highestY = -1;
        }
		return highestY;
    }

    @Override
    public boolean isNullOrAir(int x, int y, int z, boolean isOTGPlus)
    {
    	if (y >= OTG.WORLD_HEIGHT || y < OTG.WORLD_DEPTH)
    	{
    		return true;
    	}

        Chunk chunk = this.getChunk(x, z, isOTGPlus);
        if (chunk == null)
        {
        	return true;
        }

        return chunk.getBlockState(x & 0xF, y, z & 0xF).getMaterial().equals(Material.AIR);
    }

    @Override
    public BlockFunction[] getBlockColumn(int x, int z)
    {
    	//OTG.log(LogMarker.INFO, "getBlockColumn at X" + x + " Z" + z);
    	return generator.getBlockColumnInUnloadedChunk(x,z);
    }

    // TODO: This returns AIR for nothing and AIR, refactor?
    @Override
    public LocalMaterialData getMaterial(int x, int y, int z, boolean IsOTGPlus)
    {
        if (y >= OTG.WORLD_HEIGHT || y < OTG.WORLD_DEPTH)
        {
        	return ForgeMaterialData.ofMinecraftBlock(Blocks.AIR);
        	//throw new RuntimeException();
        }

        Chunk chunk = this.getChunk(x, z, IsOTGPlus);

        if(chunk == null && !IsOTGPlus)
        {
        	return ForgeMaterialData.ofMinecraftBlock(Blocks.AIR);
        }

        // Can happen when requesting a chunk outside the world border
        // or a chunk that has not yet been populated
        if (chunk == null)
		{
        	return generator.getMaterialInUnloadedChunk(x,y,z);
        	//throw new RuntimeException();
		}

		// Get internal coordinates for block in chunk
        z &= 0xF;
        x &= 0xF;

        ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(chunk.getBlockState(x, y, z));

        return material;
    }

    @Override
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean isOTGPlus)
    {
	    /*
	     * This method usually breaks on every Minecraft update. Always check
	     * whether the names are still correct. Often, you'll also need to
	     * rewrite parts of this method for newer block place logic.
	     */

        if (y < OTG.WORLD_DEPTH || y >= OTG.WORLD_HEIGHT)
        {
            return;
        }

        //DefaultMaterial defaultMaterial = material.toDefaultMaterial();

        // TODO: Fix this
        //if(defaultMaterial.equals(DefaultMaterial.DIODE_BLOCK_ON))
        {
        	//material = ForgeMaterialData.ofDefaultMaterial(DefaultMaterial.DIODE_BLOCK_OFF, material.getBlockData());
        }
        //else if(defaultMaterial.equals(DefaultMaterial.REDSTONE_COMPARATOR_ON))
        {
        	//material = ForgeMaterialData.ofDefaultMaterial(DefaultMaterial.REDSTONE_COMPARATOR_OFF, material.getBlockData());
        }

        IBlockState newState = ((ForgeMaterialData) material).internalBlock();

        BlockPos pos = new BlockPos(x, y, z);

        // Get chunk from (faster) custom cache
        Chunk chunk = getChunk(x, z, isOTGPlus);
        if (chunk == null)
        {
            // Chunk is unloaded
        	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
        }

        IBlockState iblockstate = setBlockState(chunk, pos, newState);

        if (iblockstate == null)
        {
        	return; // Happens when block to place is the same as block being placed? TODO: Is that the only time this happens?
        }

	    if (metaDataTag != null)
	    {
	    	attachMetadata(x, y, z, metaDataTag, isOTGPlus);
	    }

    	this.world.markAndNotifyBlock(pos, chunk, iblockstate, newState, 2 | 16);
    }

    public IBlockState setBlockState(Chunk _this, BlockPos pos, IBlockState state)
    {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int l = k << 4 | i;

        if (j >= _this.precipitationHeightMap[l] - 1)
        {
        	_this.precipitationHeightMap[l] = -999;
        }

        int i1 = _this.getHeightMap()[l];
        IBlockState iblockstate = _this.getBlockState(pos);

        if (iblockstate == state)
        {
            return null;
        } else {
            Block block = state.getBlock();
            Block block1 = iblockstate.getBlock();
            int k1 = iblockstate.getLightOpacity(_this.getWorld(), pos); // Relocate old light value lookup here, so that it is called before TE is removed.
            ExtendedBlockStorage extendedblockstorage = _this.getBlockStorageArray()[j >> 4];
            boolean flag = false;

            if (extendedblockstorage == Chunk.NULL_BLOCK_STORAGE)
            {
                if (block == Blocks.AIR)
                {
                    return null;
                }

                extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, _this.getWorld().provider.hasSkyLight());
                _this.getBlockStorageArray()[j >> 4] = extendedblockstorage;
                flag = j >= i1;
            }

            extendedblockstorage.set(i, j & 15, k, state);

            //if (block1 != block)
            {
                if (!_this.getWorld().isRemote)
                {
                    if (block1 != block) //Only fire block breaks when the block changes.
                    block1.breakBlock(_this.getWorld(), pos, iblockstate);
                    TileEntity te = _this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                    if (te != null && te.shouldRefresh(_this.getWorld(), pos, iblockstate, state)) _this.getWorld().removeTileEntity(pos);
                }
                else if (block1.hasTileEntity(iblockstate))
                {
                    TileEntity te = _this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                    if (te != null && te.shouldRefresh(_this.getWorld(), pos, iblockstate, state))
                    _this.getWorld().removeTileEntity(pos);
                }
            }

            if (extendedblockstorage.get(i, j & 15, k).getBlock() != block)
            {
                return null;
            } else {
                if (flag)
                {
                    _this.generateSkylightMap();
                }
                else
                {
                    int j1 = state.getLightOpacity(_this.getWorld(), pos);

                    if (j1 > 0)
                    {
                        if (j >= i1)
                        {
                            _this.relightBlock(i, j + 1, k);
                        }
                    }
                    else if (j == i1 - 1)
                    {
                    	_this.relightBlock(i, j, k);
                    }

                    if (j1 != k1 && (j1 < k1 || _this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || _this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0))
                    {
                        _this.propagateSkylightOcclusion(i, k);
                    }
                }

                // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
                //if (!_this.getWorld().isRemote && block1 != block && (!_this.getWorld().captureBlockSnapshots || block.hasTileEntity(state)))
                {
                	// Don't do this when spawning resources and BO2's/BO3's, they are considered to be in their intended updated state when spawned
               		//block.onBlockAdded(_this.getWorld(), pos, state);
                }

                if (block.hasTileEntity(state))
                {
                    TileEntity tileentity1 = _this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                    if (tileentity1 == null)
                    {
                        tileentity1 = block.createTileEntity(_this.getWorld(), state);
                        _this.getWorld().setTileEntity(pos, tileentity1);
                    }

                    if (tileentity1 != null)
                    {
                        tileentity1.updateContainingBlockInfo();
                    }
                }

                _this.markDirty();
                return iblockstate;
            }
        }
    }

    // Not used by OTG+
    @Override
    public int getHighestBlockYAt(int x, int z)
    {
        Chunk chunk = this.getChunk(x, z, false);
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

    // OTG+
    // Only used by OTG+
    @Override
    public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
    {
        Chunk chunk = this.getChunk(x, z, true);
        if (chunk == null)
        {
        	int y = generator.getHighestBlockYInUnloadedChunk(x,z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
        	return y;
        }

		// Get internal coordinates for block in chunk
        z &= 0xF;
        x &= 0xF;

        // TODO: Get highest block from heightmap?
    	for(int i = 255; i > -1; i--)
        {
    		ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(chunk.getBlockState(x, i, z));
        	if(material == null) throw new RuntimeException();
        	boolean isLiquid = material.isLiquid();
        	boolean isSolid = (material.isSolid() && !material.equals(Material.LEAVES)) || (!ignoreSnow && material.equals(Material.SNOW));
        	if(!(isLiquid && ignoreLiquid))
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
    public void startPopulation(ChunkCoordinate chunkCoord)
    {

    }

    @Override
    public void endPopulation()
    {

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
        return getChunk(x, z, false) != null;
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
    public String getWorldSettingsName()
    {
        return this.getWorld().getWorldInfo().getWorldName();
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

    public OTGChunkGenerator getChunkGenerator()
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
        this.settings = configs;
    }

    /**
     * Call this method when the Minecraft world is loaded. Call this method
     * after {@link #provideConfigs(ServerConfigProvider)} has been called.
     * @param world The Minecraft world.
     */
    public void provideWorldInstance(WorldServer world)
    {
        ServerConfigProvider configs = (ServerConfigProvider) this.settings;

        // Custom dimension settings
        // world is unique for this dimension however its worldinfo
        // was derived from the main world and has the same seed
        // configure the correct seed for this dimension.
		// If the world is a TCWorldServerMulti then it was created
		// by a console command and has already had its seed set
        if(!isMainWorld && !(world instanceof OTGWorldServerMulti))
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
            		OTG.log(LogMarker.ERROR, "WorldConfig for world \"" + world.getWorldInfo().getWorldName() + "\" has value \"" + configs.getWorldConfig().worldSeed + "\" for worldSeed which cannot be parsed as a number. Using a random seed instead.");
                }
            }

    		GameType gameType = world.getWorldInfo().getGameType();
    		boolean enableMapFeatures = world.getWorldInfo().isMapFeaturesEnabled(); // Whether the map features (e.g. strongholds) generation is enabled or disabled.
    		boolean hardcoreMode = world.getWorldInfo().isHardcoreModeEnabled();
    		WorldType worldTypeIn = world.getWorldType();

    		String generatorOptions = world.getWorldInfo() instanceof DerivedWorldInfo ? ((DerivedWorldInfo)world.getWorldInfo()).delegate.getGeneratorOptions() : world.getWorldInfo().getGeneratorOptions();
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

        this.dataFixer = DataFixesManager.createFixer();

        this.dungeonGen = new WorldGenDungeons();
        this.fossilGen = new WorldGenFossils();
        this.strongholdGen = new OTGStrongholdGen(configs, world);

        this.villageGen = new OTGVillageGen(configs);
        this.mineshaftGen = new OTGMineshaftGen();
        this.rareBuildingGen = new OTGRareBuildingGen(configs);
        this.netherFortressGen = new OTGNetherFortressGen();
        this.oceanMonumentGen = new OTGOceanMonumentGen(configs);
        this.woodLandMansionGen = new OTGWoodLandMansionGen(configs);

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

        this.WorldSession = new ForgeWorldSession(this);
        this.generator = new OTGChunkGenerator(this);

        this.structureCache = new CustomObjectStructureCache(this);
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
    	return OTG.isForge ? OTG.getBiomeAllWorlds(this.biomeGenerator.getBiome(x, z)) : getBiomeById(this.biomeGenerator.getBiome(x, z));
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
    	if(biome instanceof OTGBiome)
    	{
    		biomeId = ((OTGBiome)biome).generationId;
    	} else {
    		biomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(biome); // Non-TC biomes don't have a generationId, only a saved id
    	}
    	//ForgeBiome forgeBiome = getBiomeById(biomeId);
    	ForgeBiome forgeBiome = (ForgeBiome) OTG.getBiomeAllWorlds(biomeId);

        return forgeBiome;
    }

    void attachMetadata(int x, int y, int z, NamedBinaryTag tag, boolean allowOutsidePopulatingArea)
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
            OTG.log(LogMarker.DEBUG, "Skipping tile entity with id {}, cannot be placed at {},{},{} on id {}", nmsTag.getString("id"), x, y, z, getMaterial(x, y, z, allowOutsidePopulatingArea));
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

    // OTG+
    @Override
    public ObjectSpawner getObjectSpawner()
    {
        return this.generator.spawner;
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

	            for(ResourceLocation ob : ForgeRegistries.BIOMES.getKeys())
	    		{
	            	int vanillaBiomeId = ((ForgeEngine)OTG.getEngine()).getBiomeRegistryId(ob);
	            	if(vanillaBiomeId == -1)
	            	{
	            		throw new RuntimeException();
	            	}
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
        	if (vanillaBiome != null && vanillaBiome.biomeName.equals(biomeName) && !(vanillaBiome instanceof OTGBiome))
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
			// TODO: Only "Hell" ever seems to occur? Find out what's going on with the other biomes?
			if(localBiome.getName().equals("The Void") || localBiome.getName().equals("The End") || localBiome.getName().equals("Hell") || localBiome.getName().equals("Sky"))
			{
				continue;
			}

			if(((ForgeEngine)OTG.getEngine()).worldLoader.isConfigUnique(localBiome.getBiomeConfig().getName()))
			{
		        String biomeNameForRegistry = StringHelper.toComputerFriendlyName(localBiome.getName());
		        String resourceDomain = PluginStandardValues.PLUGIN_NAME.toLowerCase();
		        ResourceLocation registryKey = new ResourceLocation(resourceDomain, biomeNameForRegistry);

		        ((ForgeEngine)OTG.getEngine()).unRegisterForgeBiome(registryKey);
			}
		}

		((ForgeEngine)OTG.getEngine()).worldLoader.clearBiomeDictionary(this);
	}

    @Override
    public void SpawnEntity(EntityFunction entityData)
    {
    	if(OTG.getPluginConfig().SpawnLog)
    	{
    		OTG.log(LogMarker.DEBUG, "Attempting to spawn BO3 Entity() " + entityData.groupSize + " x " + entityData.mobName + " at " + entityData.x + " " + entityData.y + " " + entityData.z);
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
        	OTG.log(LogMarker.WARN, "Could not find entity: " + mobTypeName);
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
		        	OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
		        	return;
	            }

	            nbttagcompound = (NBTTagCompound)nbtbase;
	        }
	        catch (NBTException nbtexception)
	        {
	        	OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
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
		            		        	OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
		            		        	return;
	            		            }

	            		            nbttagcompound = (NBTTagCompound)nbtbase;
	            		        }
	            		        catch (NBTException nbtexception)
	            		        {
	            		        	OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
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

    			    	if(OTG.getPluginConfig().SpawnLog)
    			    	{
    			    		OTG.log(LogMarker.DEBUG, "Spawned OK");
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

    			    	if(OTG.getPluginConfig().SpawnLog)
    			    	{
    			    		OTG.log(LogMarker.DEBUG, "Spawned OK");
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

    @Override
	public boolean IsInsidePregeneratedRegion(ChunkCoordinate chunk, boolean includeBorder)
	{
		return
			!(
				// TODO: Make this prettier.
				// Cycle 0 for the pre-generator can mean 2 things:
				// 1. Nothing has been pre-generated.
				// 2. Oly the spawn chunk has been generated.
				// The pre-generator actually skips spawning the center chunk at cycle 0 (is done automatically by MC anyway).
				GetWorldSession().getPregeneratedBorderLeft() == 0 &&
				GetWorldSession().getPregeneratedBorderRight() == 0 &&
				GetWorldSession().getPregeneratedBorderTop() == 0 &&
				GetWorldSession().getPregeneratedBorderBottom() == 0
			) &&
			(
				GetWorldSession().getPregenerationRadius() > 0 &&
				chunk.getChunkX() >= GetWorldSession().getPreGeneratorCenterPoint().getChunkX() - GetWorldSession().getPregeneratedBorderLeft()
				&&
				chunk.getChunkX() <= GetWorldSession().getPreGeneratorCenterPoint().getChunkX() + GetWorldSession().getPregeneratedBorderRight() - (!includeBorder ? 1 : 0)
				&&
				chunk.getChunkZ() >= GetWorldSession().getPreGeneratorCenterPoint().getChunkZ() - GetWorldSession().getPregeneratedBorderTop()
				&&
				chunk.getChunkZ() <= GetWorldSession().getPreGeneratorCenterPoint().getChunkZ() + GetWorldSession().getPregeneratedBorderBottom() - (!includeBorder ? 1 : 0)
			)
		;
	}

    @Override
	public boolean IsInsideWorldBorder(ChunkCoordinate chunk, boolean spawningResources)
	{
		return
			GetWorldSession().getWorldBorderRadius() == 0 ||
			(
				chunk.getChunkX() >= GetWorldSession().getWorldBorderCenterPoint().getChunkX() - (GetWorldSession().getWorldBorderRadius() - 1)
				&&
				chunk.getChunkX() <= GetWorldSession().getWorldBorderCenterPoint().getChunkX() + (GetWorldSession().getWorldBorderRadius() - 1) - (spawningResources ? 1 : 0) // Resources are spawned at an offset of + half a chunk so stop 1 chunk short of the border
				&&
				chunk.getChunkZ() >= GetWorldSession().getWorldBorderCenterPoint().getChunkZ() - (GetWorldSession().getWorldBorderRadius() - 1)
				&&
				chunk.getChunkZ() <= GetWorldSession().getWorldBorderCenterPoint().getChunkZ() + (GetWorldSession().getWorldBorderRadius() - 1) - (spawningResources ? 1 : 0) // Resources are spawned at an offset of + half a chunk so stop 1 chunk short of the border
			);
	}

	ForgeWorldSession WorldSession;
    @Override
	public WorldSession GetWorldSession()
	{
		return WorldSession;
	}

	@Override
	public File getWorldSaveDir()
	{
		return this.getWorld().getSaveHandler().getWorldDirectory();
	}

	@Override
	public int getDimensionId()
	{
		return getWorld().provider.getDimension();
	}

	@Override
	public void DeleteWorldSessionData()
	{
		int dimensionId = getWorld().provider.getDimension();
		File worldDataDir = new File(getWorld().getSaveHandler().getWorldDirectory() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : ""));
		if(worldDataDir.exists())
		{
			IOHelper.deleteRecursive(worldDataDir);
		}
	}
}
