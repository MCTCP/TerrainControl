package com.khorn.terraincontrol.forge;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

public class ForgeEngine extends TerrainControlEngine
{
	// OTG+
	
    public void onSave(World world)
    {
    	//TerrainControl.log(LogMarker.INFO, "ForgeEngine onSave");    	
    	ForgeWorld forgeWorld = (ForgeWorld) getWorld(world);
    	if(forgeWorld != null && forgeWorld.getObjectSpawner().saveRequired && !forgeWorld.GetWorldSession().getPreGeneratorIsRunning())
    	{
    		forgeWorld.getStructureCache().SaveToDisk();
    	}
    }
    
    public void ProcessPregeneratorTick()
    {
    	for(LocalWorld world : getAllWorlds())
    	{
    		((ForgeWorldSession)world.GetWorldSession()).getPregenerator().ProcessTick();
    	}
    }
    
	//
    
	protected WorldLoader worldLoader;

    protected Map<ResourceLocation, Biome> biomeMap;

    public ForgeEngine(WorldLoader worldLoader)
    {
        super(new ForgeLogger());
        this.worldLoader = worldLoader;
    }

    // Used to bypass Forge's API in order to properly register a virtual biome
    // that would otherwise be blocked by Forge due to virtual biome ID's
    // surpassing 255.
    public void registerForgeBiome(int id, ResourceLocation resourceLocation, Biome biome)
    {
    	TerrainControl.log(LogMarker.TRACE, "Registering biome " + resourceLocation.toString());
    	
        Biome.REGISTRY.registryObjects.put(resourceLocation, biome);
        Biome.REGISTRY.underlyingIntegerMap.put(biome, id);
        Biome.REGISTRY.inverseObjectRegistry.put(biome, resourceLocation);
    }
    
    public void unRegisterForgeBiome(ResourceLocation resourceLocation)
    {    	    
		TerrainControl.log(LogMarker.TRACE, "Unregistering biome " + resourceLocation.toString());
    	
    	Biome biome = Biome.REGISTRY.registryObjects.get(resourceLocation);
    	
		BitSet biomeRegistryAvailabiltyMap = ((ForgeEngine)TerrainControl.getEngine()).worldLoader.getBiomeRegistryAvailabiltyMap();
		try
		{
			int biomeId = Biome.getIdForBiome(biome);
			// If this biome uses replaceToBiomeName and has an id > 255 then it is not actually registered in the biome id 
			// registry and biomeId will be 0. Check if biomeId is actually registered to this biome.
			if(Biome.getBiomeForId(biomeId) == biome)
			{
				biomeRegistryAvailabiltyMap.set(biomeId, false); // This should be enough to make Forge re-use the biome id
			}
		}
		catch(IndexOutOfBoundsException ex)
		{
			// This can happen when:
			// A. The dimension was unloaded automatically because noone was in it and then the world was unloaded because the server shut down.
			// B. The dimensions was unloaded automatically because noone was in it and then deleted and recreated.
			
			// This can happen when a world was deleted and recreated and the index was set as "can be re-used" but when re-registering the biomes
			// it wasn't set back to "used" because it looked like the biome registry already had the biome properly registered.
			
			TerrainControl.log(LogMarker.ERROR, "Could not unregister " + biome.getBiomeName());
			throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
			
			//biomeRegistryAvailabiltyMap.set(localBiome.getIds().getSavedId(), false); // This should be enough to make Forge re-use the biome id
		}
        
		int biomeId = Biome.REGISTRY.getIDForObject(biome);	
        Biome.REGISTRY.registryObjects.remove(resourceLocation);
        
		// If this biome uses replaceToBiomeName and has an id > 255 then it is not actually registered in the biome id 
		// registry and biomeId will be 0. Check if biomeId is actually registered to this biome.
		if(Biome.REGISTRY.getObjectById(biomeId) == biome)
		{
			Biome.REGISTRY.underlyingIntegerMap.put(null, biomeId);
		}
        
        Biome.REGISTRY.inverseObjectRegistry.remove(biome);
    }

    public WorldLoader getWorldLoader()
    {
    	return worldLoader;
    }   
    
    public boolean getCartographerEnabled()
    {   	
    	ForgeWorld world = getOverWorld(); // If overworld is null then the overworld is not an OTG world
    	return world == null ? false : world.getConfigs().getWorldConfig().Cartographer;
    }
    
    public ForgeWorld getOverWorld()
    {
		ArrayList<LocalWorld> allWorlds = getAllWorlds();
		for(LocalWorld world : allWorlds)
		{
			if(((ForgeWorld)world).getWorld() != null && ((ForgeWorld)world).getWorld().provider != null && ((ForgeWorld)world).getWorld().provider.getDimension() == 0)
			{
				return (ForgeWorld)world;
			}
		}
		return null;
    }
    
    public ForgeWorld getWorldByDimId(int dimensionId)
    {
    	ForgeWorld forgeWorld;
    	if(dimensionId == 0)
    	{
    		forgeWorld = ((ForgeEngine)TerrainControl.getEngine()).getOverWorld();
    	} else {
        	DimensionType dimType = DimensionManager.getProviderType(dimensionId);
    		forgeWorld = (ForgeWorld)TerrainControl.getWorld(dimType.getName());
    	}
    	return forgeWorld;
    }
    
    public LocalWorld getWorld(World world)
    {
    	if(world.provider.getDimension() > 1)
    	{
			if(world.provider.getDimensionType().getSuffix() != null && world.provider.getDimensionType().getSuffix().equals("OTG"))
	    	{
				LocalWorld localWorld = this.worldLoader.getWorld(world.provider.getDimensionType().getName());
				if(localWorld == null)
				{
					return this.worldLoader.getUnloadedWorld(world.provider.getDimensionType().getName());
				}
	    		return this.worldLoader.getWorld(world.provider.getDimensionType().getName());
	    	}
    	}
    	LocalWorld localWorld = this.worldLoader.getWorld(world.getWorldInfo().getWorldName());
		if(localWorld == null)
		{
			return this.worldLoader.getUnloadedWorld(world.getWorldInfo().getWorldName());
		}
    			
        return localWorld;
    }
        
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

    @Override
    public File getTCDataFolder()
    {
        return this.worldLoader.getConfigsFolder();
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(this.getTCDataFolder(), PluginStandardValues.BO_DirectoryName);
    }

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
}
