package com.pg85.otg.forge.gen;

import java.nio.file.Paths;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.dimensions.DimensionsConfig;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.gen.OTGChunkPopulator;
import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.NoiseSettings;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraft.world.storage.IServerWorldInfo;

public final class OTGNoiseChunkGenerator extends ChunkGenerator
{
	// Create a codec to serialise/deserialise OTGNoiseChunkGenerator
	public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
		(p_236091_0_) ->
		{
			return p_236091_0_
				.group(
					Codec.STRING.fieldOf("otg_dimension_config").forGetter(
						(p_236090_0_) -> {
							return p_236090_0_.dimensionConfig.toYamlString();
						}
					),						
					BiomeProvider.field_235202_a_.fieldOf("biome_source").forGetter(
						(p_236096_0_) -> {
							return p_236096_0_.biomeProvider;
						}
					), 
					Codec.LONG.fieldOf("seed").stable().forGetter(
						(p_236093_0_) -> {
							return p_236093_0_.worldSeed;
						}
					),
					DimensionSettings.field_236098_b_.fieldOf("settings").forGetter(
						(p_236090_0_) -> {
							return p_236090_0_.dimensionSettingsSupplier;
						}
					)
				).apply(
					p_236091_0_, 
					p_236091_0_.stable(OTGNoiseChunkGenerator::new)
				)
			;
		}
	);

	private final BlockState defaultBlock;
	private final BlockState defaultFluid;
	private final Supplier<DimensionSettings> dimensionSettingsSupplier;
	private final long worldSeed;
	private final int noiseHeight;

	private final OTGChunkGenerator internalGenerator;
	private final OTGChunkPopulator chunkPopulator;
	
	// TODO: Move this to WorldLoader when ready?
	private CustomStructureCache structureCache;
	
	private final DimensionConfig dimensionConfig;
	private final Preset preset;
	
	// Unloaded chunk data caches for BO4's
	private final FifoMap<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache;
	private final FifoMap<ChunkCoordinate, IChunk> unloadedChunksCache;
	//
	
	public OTGNoiseChunkGenerator(BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(new DimensionConfig(OTG.getEngine().getPresetLoader().getDefaultPresetName(), 0, true), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	private OTGNoiseChunkGenerator(String dimensionConfigYaml, BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(DimensionConfig.fromYamlString(dimensionConfigYaml), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}
	
	public OTGNoiseChunkGenerator(DimensionConfig dimensionConfig, BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(dimensionConfig, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}
	
	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	private OTGNoiseChunkGenerator(DimensionConfig dimensionConfigSupplier, BiomeProvider biomeProvider1, BiomeProvider biomeProvider2, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		super(biomeProvider1, biomeProvider2, dimensionSettingsSupplier.get().func_236108_a_(), seed);

		if (!(biomeProvider1 instanceof LayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		this.dimensionConfig = dimensionConfigSupplier;
		this.worldSeed = seed;
		DimensionSettings dimensionsettings = dimensionSettingsSupplier.get();
		this.dimensionSettingsSupplier = dimensionSettingsSupplier;
		NoiseSettings noisesettings = dimensionsettings.func_236113_b_();
		this.noiseHeight = noisesettings.func_236169_a_();
		this.defaultBlock = dimensionsettings.func_236115_c_();
		this.defaultFluid = dimensionsettings.func_236116_d_();
		
		// Unloaded chunk data caches for BO4's
        // TODO: Add a setting to the worldconfig for the size of these caches. 
        // Worlds with lots of BO4's and large smoothing areas may want to increase this. 
        this.unloadedBlockColumnsCache = new FifoMap<BlockPos2D, LocalMaterialData[]>(1024);
        this.unloadedChunksCache = new FifoMap<ChunkCoordinate, IChunk>(128);
        //

		this.preset = OTG.getEngine().getPresetLoader().getPresetByName(this.dimensionConfig.PresetName);
        
		this.internalGenerator = new OTGChunkGenerator(preset, seed, (LayerSource) biomeProvider1);
		this.chunkPopulator = new OTGChunkPopulator();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public ChunkGenerator func_230349_a_(long seed)
	{
		return new OTGNoiseChunkGenerator(this.dimensionConfig, this.biomeProvider.func_230320_a_(seed), seed, this.dimensionSettingsSupplier);
	}
	
	// TODO: Move this to WorldLoader when ready?
	private boolean isInitialised = false;
	private void init(String worldName)
	{
		if(!isInitialised)
		{
			isInitialised = true;
			// TODO: PresetNameProvider / ModLoadedCheckProvider
			this.structureCache = new CustomStructureCache(worldName, Paths.get("./saves/" + worldName + "/"), 0, this.worldSeed, this.preset.getWorldConfig().isOTGPlus(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().spawnLog, OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), null, OTG.getEngine().getMaterialReader(), OTG.getEngine().getCustomObjectResourcesManager(), null);
			//this.structureCache = new CustomStructureCache(worldName, Paths.get("./saves/" + worldName + "/"), 0, this.worldSeed, this.preset.getWorldConfig().isOTGPlus); 
			DimensionsConfig dimensionsConfig = new DimensionsConfig(Paths.get("./saves/" + worldName + "/"), worldName);
			dimensionsConfig.WorldName = worldName;
			dimensionsConfig.Overworld = this.dimensionConfig;
			OTG.getEngine().setDimensionsConfig(dimensionsConfig);
		}
	}
	
	// Base terrain gen
	
	// Generates the base terrain for a chunk.
	@Override
	public void func_230352_b_(IWorld world, StructureManager manager, IChunk chunk)
	{
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), world.getRandom(), buffer, buffer.getChunkCoordinate());
	}

	// Replaces surface and ground blocks in base terrain and places bedrock.
	@Override
	public void generateSurface(WorldGenRegion worldGenRegion, IChunk chunk)
	{
		// Done during this.internalGenerator.populateNoise
	}

	// Carves caves and ravines
	@Override
	public void func_230350_a_(long seed, BiomeManager biomeManager, IChunk chunk, GenerationStage.Carving stage)
	{
		if (stage == GenerationStage.Carving.AIR) {
			ChunkPrimer protoChunk = (ChunkPrimer)chunk;

			ChunkBuffer chunkBuffer = new ForgeChunkBuffer(protoChunk);
			BitSet carvingMask = protoChunk.func_230345_b_(stage); // get or create carving mask
			this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask);
		}
	}

	// Population / decoration
	
	// Does population for a given pos/chunk
	@Override
	public void func_230351_a_(WorldGenRegion worldGenRegion, StructureManager structureManager)
	{
		int chunkX = worldGenRegion.getMainChunkX();
		int chunkZ = worldGenRegion.getMainChunkZ();
		int blockX = chunkX * 16;
		int blockZ = chunkZ * 16;
		BlockPos blockpos = new BlockPos(blockX, 0, blockZ);
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
		
    	// Unloaded chunk data caches for BO4's
        this.unloadedChunksCache.remove(chunkCoord);
        //
		
		// Fetch the biomeConfig by registryKey
		RegistryKey<Biome> key = ((OTGBiomeProvider)this.biomeProvider).getBiomeRegistryKey((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);
		BiomeConfig biomeConfig = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeConfig(key.func_240901_a_().toString());

		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		long decorationSeed = sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), blockX, blockZ);
		try
		{
			// Override normal population (Biome.func_242427_a()) with OTG's.
			biomePopulate(biomeConfig, structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
		} catch (Exception exception) {
			CrashReport crashreport = CrashReport.makeCrashReport(exception, "Biome decoration");
			crashreport.makeCategory("Generation").addDetail("CenterX", chunkX).addDetail("CenterZ", chunkZ).addDetail("Seed", decorationSeed);
			throw new ReportedException(crashreport);
		}
	}

	// Chunk population method taken from Biome (Biome.func_242427_a())
	private void biomePopulate(BiomeConfig biomeConfig, StructureManager p_242427_1_, ChunkGenerator p_242427_2_, WorldGenRegion p_242427_3_, long p_242427_4_, SharedSeedRandom p_242427_6_, BlockPos p_242427_7_)
	{
		init(((IServerWorldInfo)p_242427_3_.getWorldInfo()).getWorldName());
		ChunkCoordinate chunkBeingPopulated = ChunkCoordinate.fromBlockCoords(p_242427_7_.getX(), p_242427_7_.getZ());
		this.chunkPopulator.populate(chunkBeingPopulated, new ForgeWorldGenRegion(this.preset.getName(), this.preset.getWorldConfig(), p_242427_3_, this), biomeConfig, this.structureCache);
	}
	
	// Mob spawning on initial chunk spawn (animals).
	@Override
	public void func_230354_a_(WorldGenRegion worldGenRegion)
	{
		if (!this.dimensionSettingsSupplier.get().func_236120_h_())
		{
			int i = worldGenRegion.getMainChunkX();
			int j = worldGenRegion.getMainChunkZ();
			Biome biome = worldGenRegion.getBiome((new ChunkPos(i, j)).asBlockPos());
			SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
			sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), i << 4, j << 4);
			WorldEntitySpawner.performWorldGenSpawning(worldGenRegion, biome, i, j, sharedseedrandom);
		}
	}
	
	// Mob spawning on chunk tick
	@Override
	public List<MobSpawnInfo.Spawners> func_230353_a_(Biome biome, StructureManager structureManager, EntityClassification entityClassification, BlockPos blockPos)
	{
		if (structureManager.func_235010_a_(blockPos, true, Structure.field_236374_j_).isValid())
		{
			if (entityClassification == EntityClassification.MONSTER)
			{
				return Structure.field_236374_j_.getSpawnList();
			}

			if (entityClassification == EntityClassification.CREATURE)
			{
				return Structure.field_236374_j_.getCreatureSpawnList();
			}
		}

		if (entityClassification == EntityClassification.MONSTER)
		{
			if (structureManager.func_235010_a_(blockPos, false, Structure.field_236366_b_).isValid())
			{
				return Structure.field_236366_b_.getSpawnList();
			}

			if (structureManager.func_235010_a_(blockPos, false, Structure.field_236376_l_).isValid())
			{
				return Structure.field_236376_l_.getSpawnList();
			}

			if (structureManager.func_235010_a_(blockPos, true, Structure.field_236378_n_).isValid())
			{
				return Structure.field_236378_n_.getSpawnList();
			}
		}

		return super.func_230353_a_(biome, structureManager, entityClassification, blockPos);
	}

	// Noise

	@Override
	public int func_222529_a(int x, int z, Type heightmapType)
	{
		// TODO: Used for structure spawning, implement this. See NoiseChunkGenerator func_222529_a
		return 0;
	}

	@Override
	public IBlockReader func_230348_a_(int x, int z)
	{
		// TODO: Used for structure spawning, implement this. See NoiseChunkGenerator func_230348_a_
		return null;
	}
	
	// Getters / misc
	
	@Override
	protected Codec<? extends ChunkGenerator> func_230347_a_()
	{
		return CODEC;
	}

	@Override
	public int func_230355_e_()
	{
		return this.noiseHeight;
	}

	@Override
	public int func_230356_f_()
	{
		return this.dimensionSettingsSupplier.get().func_236119_g_();
	}	
		
	// BO4's / Smoothing Areas
	
	// BO4's and smoothing areas may do material and height checks in unloaded chunks, OTG generates 
	// base terrain for the chunks in memory and caches the result in a limited size-cache.
	// TODO: Re-use the data when chunks are properly generated, or find a way to request "normal" 
	// base terrain gen outside of the WorldGenRegion chunks.
	
    private LocalMaterialData[] getBlockColumnInUnloadedChunk(IWorldGenRegion worldGenRegion, int x, int z)
    {
    	BlockPos2D blockPos = new BlockPos2D(x, z);
    	ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
    	
		// Get internal coordinates for block in chunk
    	byte blockX = (byte)(x &= 0xF);
    	byte blockZ = (byte)(z &= 0xF);

    	LocalMaterialData[] cachedColumn = this.unloadedBlockColumnsCache.get(blockPos);

    	if(cachedColumn != null)
    	{
    		return cachedColumn;
    	}

    	IChunk chunk = this.unloadedChunksCache.get(chunkCoord);
    	if(chunk == null)
    	{
			// Generate a chunk without populating it
    		chunk = getUnloadedChunk(worldGenRegion.getWorldRandom(), chunkCoord);
			unloadedChunksCache.put(chunkCoord, chunk);
    	}
		
		cachedColumn = new LocalMaterialData[256];

    	LocalMaterialData[] blocksInColumn = new LocalMaterialData[256];
    	BlockState blockInChunk;
    	for(short y = 0; y < 256; y++)
        {
        	blockInChunk = chunk.getBlockState(new BlockPos(blockX, y, blockZ));
        	if(blockInChunk != null)
        	{
	        	blocksInColumn[y] = ForgeMaterialData.ofMinecraftBlockState(blockInChunk);
        	} else {
        		break;
        	}
        }
		unloadedBlockColumnsCache.put(blockPos, cachedColumn);
		
        return blocksInColumn;
    }
    
	private IChunk getUnloadedChunk(Random random, ChunkCoordinate chunkCoordinate)
	{		
		IChunk chunk = new ChunkPrimer(new ChunkPos(chunkCoordinate.getChunkX(), chunkCoordinate.getChunkZ()), null);		
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), random, buffer, buffer.getChunkCoordinate());
		return chunk;
	}
	
    LocalMaterialData getMaterialInUnloadedChunk(IWorldGenRegion worldGenRegion, int x, int y, int z)
    {
    	LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(worldGenRegion, x,z);
        return blockColumn[y];
    }
    
    int getHighestBlockYInUnloadedChunk(IWorldGenRegion worldGenRegion, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
    {
    	int height = -1;

    	LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(worldGenRegion, x, z);
    	ForgeMaterialData material;
    	boolean isLiquid;
    	boolean isSolid;
    	
        for(int y = 255; y > -1; y--)
        {
        	material = (ForgeMaterialData) blockColumn[y];
        	isLiquid = material.isLiquid();
        	isSolid = material.isSolid() || (!ignoreSnow && material.isMaterial(LocalMaterials.SNOW));
        	if(!(isLiquid && ignoreLiquid))
        	{
            	if((findSolid && isSolid) || (findLiquid && isLiquid))
        		{
            		return y;
        		}
            	if((findSolid && isLiquid) || (findLiquid && isSolid))
            	{
            		return -1;
            	}
        	}
        }
    	return height;
    }

	double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}
}
