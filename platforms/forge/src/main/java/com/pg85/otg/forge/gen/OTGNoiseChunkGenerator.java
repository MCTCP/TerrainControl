package com.pg85.otg.forge.gen;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.dimensions.DimensionsConfig;
import com.pg85.otg.config.preset.Preset;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.gen.ChunkBuffer;
import com.pg85.otg.gen.NewOTGChunkGenerator;
import com.pg85.otg.gen.ChunkPopulator;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.Heightmap;
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
	private final ForgeChunkGenNoise chunkGenNoise;
	private final ChunkPopulator chunkPopulator;
	// TODO: Move this to WorldLoader when ready
	private final CustomStructureCache structureCache;	
	
	// TODO: Hardcoded world/preset name, should be provided via world creation gui / config.yaml.
	private final Preset preset;
	String worldName = "New World";
	String presetName = "Default";
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

		this.internalGenerator = new NewOTGChunkGenerator(seed, (LayerSource) biomeProvider, ForgeMaterialData.ofMinecraftBlockState(Blocks.STONE.getDefaultState()), ForgeMaterialData.ofMinecraftBlockState(Blocks.WATER.getDefaultState()));
		this.chunkPopulator = new ChunkPopulator();
		
		// TODO: Move this to world creation / loading logic, don't use hardcoded values.
		this.preset = OTG.getEngine().getPresetLoader().getPresetByName(this.presetName);
		this.structureCache = new CustomStructureCache(this.worldName, Paths.get("./saves/" + this.worldName + "/"), 0, seed, false); 
		DimensionsConfig dimensionsConfig = new DimensionsConfig(Paths.get("./saves/" + this.worldName + "/"), this.worldName);
		dimensionsConfig.WorldName = this.worldName;
		dimensionsConfig.Overworld = new DimensionConfig(preset.getName(), 0, true, preset.getWorldConfig());	
		dimensionsConfig.Dimensions = new ArrayList<DimensionConfig>();
		OTG.setDimensionsConfig(dimensionsConfig);
		//
		
		this.worldSeed = seed;
		DimensionSettings dimensionsettings = p_i241976_5_.get();
		this.dimensionSettings = p_i241976_5_;
		NoiseSettings noisesettings = dimensionsettings.func_236113_b_();
		this.field_236085_x_ = noisesettings.func_236169_a_();
		this.defaultBlock = dimensionsettings.func_236115_c_();
		this.defaultFluid = dimensionsettings.func_236116_d_();
		
		this.chunkGenNoise = new ForgeChunkGenNoise(this, noisesettings, seed);
	}
	
	@OnlyIn(Dist.CLIENT)
	public ChunkGenerator func_230349_a_(long p_230349_1_)
	{
		return new OTGNoiseChunkGenerator(this.biomeProvider.func_230320_a_(p_230349_1_), p_230349_1_, this.dimensionSettings);
	}	
	
	// Base terrain gen
	
	// Generates the base terrain for a chunk.
	public void func_230352_b_(IWorld world, StructureManager manager, IChunk chunk)
	{
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		this.internalGenerator.populateNoise(buffer, buffer.getChunkCoordinate());
	}
	
	// Replaces surface and ground blocks in base terrain and places bedrock.
	public void generateSurface(WorldGenRegion p_225551_1_, IChunk p_225551_2_)
	{
		ChunkPos chunkpos = p_225551_2_.getPos();
		int i = chunkpos.x;
		int j = chunkpos.z;
		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		sharedseedrandom.setBaseChunkSeed(i, j);
		ChunkPos chunkpos1 = p_225551_2_.getPos();
		int k = chunkpos1.getXStart();
		int l = chunkpos1.getZStart();
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

		for (int i1 = 0; i1 < 16; ++i1)
		{
			for (int j1 = 0; j1 < 16; ++j1)
			{
				int k1 = k + i1;
				int l1 = l + j1;
				int i2 = p_225551_2_.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, i1, j1) + 1;
				// TODO: This should be provided by NewOTGChunkGenerator?
				double d1 = this.chunkGenNoise.getSurfaceDepthNoise().noiseAt((double) k1 * 0.0625D, (double) l1 * 0.0625D, 0.0625D, (double) i1 * 0.0625D) * 15.0D;
				p_225551_1_.getBiome(blockpos$mutable.setPos(k + i1, i2, l + j1)).buildSurface(sharedseedrandom, p_225551_2_, k1, l1, i2, d1, this.defaultBlock, this.defaultFluid, this.func_230356_f_(), p_225551_1_.getSeed());
			}
		}

		this.makeBedrock(p_225551_2_, sharedseedrandom);
	}

	private void makeBedrock(IChunk chunkIn, Random rand)
	{
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		int i = chunkIn.getPos().getXStart();
		int j = chunkIn.getPos().getZStart();
		DimensionSettings dimensionsettings = this.dimensionSettings.get();
		int k = dimensionsettings.func_236118_f_();
		int l = this.field_236085_x_ - 1 - dimensionsettings.func_236117_e_();
		boolean flag = l + 4 >= 0 && l < this.field_236085_x_;
		boolean flag1 = k + 4 >= 0 && k < this.field_236085_x_;
		if (flag || flag1)
		{
			for (BlockPos blockpos : BlockPos.getAllInBoxMutable(i, 0, j, i + 15, 0, j + 15))
			{
				if (flag)
				{
					for (int j1 = 0; j1 < 5; ++j1)
					{
						if (j1 <= rand.nextInt(5))
						{
							chunkIn.setBlockState(blockpos$mutable.setPos(blockpos.getX(), l - j1, blockpos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
						}
					}
				}

				if (flag1)
				{
					for (int k1 = 4; k1 >= 0; --k1)
					{
						if (k1 <= rand.nextInt(5))
						{
							chunkIn.setBlockState(blockpos$mutable.setPos(blockpos.getX(), k + k1, blockpos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
						}
					}
				}
			}
		}
	}	
	
	// Population / decoration
	
	// Does population for a given pos/chunk
	@Override
	public void func_230351_a_(WorldGenRegion p_230351_1_, StructureManager p_230351_2_)
	{
		int i = p_230351_1_.getMainChunkX();
		int j = p_230351_1_.getMainChunkZ();
		int k = i * 16;
		int l = j * 16;
		BlockPos blockpos = new BlockPos(k, 0, l);
		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		long i1 = sharedseedrandom.setDecorationSeed(p_230351_1_.getSeed(), k, l);

		// Fetch the biomeConfig by registryKey
		RegistryKey<Biome> key = ((OTGBiomeProvider)this.biomeProvider).getBiomeRegistryKey((i << 2) + 2, 2, (j << 2) + 2);
		BiomeConfig biomeConfig = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeConfig(key.func_240901_a_().toString());

		try
		{
			// Override normal population (Biome.func_242427_a()) with OTG's.
			func_242427_a(biomeConfig, p_230351_2_, this, p_230351_1_, i1, sharedseedrandom, blockpos);
		} catch (Exception exception) {
			CrashReport crashreport = CrashReport.makeCrashReport(exception, "Biome decoration");
			crashreport.makeCategory("Generation").addDetail("CenterX", i).addDetail("CenterZ", j).addDetail("Seed", i1);
			throw new ReportedException(crashreport);
		}
	}

	// Chunk population method taken from Biome (Biome.func_242427_a())
	public void func_242427_a(BiomeConfig biomeConfig, StructureManager p_242427_1_, ChunkGenerator p_242427_2_, WorldGenRegion p_242427_3_, long p_242427_4_, SharedSeedRandom p_242427_6_, BlockPos p_242427_7_)
	{
		ChunkCoordinate chunkBeingPopulated = ChunkCoordinate.fromBlockCoords(p_242427_7_.getX(), p_242427_7_.getZ());
		this.chunkPopulator.populate(chunkBeingPopulated, this.structureCache, new ForgeWorldGenRegion(this.worldName, this.worldSeed, this.preset.getWorldConfig(), p_242427_3_, this), biomeConfig);
	}
	
	// Mob spawning on initial chunk spawn (animals).
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
	
	// TODO: This should use NewOTGChunkGenerator, not chunkGenNoise? Are these ever called?
	@Override
	public int func_222529_a(int p_222529_1_, int p_222529_2_, Type heightmapType)
	{
		return this.chunkGenNoise.func_222529_a(p_222529_1_, p_222529_2_, heightmapType);
	}

	// TODO: This should use NewOTGChunkGenerator, not chunkGenNoise? Are these ever called?
	@Override
	public IBlockReader func_230348_a_(int p_230348_1_, int p_230348_2_)
	{
		return this.chunkGenNoise.func_230348_a_(p_230348_1_, p_230348_2_);
	}
	
	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	public BiomeProvider getBiomeProvider1()
	{
		return this.biomeProvider;
	}

	// Getters / misc
	
	protected Codec<? extends ChunkGenerator> func_230347_a_()
	{
		return CODEC;
	}

	public int func_230355_e_()
	{
		return this.field_236085_x_;
	}

	public int func_230356_f_()
	{
		return this.dimensionSettings.get().func_236119_g_();
	}
	
	public boolean func_236088_a_(long p_236088_1_, RegistryKey<DimensionSettings> p_236088_3_)
	{
		return this.worldSeed == p_236088_1_ && this.dimensionSettings.get().func_242744_a(p_236088_3_);
	}
}
