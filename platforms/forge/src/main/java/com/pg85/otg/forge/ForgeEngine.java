package com.pg85.otg.forge;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.biome.BiomeLoadInstruction;
import com.pg85.otg.config.minecraft.DefaultBiome;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.ForgeMojangSettings;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialReader;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.forge.util.ForgeLogger;
import com.pg85.otg.forge.util.ForgeModLoadedChecker;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ForgeEngine extends OTGEngine
{
	public ForgeEngine()
	{
		super(			
			new ForgeLogger(), 
			Paths.get(FMLLoader.getGamePath().toString(), File.separator + "config" + File.separator + Constants.MOD_ID),
			new ForgeMaterialReader(),
			new ForgeModLoadedChecker(),			
			new ForgePresetLoader(Paths.get(FMLLoader.getGamePath().toString(), File.separator + "config" + File.separator + Constants.MOD_ID))
		);
	}

	@Override
	public Collection<BiomeLoadInstruction> getDefaultBiomes()
	{
		// Loop through all default biomes and create the default settings for them
		List<BiomeLoadInstruction> standardBiomes = new ArrayList<BiomeLoadInstruction>();
		for (DefaultBiome defaultBiome : DefaultBiome.values())
		{
			int id = defaultBiome.Id;
			BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(ForgeMojangSettings.fromId(id),
					128, OTG.getEngine().getLogger()); // TODO: Why is this 128, should be 255?
			standardBiomes.add(instruction);
		}

		return standardBiomes;
	}

	@Override
	public void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{
		// TODO: Implement this
	}

	@Override
	public File getJarFile()
	{
		return ModList.get().getModFileById(Constants.MOD_ID_SHORT).getFile().getFilePath().toFile();
	}

	public void onSave(IWorld world)
	{
		// For server worlds, save the structure cache.
		if(
			!world.isRemote() && 
			world.getChunkProvider() instanceof ServerChunkProvider && 
			((ServerChunkProvider)world.getChunkProvider()).generator instanceof OTGNoiseChunkGenerator
		)
		{
			((OTGNoiseChunkGenerator)((ServerChunkProvider)world.getChunkProvider()).generator).saveStructureCache();
		}
	}
}
