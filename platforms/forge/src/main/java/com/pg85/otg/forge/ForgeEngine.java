package com.pg85.otg.forge;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.biome.BiomeLoadInstruction;
import com.pg85.otg.config.minecraft.DefaultBiome;
import com.pg85.otg.config.standard.MojangSettings.EntityCategory;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.MobSpawnGroupHelper;
import com.pg85.otg.forge.biome.ForgeMojangSettings;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialReader;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.forge.util.ForgeLogger;
import com.pg85.otg.forge.util.ForgeModLoadedChecker;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;

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
		String[] resourceLocationArr = biomeResourceLocation.split(":");		    
		String resourceDomain = resourceLocationArr.length > 1 ? resourceLocationArr[0] : null;
		String resourceLocation = resourceLocationArr.length > 1 ? resourceLocationArr[1] : resourceLocationArr[0];
			
		Biome biome = null;
		try
		{
			ResourceLocation location = new ResourceLocation(resourceDomain, resourceLocation);
			biome = ForgeRegistries.BIOMES.getValue(location);
		}
		catch(ResourceLocationException ex)
		{
			// Can happen when no biome is registered or input is otherwise invalid.
		}
		if(biome != null)
		{
			// Merge the vanilla biome's mob spawning lists with the mob spawning lists from the BiomeConfig.
			// Mob spawning settings for the same creature will not be inherited (so BiomeConfigs can override vanilla mob spawning settings).
			// We also inherit any mobs that have been added to vanilla biomes' mob spawning lists by other mods.
			biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityClassification.MONSTER), EntityCategory.MONSTER);
			biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityClassification.AMBIENT), EntityCategory.AMBIENT_CREATURE);
			biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityClassification.CREATURE), EntityCategory.CREATURE);
			biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityClassification.WATER_AMBIENT), EntityCategory.WATER_AMBIENT);
			biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityClassification.WATER_CREATURE), EntityCategory.WATER_CREATURE);
			biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biome, EntityClassification.MISC), EntityCategory.MISC);
		} else {
			if(OTG.getEngine().getPluginConfig().getDeveloperModeEnabled())
			{
				OTG.log(LogMarker.WARN, "Could not inherit mobs for unrecognised biome \"" +  biomeResourceLocation + "\" in " + biomeConfigStub.getBiomeName() + Constants.BiomeConfigFileExtension);
			}
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

	public void onSave(IWorld world)
	{
		// For server worlds, save the structure cache.
		if(
			!world.isClientSide() && 
			world.getChunkSource() instanceof ServerChunkProvider && 
			((ServerChunkProvider)world.getChunkSource()).generator instanceof OTGNoiseChunkGenerator
		)
		{
			((OTGNoiseChunkGenerator)((ServerChunkProvider)world.getChunkSource()).generator).saveStructureCache();
		}
	}
}
