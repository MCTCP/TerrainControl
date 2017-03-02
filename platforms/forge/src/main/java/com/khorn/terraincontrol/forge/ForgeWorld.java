package com.khorn.terraincontrol.forge;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.configuration.*;
import com.khorn.terraincontrol.configuration.BiomeConfigFinder.BiomeConfigStub;
import com.khorn.terraincontrol.configuration.standard.MojangSettings.EntityCategory;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.customobjects.bo3.EntityFunction;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import com.khorn.terraincontrol.forge.generator.ChunkProvider;
import com.khorn.terraincontrol.forge.generator.structure.*;
import com.khorn.terraincontrol.forge.util.MobSpawnGroupHelper;
import com.khorn.terraincontrol.forge.util.NBTHelper;
import com.khorn.terraincontrol.generator.SpawnableObject;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ForgeWorld implements LocalWorld
{
    private ChunkProvider generator;
    private World world;
    private ConfigProvider settings;
    private CustomObjectStructureCache structureCache;
    private String name;
    private long seed;
    private BiomeGenerator biomeGenerator;
    private DataFixer dataFixer;

    private static final int MAX_BIOMES_COUNT = 1024;
    private static final int MAX_SAVED_BIOMES_COUNT = 255;
    private static final int STANDARD_WORLD_HEIGHT = 128;

    private HashMap<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();

    public StrongholdGen strongholdGen;
    public VillageGen villageGen;
    public MineshaftGen mineshaftGen;
    public RareBuildingGen rareBuildingGen;
    public NetherFortressGen netherFortressGen;
    public OceanMonumentGen oceanMonumentGen;

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

    private static Biome[] vanillaBiomes = new Biome[MAX_BIOMES_COUNT];    
    private static boolean vanillaBiomesCached = false;
    
    public ForgeWorld(String _name)
    {
        this.name = _name;

        cacheVanillaBiomes();
    	WorldLoader.unRegisterDefaultBiomes(); 
        
        // Clear the BiomeDictionary (it will be refilled when biomes are loaded in createBiomeFor)
    	WorldLoader.clearBiomeDictionary();
    }
          
    @Override
    public LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds, ConfigProvider configProvider)
    {      	
		// Make an exception for the hell and sky biomes. 
		// The hell and end chunk providers refer specifically to 
		// Biomes.HELL and Biomes.SKY and query the biome registry
		// for them. Other biomes are not referred to in this way.
    	if(biomeConfig.getName().equals("Hell")) { return new ForgeBiome(Biomes.HELL, biomeConfig, new BiomeIds(8,8)); }
    	if(biomeConfig.getName().equals("Sky")) { return new ForgeBiome(Biomes.SKY, biomeConfig, new BiomeIds(9,9)); }
    	
    	// Always try to register biomes and create Biome Configs. Biomes with id's > 255 are registered
    	// only for biome -> id queries, any (saved)id -> biome query will return the ReplaceToBiomeName biome.
        
        Biome existingBiome = Biome.getBiome(biomeIds.getSavedId());
    	Biome biome = BiomeGenCustom.getOrCreateBiome(biomeConfig, biomeIds);
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
            TerrainControl.log(LogMarker.DEBUG, "Asked to register {} with id {}, but succeeded with id {}",
                    biomeConfig.getName(), requestedGenerationId, allocatedGenerationId);
        } else {
            TerrainControl.log(LogMarker.DEBUG, "Registered {} with id {}",
                    biomeConfig.getName(), allocatedGenerationId);
        }

        ForgeBiome forgeBiome = new ForgeBiome(biome, biomeConfig, biomeIds);
        
        registerBiomeInBiomeDictionary(biome, existingBiome, biomeConfig, configProvider);
        
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
    	BiomeDictionary.registerBiomeType(biome, typeArr);
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
		        	TerrainControl.log(LogMarker.INFO, "Error: Can't find BiomeDictId: \"" + typeString + "\".");
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
    	throw new NotImplementedException();
        //return nextBiomeId++;
    }

    @Override
    public ForgeBiome getBiomeById(int id) throws BiomeNotFoundException
    {
        LocalBiome biome = this.settings.getBiomeByIdOrNull(id);
        if (biome == null)
        {
            throw new BiomeNotFoundException(id, Arrays.asList(this.settings.getBiomeArray()));
        }
        return (ForgeBiome) biome;
    }

    @Override
    public ForgeBiome getBiomeByIdOrNull(int id)
    {
        return (ForgeBiome) this.settings.getBiomeByIdOrNull(id);
    }

    @Override
    public LocalBiome getBiomeByName(String name) throws BiomeNotFoundException
    {
        LocalBiome biome = this.biomeNames.get(name);
        if (biome == null)
        {
            throw new BiomeNotFoundException(name, this.biomeNames.keySet());
        }
        return biome;
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
            // (Tree growing, /tc spawn, etc.)
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

        IBlockState newState = ((ForgeMaterialData) material).internalBlock();

        // Get chunk from (faster) custom cache
        Chunk chunk = this.getChunk(x, y, z);

        if (chunk == null)
        {
            // Chunk is unloaded
            return;
        }

        BlockPos pos = new BlockPos(x, y, z);

        IBlockState oldState = this.world.getBlockState(pos);
        int oldLight = oldState.getLightValue(this.world, pos);
        int oldOpacity = oldState.getLightOpacity(this.world, pos);

        IBlockState iblockstate = chunk.setBlockState(pos, newState);

        if (iblockstate == null)
        {
            return;
        }

        // Relight and update players
        if (newState.getLightOpacity(this.world, pos) != oldOpacity || newState.getLightValue(this.world, pos) != oldLight)
        {
            this.world.theProfiler.startSection("checkLight");
            this.world.checkLight(pos);
            this.world.theProfiler.endSection();
        }

        this.world.markAndNotifyBlock(pos, chunk, iblockstate, newState, 2);
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
            throw new IllegalStateException("Chunk is already being populated." + " This may be a bug in Terrain Control, but it may also be" + " another mod that is poking in unloaded chunks. Set" + " PopulationBoundsCheck to false in the WorldConfig to" + " disable this error.");
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
                    + " This may be a bug in Terrain Control, but it may also be"
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

    public ChunkProvider getChunkGenerator()
    {
        return this.generator;
    }

    @SideOnly(Side.CLIENT)
    public void provideClientConfigs(WorldClient world, ClientConfigProvider config)
    {
        this.settings = config;
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

        this.world = world;
        this.seed = world.getSeed();
        world.setSeaLevel(configs.getWorldConfig().waterLevelMax);

        this.structureCache = new CustomObjectStructureCache(this);
        this.dataFixer = DataFixesManager.createFixer();

        this.dungeonGen = new WorldGenDungeons();
        this.fossilGen = new WorldGenFossils();
        this.strongholdGen = new StrongholdGen(configs);

        this.villageGen = new VillageGen(configs);
        this.mineshaftGen = new MineshaftGen();
        this.rareBuildingGen = new RareBuildingGen(configs);
        this.netherFortressGen = new NetherFortressGen();
        this.oceanMonumentGen = new OceanMonumentGen(configs);

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

        this.generator = new ChunkProvider(this);
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
        return getBiomeById(this.biomeGenerator.getBiome(x, z));
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
    	if(biome instanceof BiomeGenCustom)
    	{
    		biomeId = ((BiomeGenCustom)biome).generationId;
    	} else {
    		biomeId = Biome.getIdForBiome(biome); // Non-TC biomes don't have a generationId, only a saved id
    	}
    	ForgeBiome forgeBiome = getBiomeById(biomeId);
    	
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
        nmsTag = this.dataFixer.process(FixTypes.BLOCK_ENTITY, nmsTag, -1);
        // Add that data to the current tile entity in the world
        TileEntity tileEntity = this.world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity != null)
        {
            tileEntity.readFromNBT(nmsTag);
        } else
        {
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
        	if (vanillaBiome != null && vanillaBiome.getBiomeName().equals(biomeName) && !(vanillaBiome instanceof BiomeGenCustom))
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
		BitSet biomeRegistryAvailabiltyMap = WorldLoader.getBiomeRegistryAvailabiltyMap();
	    // Unregister only the biomes registered by this world
		for(LocalBiome localBiome : this.biomeNames.values())
		{	
			// Make an exception for the hell and sky biomes. 
			// The hell and end chunk providers refer specifically to 
			// Biomes.HELL and Biomes.SKY and query the biome registry
			// for them. Other biomes are not referred to in this way.
			if(localBiome.getName().equals("Hell") || localBiome.getName().equals("Sky")) { continue; }
			biomeRegistryAvailabiltyMap.set(localBiome.getIds().getSavedId(), false); // This should be enough to make Forge re-use the biome id
		}
		
		WorldLoader.clearBiomeDictionary();
	}
	
    @Override
    public void SpawnEntity(EntityFunction entityData)
    {
    	Random rand = new Random();
    	
		String mobTypeName = entityData.mobName;
		int groupSize = entityData.groupSize;
		String nameTag = entityData.nameTagOrNBTFileName;

        Class<?> entityClass = null;
        
        for(String entry : EntityList.NAME_TO_CLASS.keySet())
        {
        	if(entry.toLowerCase().replace("entity", "").equals(mobTypeName.toLowerCase().replace("entity", "")))
        	{
            	entityClass = EntityList.NAME_TO_CLASS.get(entry);
        		break;
        	}
        }
        	                                
        if(entityClass == null)
        {
        	TerrainControl.log(LogMarker.INFO, "Could not find entity: " + mobTypeName);
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
	            	throw new NotImplementedException(); // Not a valid tag
	            }
	
	            nbttagcompound = (NBTTagCompound)nbtbase;
	        }
	        catch (NBTException nbtexception)
	        {
	        	TerrainControl.log(LogMarker.INFO, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	        	return;
	        }		                                            
	        
	        nbttagcompound.setString("id", entityData.mobName);
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
            
            Material material = world.getBlockState(new BlockPos(j1, k1, l1)).getMaterial();
            if (!world.isBlockNormalCube(new BlockPos(j1, k1, l1), false) && (creatureType == EnumCreatureType.WATER_CREATURE && material == Material.WATER || material == Material.AIR ))
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
	            		            	throw new NotImplementedException(); // Not a valid tag
	            		            }
	            		
	            		            nbttagcompound = (NBTTagCompound)nbtbase;
	            		        }
	            		        catch (NBTException nbtexception)
	            		        {
	            		        	TerrainControl.log(LogMarker.INFO, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	            		        	return;
	            		        }		                                            
	            		        
	            		        nbttagcompound.setString("id", entityData.mobName);
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
	            		
	        			((EntityLiving) entityliving).setCustomNameTag(mobTypeName.replace("entity", "").substring(0, 1).toUpperCase() + mobTypeName.toLowerCase().replace("entity", "").substring(1));
	                    
	            		if(entityData.nameTagOrNBTFileName != null && !entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") && !entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt"))
	            		{
	        				if(nameTag != null && nameTag.length() > 0)
	        				{
	        					((EntityLiving) entityliving).setCustomNameTag(nameTag);
	        				}
	            		}
	    				
						((EntityLiving) entityliving).enablePersistence(); // <- makes sure mobs don't de-spawn
	            		
	            		world.spawnEntityInWorld(entityliving);
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
	            		
	            		world.spawnEntityInWorld(entityliving);
	            	}						                                                	
	            }
            }
		}
    }
    
    public BlockPos getSpawnPoint()
    {    	
    	return new BlockPos(world.provider.getSpawnPoint().getX(), world.provider.getSpawnPoint().getY(), world.provider.getSpawnPoint().getZ());
    }
    
	public boolean IsInsideWorldBorder(ChunkCoordinate chunk)
	{
		BlockPos spawnPoint = getSpawnPoint();
    	ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());		
		return
			((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius == 0 ||
			(
				chunk.getChunkX() >= spawnChunk.getChunkX() - (((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius - 1)
				&& chunk.getChunkX() <= spawnChunk.getChunkX() + (((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius - 1)
				&& chunk.getChunkZ() >= spawnChunk.getChunkZ() - (((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius - 1)
				&& chunk.getChunkZ() <= spawnChunk.getChunkZ() + (((ForgeEngine)TerrainControl.getEngine()).WorldBorderRadius - 1)
			);
	}
}
