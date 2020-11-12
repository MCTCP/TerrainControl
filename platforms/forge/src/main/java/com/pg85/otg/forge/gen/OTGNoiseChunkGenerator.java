package com.pg85.otg.forge.gen;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.common.materials.LocalMaterials;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.dimensions.DimensionsConfig;
import com.pg85.otg.config.preset.Preset;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.gen.ChunkBuffer;
import com.pg85.otg.gen.ChunkPopulator;
import com.pg85.otg.gen.NewOTGChunkGenerator;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;

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
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.NoiseSettings;
import net.minecraft.world.spawner.WorldEntitySpawner;

public final class OTGNoiseChunkGenerator extends ChunkGenerator
{
	public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
		(p_236091_0_) -> p_236091_0_.group(
			BiomeProvider.field_235202_a_.fieldOf("biome_source").forGetter((p_236096_0_) -> p_236096_0_.biomeProvider),
			Codec.LONG.fieldOf("seed").stable().forGetter((p_236093_0_) -> p_236093_0_.worldSeed),
			DimensionSettings.field_236098_b_.fieldOf("settings").forGetter((p_236090_0_) -> p_236090_0_.dimensionSettings)
		).apply(p_236091_0_, p_236091_0_.stable(OTGNoiseChunkGenerator::new))
	);

	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;
	protected final Supplier<DimensionSettings> dimensionSettings;
	private final long worldSeed;
	private final int field_236085_x_;

	private final NewOTGChunkGenerator internalGenerator;
	private final ChunkPopulator chunkPopulator;
	// TODO: Move this to WorldLoader when ready
	private final CustomStructureCache structureCache;
	
	// TODO: Hardcoded world/preset name, should be provided via world creation gui / config.yaml.
	private final Preset preset;
	private final boolean isBo4Enabled = false; // Set to true for BO4 testing.
	private final String worldName = "New World";
	private final String presetName = "Default";
	//
	
	// Unloaded chunk data caches for BO4's
	private final FifoMap<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache;
	private final FifoMap<ChunkCoordinate, IChunk> unloadedChunksCache;
	//
	
	public OTGNoiseChunkGenerator(BiomeProvider p_i241975_1_, long p_i241975_2_, Supplier<DimensionSettings> p_i241975_4_)
	{
		this(p_i241975_1_, p_i241975_1_, p_i241975_2_, p_i241975_4_);
	}

	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	private OTGNoiseChunkGenerator(BiomeProvider biomeProvider, BiomeProvider p_i241976_2_, long seed, Supplier<DimensionSettings> p_i241976_5_)
	{
		super(biomeProvider, p_i241976_2_, p_i241976_5_.get().func_236108_a_(), seed);

		if (!(biomeProvider instanceof LayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}
			
		this.worldSeed = seed;
		DimensionSettings dimensionsettings = p_i241976_5_.get();
		this.dimensionSettings = p_i241976_5_;
		NoiseSettings noisesettings = dimensionsettings.func_236113_b_();
		this.field_236085_x_ = noisesettings.func_236169_a_();
		this.defaultBlock = dimensionsettings.func_236115_c_();
		this.defaultFluid = dimensionsettings.func_236116_d_();
		
		// Unloaded chunk data caches for BO4's
        // TODO: Add a setting to the worldconfig for the size of these caches. 
        // Worlds with lots of BO4's and large smoothing areas may want to increase this. 
        this.unloadedBlockColumnsCache = new FifoMap<BlockPos2D, LocalMaterialData[]>(1024);
        this.unloadedChunksCache = new FifoMap<ChunkCoordinate, IChunk>(128);
        //

		// TODO: Move this to world creation / loading logic, don't use hardcoded values.
		this.preset = OTG.getEngine().getPresetLoader().getPresetByName(this.presetName);
		this.structureCache = new CustomStructureCache(this.worldName, Paths.get("./saves/" + this.worldName + "/"), 0, seed, false); 
		DimensionsConfig dimensionsConfig = new DimensionsConfig(Paths.get("./saves/" + this.worldName + "/"), this.worldName);
		dimensionsConfig.WorldName = this.worldName;
		dimensionsConfig.Overworld = new DimensionConfig(preset.getName(), 0, true, preset.getWorldConfig());	
		dimensionsConfig.Dimensions = new ArrayList<DimensionConfig>();
		OTG.setDimensionsConfig(dimensionsConfig);
		//
        
		this.internalGenerator = new NewOTGChunkGenerator(seed, (LayerSource) biomeProvider);
		this.chunkPopulator = new ChunkPopulator();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public ChunkGenerator func_230349_a_(long p_230349_1_)
	{
		return new OTGNoiseChunkGenerator(this.biomeProvider.func_230320_a_(p_230349_1_), p_230349_1_, this.dimensionSettings);
	}	
	
	// Base terrain gen
	
	// Generates the base terrain for a chunk.
	@Override
	public void func_230352_b_(IWorld world, StructureManager manager, IChunk chunk)
	{
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		this.internalGenerator.populateNoise(this.preset.getWorldConfig(), world.getRandom(), buffer, buffer.getChunkCoordinate());
	}

	// Replaces surface and ground blocks in base terrain and places bedrock.
	@Override
	public void generateSurface(WorldGenRegion p_225551_1_, IChunk p_225551_2_)
	{
		// Done during this.internalGenerator.populateNoise
	}
	
	// Population / decoration
	
	// Does population for a given pos/chunk
	@Override
	public void func_230351_a_(WorldGenRegion p_230351_1_, StructureManager p_230351_2_)
	{
		int chunkX = p_230351_1_.getMainChunkX();
		int chunkZ = p_230351_1_.getMainChunkZ();
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
		long decorationSeed = sharedseedrandom.setDecorationSeed(p_230351_1_.getSeed(), blockX, blockZ);
		try
		{
			// Override normal population (Biome.func_242427_a()) with OTG's.
			biomePopulate(biomeConfig, p_230351_2_, this, p_230351_1_, decorationSeed, sharedseedrandom, blockpos);
		} catch (Exception exception) {
			CrashReport crashreport = CrashReport.makeCrashReport(exception, "Biome decoration");
			crashreport.makeCategory("Generation").addDetail("CenterX", chunkX).addDetail("CenterZ", chunkZ).addDetail("Seed", decorationSeed);
			throw new ReportedException(crashreport);
		}
	}

	// Chunk population method taken from Biome (Biome.func_242427_a())
	public void biomePopulate(BiomeConfig biomeConfig, StructureManager p_242427_1_, ChunkGenerator p_242427_2_, WorldGenRegion p_242427_3_, long p_242427_4_, SharedSeedRandom p_242427_6_, BlockPos p_242427_7_)
	{
		ChunkCoordinate chunkBeingPopulated = ChunkCoordinate.fromBlockCoords(p_242427_7_.getX(), p_242427_7_.getZ());
		this.chunkPopulator.populate(chunkBeingPopulated, this.structureCache, new ForgeWorldGenRegion(this.worldName, this.worldSeed, this.preset.getWorldConfig(), p_242427_3_, this), biomeConfig, this.isBo4Enabled);
	}
	
	// Mob spawning on initial chunk spawn (animals).
	@Override
	public void func_230354_a_(WorldGenRegion p_230354_1_)
	{
		if (!this.dimensionSettings.get().func_236120_h_())
		{
			int i = p_230354_1_.getMainChunkX();
			int j = p_230354_1_.getMainChunkZ();
			Biome biome = p_230354_1_.getBiome((new ChunkPos(i, j)).asBlockPos());
			SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
			sharedseedrandom.setDecorationSeed(p_230354_1_.getSeed(), i << 4, j << 4);
			WorldEntitySpawner.performWorldGenSpawning(p_230354_1_, biome, i, j, sharedseedrandom);
		}
	}
	
	// Mob spawning on chunk tick
	@Override
	public List<MobSpawnInfo.Spawners> func_230353_a_(Biome p_230353_1_, StructureManager p_230353_2_, EntityClassification p_230353_3_, BlockPos p_230353_4_)
	{
		if (p_230353_2_.func_235010_a_(p_230353_4_, true, Structure.field_236374_j_).isValid())
		{
			if (p_230353_3_ == EntityClassification.MONSTER)
			{
				return Structure.field_236374_j_.getSpawnList();
			}

			if (p_230353_3_ == EntityClassification.CREATURE)
			{
				return Structure.field_236374_j_.getCreatureSpawnList();
			}
		}

		if (p_230353_3_ == EntityClassification.MONSTER)
		{
			if (p_230353_2_.func_235010_a_(p_230353_4_, false, Structure.field_236366_b_).isValid())
			{
				return Structure.field_236366_b_.getSpawnList();
			}

			if (p_230353_2_.func_235010_a_(p_230353_4_, false, Structure.field_236376_l_).isValid())
			{
				return Structure.field_236376_l_.getSpawnList();
			}

			if (p_230353_2_.func_235010_a_(p_230353_4_, true, Structure.field_236378_n_).isValid())
			{
				return Structure.field_236378_n_.getSpawnList();
			}
		}

		return super.func_230353_a_(p_230353_1_, p_230353_2_, p_230353_3_, p_230353_4_);
	}	

	// Noise
	
	@Override
	public int func_222529_a(int p_222529_1_, int p_222529_2_, Type heightmapType)
	{
		// Not used for OTG dimensions
		// TODO: Make sure this won't cause problems, hook up via NewOTGChunkGenerator?		
		return 0;
	}

	@Override
	public IBlockReader func_230348_a_(int p_230348_1_, int p_230348_2_)
	{
		// Not used for OTG dimensions
		// TODO: Make sure this won't cause problems, hook up via NewOTGChunkGenerator?
		return null;
	}
	
	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	public BiomeProvider getBiomeProvider1()
	{
		return this.biomeProvider;
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
		return this.field_236085_x_;
	}

	@Override
	public int func_230356_f_()
	{
		return this.dimensionSettings.get().func_236119_g_();
	}	
		
	// BO4's / Smoothing Areas
	
	// BO4's and smoothing areas may do material and height checks in unloaded chunks, OTG generates 
	// base terrain for the chunks in memory and caches the result in a limited size-cache.
	// TODO: Re-use the data when chunks are properly generated, or find a way to request "normal" 
	// base terrain gen outside of the WorldGenRegion chunks.
	
    public LocalMaterialData[] getBlockColumnInUnloadedChunk(LocalWorldGenRegion worldGenRegion, int x, int z)
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
    	   
    	IChunk chunk = ((ForgeWorldGenRegion)worldGenRegion).getChunk(chunkCoord);
    	if(chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.HEIGHTMAPS))
    	{
    		chunk = this.unloadedChunksCache.get(chunkCoord);
    	} else {
    		this.unloadedChunksCache.remove(chunkCoord);
    	}
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
    
	public IChunk getUnloadedChunk(Random random, ChunkCoordinate chunkCoordinate)
	{		
		IChunk chunk = new ChunkPrimer(new ChunkPos(chunkCoordinate.getChunkX(), chunkCoordinate.getChunkZ()), null);		
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		this.internalGenerator.populateNoise(this.preset.getWorldConfig(), random, buffer, buffer.getChunkCoordinate());
		return chunk;
	}
	
    public LocalMaterialData getMaterialInUnloadedChunk(LocalWorldGenRegion worldGenRegion, int x, int y, int z)
    {
    	LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(worldGenRegion, x,z);
        return blockColumn[y];
    }
    
    public int getHighestBlockYInUnloadedChunk(LocalWorldGenRegion worldGenRegion, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
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
}
