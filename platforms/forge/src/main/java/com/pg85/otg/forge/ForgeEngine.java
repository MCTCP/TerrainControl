package com.pg85.otg.forge;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.io.FileSettingsReader;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.forge.biomes.ForgeBiomeRegistryManager;
import com.pg85.otg.forge.generator.ForgeChunkBuffer;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.forge.util.ForgeLogger;
import com.pg85.otg.forge.util.ForgeMaterialData;
import com.pg85.otg.forge.world.ForgeWorldSession;
import com.pg85.otg.forge.world.WorldLoader;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

public class ForgeEngine extends OTGEngine
{
	public static LinkedHashMap<String, DimensionConfigGui> Presets = new LinkedHashMap<String, DimensionConfigGui>();

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
    
    // Configs
    
	public WorldConfig loadWorldConfigFromDisk(File worldDir)
	{
        File worldConfigFile = new File(worldDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        if(!worldConfigFile.exists())
        {
        	return null;
        }
        SettingsMap settingsMap = FileSettingsReader.read(worldDir.getName(), worldConfigFile);
        return new WorldConfig(worldDir, settingsMap, null, null);
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

	public static void loadPresets()
	{
		Presets.clear();
		
	    ArrayList<String> worldNames = new ArrayList<String>();
	    File OTGWorldsDirectory = new File(OTG.getEngine().getOTGRootFolder().getAbsolutePath() + File.separator + PluginStandardValues.PresetsDirectoryName);
	    if(OTGWorldsDirectory.exists() && OTGWorldsDirectory.isDirectory())
	    {
	    	for(File worldDir : OTGWorldsDirectory.listFiles())
	    	{
	    		if(worldDir.isDirectory() && !worldDir.getName().toLowerCase().trim().startsWith("dim-"))
	    		{
	    			for(File file : worldDir.listFiles())
	    			{
	    				if(file.getName().equals("WorldConfig.ini"))
	    				{
			    			worldNames.add(worldDir.getName());
			    			WorldConfig worldConfig = ((ForgeEngine)OTG.getEngine()).loadWorldConfigFromDisk(worldDir);
					        Presets.put(worldDir.getName(), new DimensionConfigGui(worldDir.getName(), worldConfig));
					        break;
	    				}
	    			}
	    		}
	    	}
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
}