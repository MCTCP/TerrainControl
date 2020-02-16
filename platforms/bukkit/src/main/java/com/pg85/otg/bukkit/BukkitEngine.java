package com.pg85.otg.bukkit;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.bukkit.biomes.BukkitMojangSettings;
import com.pg85.otg.bukkit.materials.BukkitMaterialData;
import com.pg85.otg.bukkit.util.BukkitLogger;
import com.pg85.otg.bukkit.world.BukkitWorld;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.server.v1_12_R1.Block;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitEngine extends OTGEngine
{
    private final OTGPlugin plugin;

    BukkitEngine(OTGPlugin plugin)
    {
        super(new BukkitLogger(plugin.getLogger()));
        this.plugin = plugin;
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        return plugin.worlds.get(name);
    }

    @Override
    public File getOTGRootFolder()
    {
        return plugin.getDataFolder();
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(this.getOTGRootFolder(), PluginStandardValues.BO_DirectoryName);
    }

    @Override
    public File getWorldsDirectory()
    {
        return new File(this.getOTGRootFolder(), PluginStandardValues.PresetsDirectoryName);
    }
    
    @Override
    public LocalMaterialData readMaterial(String input) throws InvalidConfigException
    {
    	return BukkitMaterialData.ofString(input);
    }

    @Override
    public LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return BukkitMaterialData.ofDefaultMaterial(defaultMaterial, blockData);
    }
	
    @Override
    public ArrayList<LocalWorld> getAllWorlds()
    {
    	ArrayList<LocalWorld> worlds = new ArrayList<LocalWorld>();
    	worlds.addAll(plugin.worlds.values());
    	return worlds;
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
            BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(BukkitMojangSettings.fromId(id), BukkitWorld.STANDARD_WORLD_HEIGHT);
            standardBiomes.add(instruction);
        }

        return standardBiomes;
    }
    
    // Only used for Forge atm TODO: Put in Forge layer only, not common.
    
	@Override
	public LocalWorld getUnloadedWorld(String name)
	{
		return null;
	}

	// Mods are never loaded on Spigot
	
	@Override
	public boolean isModLoaded(String mod)
	{
		return false;
	}

	@Override
	public boolean areEnoughBiomeIdsAvailableForPresets(ArrayList<String> presetNames)
	{
		// TODO: Implement this
		return true;
	}

    @Override
    public void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation) { }
}
