package com.pg85.otg.forge;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pg85.otg.OTGEngine;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.config.biome.BiomeLoadInstruction;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.forge.biome.ForgeMojangSettings;
import com.pg85.otg.forge.materials.ForgeMaterials;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.forge.util.ForgeLogger;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;

import net.minecraftforge.fml.loading.FMLLoader;

public class ForgeEngine extends OTGEngine
{
	public ForgeEngine()
	{
		super(
			new ForgeLogger(), 
			Paths.get(FMLLoader.getGamePath().toString(), File.separator + "config" + File.separator + PluginStandardValues.PLUGIN_NAME), 
			new ForgePresetLoader(Paths.get(FMLLoader.getGamePath().toString(), File.separator + "config" + File.separator + PluginStandardValues.PLUGIN_NAME))
		);
	}

	@Override
	public LocalWorld getWorld(String name)
	{
		// TODO: Implement this
		return null;
	}

	@Override
	public LocalWorld getUnloadedWorld(String name)
	{
		// TODO: Implement this
		return null;
	}

	@Override
	public ArrayList<LocalWorld> getAllWorlds()
	{
		// TODO: Implement this
		return null;
	}

	@Override
	public boolean isModLoaded(String mod)
	{
		// TODO: Implement this
		return false;
	}

	@Override
	public boolean areEnoughBiomeIdsAvailableForPresets(ArrayList<String> presetNames)
	{
		// TODO: Implement this
		return false;
	}

	@Override
	public Collection<BiomeLoadInstruction> getDefaultBiomes()
	{
        // Loop through all default biomes and create the default settings for them
        List<BiomeLoadInstruction> standardBiomes = new ArrayList<BiomeLoadInstruction>();
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            int id = defaultBiome.Id;
            BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(ForgeMojangSettings.fromId(id), 128); // TODO: Why is this 128, should be 255?
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
	public LocalMaterialData readMaterial(String name) throws InvalidConfigException
	{
		return ForgeMaterials.readMaterial(name);
	}
}
