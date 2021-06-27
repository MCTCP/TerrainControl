package com.pg85.otg.spigot;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.config.biome.BiomeConfigFinder;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.spigot.biome.MobSpawnGroupHelper;
import com.pg85.otg.spigot.materials.SpigotMaterials;
import com.pg85.otg.spigot.presets.SpigotPresetLoader;
import com.pg85.otg.spigot.util.SpigotLogger;
import com.pg85.otg.spigot.util.SpigotPluginLoadedChecker;
import com.pg85.otg.util.minecraft.EntityCategory;

import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.EnumCreatureType;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.RegistryGeneration;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SpigotEngine extends OTGEngine
{
	private final JavaPlugin plugin;

	protected SpigotEngine (JavaPlugin plugin)
	{
		super(
			new SpigotLogger(),
			plugin.getDataFolder().toPath(),
			new SpigotPluginLoadedChecker(),
			new SpigotPresetLoader(plugin.getDataFolder())
		);
		this.plugin = plugin;
	}

	@Override
	public void onStart()
	{
		SpigotMaterials.init();
		super.onStart();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void mergeVanillaBiomeMobSpawnSettings (BiomeConfigFinder.BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{
		String[] resourceLocationArr = biomeResourceLocation.split(":");			
		String resourceDomain = resourceLocationArr.length > 1 ? resourceLocationArr[0] : null;
		String resourceLocation = resourceLocationArr.length > 1 ? resourceLocationArr[1] : resourceLocationArr[0];		
		
		NamespacedKey location = null;
		try
		{
			location = new NamespacedKey(resourceDomain, resourceLocation);
		}
		catch(IllegalArgumentException ex)
		{
			// Can happen when input is invalid.
		}
		
		if(location != null)
		{
			Biome biome = Registry.BIOME.get(location);
			BiomeBase biomeBase = null;
			if(biome != null)
			{
				biomeBase = RegistryGeneration.WORLDGEN_BIOME.get(new MinecraftKey(biome.getKey().toString()));		
			}
			if(biomeBase != null)
			{
				// Merge the vanilla biome's mob spawning lists with the mob spawning lists from the BiomeConfig.
				// Mob spawning settings for the same creature will not be inherited (so BiomeConfigs can override vanilla mob spawning settings).
				// We also inherit any mobs that have been added to vanilla biomes' mob spawning lists by other mods.
				
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.MONSTER), EntityCategory.MONSTER);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.AMBIENT), EntityCategory.AMBIENT_CREATURE);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.CREATURE), EntityCategory.CREATURE);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.WATER_AMBIENT), EntityCategory.WATER_AMBIENT);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.WATER_CREATURE), EntityCategory.WATER_CREATURE);
				biomeConfigStub.mergeMobs(MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, EnumCreatureType.MISC), EntityCategory.MISC);
				return;
			}
		}
		if(OTG.getEngine().getPluginConfig().getDeveloperModeEnabled())
		{
			OTG.log(LogMarker.WARN, "Could not inherit mobs for unrecognised biome \"" +  biomeResourceLocation + "\" in " + biomeConfigStub.getBiomeName() + Constants.BiomeConfigFileExtension);
		}
	}

	@Override
	public File getJarFile()
	{
		String fileName = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
		// URLEncoded string, decode.
		try {
			fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) { }
		
		if(fileName != null)
		{
			File modFile = new File(fileName);
			if(modFile.isFile())
			{
				return modFile;
			}
		}
		return null;
	}
}
