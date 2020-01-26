package com.pg85.otg.bukkit;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
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
        // Try parsing as an internal Minecraft name
        // This is so that things like "minecraft:stone" aren't parsed
        // as the block "minecraft" with data "stone", but instead as the
        // block "minecraft:stone" with no block data.
        Block block = Block.getByName(input);
        if (block != null)
        {
            return BukkitMaterialData.ofMinecraftBlock(block);
        }

        try
        {
            // Try block(:data) syntax
            return getMaterial0(input);
        } catch (NumberFormatException e)
        {
            throw new InvalidConfigException("Unknown material: " + input);
        }    
    }

    @SuppressWarnings("deprecation")
    private LocalMaterialData getMaterial0(String input) throws NumberFormatException
    {
        String blockName = input;
        int blockData = -1;

        // When there is a . or a : in the name, extract block data
        int splitIndex = input.lastIndexOf(":");
        if (splitIndex == -1)
        {
            splitIndex = input.lastIndexOf(".");
        }
        if (splitIndex != -1)
        {
            blockName = input.substring(0, splitIndex);
            blockData = Integer.parseInt(input.substring(splitIndex + 1));
        }

        // Parse block name
        Block block = Block.getByName(blockName);
        if (block == null)
        {
            DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(blockName);
            if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
            {
                block = Block.getById(defaultMaterial.id);
            }
        }

        // Get the block
        if (block != null)
        {
            if (blockData == -1)
            {
                // Use default
                return BukkitMaterialData.ofMinecraftBlock(block);
            } else
            {
                // Use specified data
                try
                {
                    return BukkitMaterialData.ofMinecraftBlockData(block.fromLegacyData(blockData));
                } catch (IllegalArgumentException e)
                {
                	OTG.log(LogMarker.WARN, "Illegal block data for the block type, cannot use " + input);
                    return null;
                }
            }
        }

        // Failed
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
