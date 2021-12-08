package com.pg85.otg.forge;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTGEngine;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterials;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.forge.util.ForgeLogger;
import com.pg85.otg.forge.util.ForgeModLoadedChecker;
import net.minecraft.core.WritableRegistry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import java.io.File;
import java.nio.file.Paths;

public class ForgeEngine extends OTGEngine
{
	public ForgeEngine()
	{
		super(				
			new ForgeLogger(), 
			Paths.get(FMLLoader.getGamePath().toString(), File.separator + "config" + File.separator + Constants.MOD_ID),
			new ForgeModLoadedChecker(),
			new ForgePresetLoader(Paths.get(FMLLoader.getGamePath().toString(), File.separator + "config" + File.separator + Constants.MOD_ID))
		);
	}
	
	@Override
	public void onStart()
	{
		ForgeMaterials.init();
		super.onStart();
	}

	public void reloadPreset(String presetFolderName, WritableRegistry<Biome> biomeRegistry)
	{
		((ForgePresetLoader)this.presetLoader).reloadPresetFromDisk(presetFolderName, this.biomeResourcesManager, this.logger, biomeRegistry);
	}
	
	public void onSave(LevelAccessor world)
	{
		// For server worlds, save the structure cache.
		if(
			!world.isClientSide() && 
			world.getChunkSource() instanceof ServerChunkCache && 
			((ServerChunkCache)world.getChunkSource()).getGenerator() instanceof OTGNoiseChunkGenerator
		)
		{
			((OTGNoiseChunkGenerator)((ServerChunkCache)world.getChunkSource()).getGenerator()).saveStructureCache();
		}
	}

	public void onUnload(LevelAccessor world)
	{
		// For server worlds, stop any worker threads.
		if(
			!world.isClientSide() && 
			world.getChunkSource() instanceof ServerChunkCache && 
			((ServerChunkCache)world.getChunkSource()).getGenerator() instanceof OTGNoiseChunkGenerator
		)
		{
			((OTGNoiseChunkGenerator)((ServerChunkCache)world.getChunkSource()).getGenerator()).stopWorkerThreads();
		}
	}
	
	@Override
	public File getJarFile()
	{
		File modFile = ModList.get().getModFileById(Constants.MOD_ID_SHORT).getFile().getFilePath().toFile();
		if(!modFile.isFile())
		{
			return null;
		}
		return modFile;
	}
}
