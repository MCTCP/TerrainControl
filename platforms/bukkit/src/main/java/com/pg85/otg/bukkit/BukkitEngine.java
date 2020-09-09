package com.pg85.otg.bukkit;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.bukkit.biomes.BukkitMojangSettings;
import com.pg85.otg.bukkit.materials.BukkitMaterialData;
import com.pg85.otg.bukkit.util.BukkitLogger;
import com.pg85.otg.bukkit.util.MobSpawnGroupHelper;
import com.pg85.otg.bukkit.world.BukkitWorld;
import com.pg85.otg.bukkit.world.WorldHelper;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.standard.MojangSettings;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.MinecraftKey;

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
    public void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
    {
        BiomeBase biome = BiomeBase.REGISTRY_ID.get(new MinecraftKey(biomeResourceLocation));
        if(biome != null)
        {
            // Merge the vanilla biome's mob spawning lists with the mob spawning lists from the BiomeConfig.
            // Mob spawning settings for the same creature will not be inherited (so BiomeConfigs can override vanilla mob spawning settings).
            // We also inherit any mobs that have been added to vanilla biomes' mob spawning lists by other mods.
            biomeConfigStub.spawnMonstersMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnMonstersMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, MojangSettings.EntityCategory.MONSTER));
            biomeConfigStub.spawnCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, MojangSettings.EntityCategory.CREATURE));
            biomeConfigStub.spawnAmbientCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnAmbientCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, MojangSettings.EntityCategory.AMBIENT_CREATURE));
            biomeConfigStub.spawnWaterCreaturesMerged = biomeConfigStub.mergeMobs(biomeConfigStub.spawnWaterCreaturesMerged, MobSpawnGroupHelper.getListFromMinecraftBiome(biome, MojangSettings.EntityCategory.WATER_CREATURE));
        }
        else
        {
            OTG.log(LogMarker.WARN, "Biome " + biomeResourceLocation + " not found for InheritMobsFromBiomeName in " + biomeConfigStub.getBiomeName() + ".bc");
        }
    }
    
    public void onSave(BukkitWorld bukkitWorld)
    {
    	if(bukkitWorld != null && bukkitWorld.getObjectSpawner().saveRequired && !bukkitWorld.getWorldSession().getPreGeneratorIsRunning())
    	{
    		bukkitWorld.getStructureCache().saveToDisk();
    	}
    }
    
    // TODO: WorldSave and WorldUnload events don't appear to be working for spigot (see OTGListener)?
    public void onSave(org.bukkit.World world)
    {
    	//OTG.log(LogMarker.INFO, "BukkitEngine onSave");
    	onSave((BukkitWorld)WorldHelper.toLocalWorld(world));
    }
}
