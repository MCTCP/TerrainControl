package com.pg85.otg.forge;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfigFinder;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.forge.biomes.ForgeBiomeRegistryManager;
import com.pg85.otg.forge.generator.ForgeChunkBuffer;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.util.ForgeLogger;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.forge.world.ForgeWorldSession;
import com.pg85.otg.forge.world.WorldLoader;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

public class ForgeEngine extends OTGEngine
{
	private ForgeBiomeRegistryManager biomeRegistryManager;
	private WorldLoader worldLoader;
    private long lastPregeneratorStatusUpdateTime = System.currentTimeMillis();   
	
    public ForgeEngine()
    {
        super(new ForgeLogger());
        
        this.worldLoader = new WorldLoader(new File(Loader.instance().getConfigDir(), "OpenTerrainGenerator"));      
        this.biomeRegistryManager = new ForgeBiomeRegistryManager();
    }

    public WorldLoader getWorldLoader()
    {
    	return worldLoader;
    }

    public ForgeBiomeRegistryManager getBiomeRegistryManager()
    {
    	return biomeRegistryManager;
    }
   
    public void onSave(World world)
    {
    	//OTG.log(LogMarker.INFO, "ForgeEngine onSave");
    	ForgeWorld forgeWorld = (ForgeWorld) this.worldLoader.getWorld(world);
    	if(forgeWorld != null && forgeWorld.getObjectSpawner().saveRequired && !forgeWorld.getWorldSession().getPreGeneratorIsRunning())
    	{
    		forgeWorld.getStructureCache().saveToDisk();
    	}
    }
	
    // Pregenerator
    
    public void processPregeneratorTick()
    {
    	for(LocalWorld world : getAllWorlds())
    	{
    		((ForgeWorldSession)world.getWorldSession()).getPregenerator().processTick();
    	}
    	
    	if(System.currentTimeMillis() - lastPregeneratorStatusUpdateTime  > 1000l)
    	{
    		lastPregeneratorStatusUpdateTime = System.currentTimeMillis();
        	ServerPacketManager.sendPregeneratorStatusPacketToAllPlayers(FMLCommonHandler.instance().getMinecraftServerInstance());
    	}
    }
    
    // OTG dirs
    
    @Override
    public File getOTGRootFolder()
    {
        return this.worldLoader.getConfigsFolder();
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
        
    // Worlds
	
    @Override
    public LocalWorld getWorld(String name)
    {
        return this.worldLoader.getWorld(name);
    }

    @Override
    public LocalWorld getUnloadedWorld(String name)
    {
    	return this.worldLoader.getUnloadedWorld(name);
    }

    public ArrayList<ForgeWorld> getUnloadedWorlds()
    {
    	return this.worldLoader.getUnloadedWorlds();
    }

    @Override
    public ArrayList<LocalWorld> getAllWorlds()
    {
    	return this.worldLoader.getAllWorlds();
    }
    
    public ForgeWorld getOverWorld()
    {
    	return this.worldLoader.getOverWorld();
    }
    
    public ForgeWorld getWorld(World world)
    {
    	return this.worldLoader.getWorld(world);
    }
    
    public ForgeWorld getWorldByDimId(int dimensionId)
    {
    	return this.worldLoader.getWorldByDimId(dimensionId);
    }
    
    public ForgeWorld getUnloadedWorldByDimId(int dimensionId)
    {
    	return this.worldLoader.getUnloadedWorldByDimId(dimensionId);
    }
    
	// Presets
	
	@Override
	public String getPresetName(String worldName)
	{
		// If this dim's name is the same as the preset worldname then this is an OTG overworld
		if(worldName.equals("overworld") || worldName.equals(OTG.getDimensionsConfig().WorldName))
    	{
    		return OTG.getDimensionsConfig().Overworld.PresetName;	
    	} else {
    		// If this is an OTG dim other than the overworld then the world name will always match the preset name
    		return worldName;
    	}
	}	
	
	// Material
	
    @Override
    public LocalMaterialData readMaterial(String input) throws InvalidConfigException
    {
        return ForgeMaterialData.ofString(input);
    }

    @Override
    public LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return ForgeMaterialData.ofDefaultMaterial(defaultMaterial, blockData);
    }	
	
	// Events
	
	@Override
	public boolean fireReplaceBiomeBlocksEvent(int x, int z, ChunkBuffer chunkBuffer,
			LocalWorld localWorld)
	{
        return ForgeEventFactory.onReplaceBiomeBlocks(((ForgeWorld)localWorld).getChunkGenerator(), x, z, ((ForgeChunkBuffer)chunkBuffer).getChunkPrimer(), ((ForgeWorld)localWorld).getWorld());
	}

	@Override
	public boolean isModLoaded(String mod)
	{
		return Loader.isModLoaded(mod);
	}

	@Override
	public boolean areEnoughBiomeIdsAvailableForPresets(ArrayList<String> presetNames)
	{
		if(presetNames == null)
		{
			return true;
		}
		int requiredBiomesCount = 0;
		for(String presetName : presetNames)
		{
			if(OTG.getDimensionsConfig() == null || OTG.getDimensionsConfig().getDimensionConfig(presetName) == null)
			{
				File settingsDir = new File(OTG.getEngine().getWorldsDirectory() + File.separator + presetName);
				requiredBiomesCount += getBiomeIdsRequiredCount(settingsDir);
			}
		}
		if(requiredBiomesCount > getBiomeRegistryManager().getAvailableBiomeIdsCount())
		{
			return false;
		}
		return true;
	}
	
    public int getBiomeIdsRequiredCount(File settingsDir)
    {
    	WorldConfig worldConfig = WorldConfig.loadWorldConfigFromDisk(settingsDir);
    	
        // Establish folders
        List<File> biomeDirs = new ArrayList<File>(2);
        // OpenTerrainGenerator/Presets/<WorldName>/<WorldBiomes/
        biomeDirs.add(new File(settingsDir, OTG.correctOldBiomeConfigFolder(settingsDir)));
        // OpenTerrainGenerator/GlobalBiomes/
        biomeDirs.add(new File(OTG.getEngine().getOTGRootFolder(), PluginStandardValues.BiomeConfigDirectoryName));

        // Build a set of all biomes to load
        Collection<BiomeLoadInstruction> biomesToLoad = new HashSet<BiomeLoadInstruction>();

        // Load all files
        BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder(OTG.getPluginConfig().biomeConfigExtension);
        Map<String, BiomeConfigStub> biomeConfigStubs = biomeConfigFinder.findBiomes(worldConfig, worldConfig.worldHeightScale, biomeDirs, biomesToLoad);
        
        // Read all settings
        Map<String, BiomeConfig> loadedBiomes = ServerConfigProvider.readAndWriteSettings(worldConfig, biomeConfigStubs, false);
    	
        // Get the amount of biome ids required for this world
    	int customBiomeIdsRequired = 0;
    	for(BiomeConfig biomeConfig : loadedBiomes.values())
    	{
    		if(biomeConfig.replaceToBiomeName == null || biomeConfig.replaceToBiomeName.trim().length() == 0)
    		{
    			// This is a custom biome, not a virtual biome.
    			customBiomeIdsRequired++;
    		}
    	}
    	return customBiomeIdsRequired;
    }
	
    @Override
    public Collection<BiomeLoadInstruction> getDefaultBiomes()
    {
    	return ForgeBiomeRegistryManager.getDefaultBiomes();
    }
    

    /**
     * Used by mob inheritance code. Used to inherit default mob spawning settings (including those added by other mods)
     * @param biomeConfigStub
     */
    @Override
	public void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{
    	ForgeBiomeRegistryManager.mergeVanillaBiomeMobSpawnSettings(biomeConfigStub, biomeResourceLocation);
	}
}