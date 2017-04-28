package com.khorn.terraincontrol.forge;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.forge.generator.Pregenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ForgeEngine extends TerrainControlEngine
{
	public int WorldBorderRadius;
	
	protected Pregenerator pregenerator;

	protected WorldLoader worldLoader;

    protected Map<ResourceLocation, Biome> biomeMap;

    public ForgeEngine(WorldLoader worldLoader)
    {
        super(new ForgeLogger());
        this.worldLoader = worldLoader;
        pregenerator = new Pregenerator();
    }

    // Used to bypass Forge's API in order to properly register a virtual biome
    // that would otherwise be blocked by Forge due to virtual biome ID's
    // surpassing 255.
    public void registerForgeBiome(int id, ResourceLocation resourceLocation, Biome biome)
    {
        Biome.REGISTRY.registryObjects.put(resourceLocation, biome);
        Biome.REGISTRY.underlyingIntegerMap.put(biome, id);
        Biome.REGISTRY.inverseObjectRegistry.put(biome, resourceLocation);
    }
    
    public void unRegisterForgeBiome(ResourceLocation resourceLocation)
    {
		TerrainControl.log(LogMarker.DEBUG, "Unregistering biome " + resourceLocation.toString());
    	
    	Biome biome = Biome.REGISTRY.registryObjects.get(resourceLocation);
    	
		BitSet biomeRegistryAvailabiltyMap = ((ForgeEngine)TerrainControl.getEngine()).worldLoader.getBiomeRegistryAvailabiltyMap();
		try
		{
			biomeRegistryAvailabiltyMap.set(Biome.getIdForBiome(biome), false); // This should be enough to make Forge re-use the biome id
		}
		catch(IndexOutOfBoundsException ex)
		{
			// This can happen when:
			// A. The dimension was unloaded automatically because noone was in it and then the world was unloaded because the server shut down.
			// B. The dimensions was unloaded automatically because noone was in it and then deleted and recreated.
			
			// This can happen when a world was deleted and recreated and the index was set as "can be re-used" but when re-registering the biomes
			// it wasn't set back to "used" because it looked like the biome registry already had the biome properly registered.
			
			TerrainControl.log(LogMarker.ERROR, "Could not unregister " + biome.getBiomeName());
			throw new NotImplementedException();
			
			//biomeRegistryAvailabiltyMap.set(localBiome.getIds().getSavedId(), false); // This should be enough to make Forge re-use the biome id
		}
        
        Biome.REGISTRY.registryObjects.remove(resourceLocation);
        Biome.REGISTRY.underlyingIntegerMap.put(null, Biome.REGISTRY.getIDForObject(biome));
        Biome.REGISTRY.inverseObjectRegistry.remove(biome);
    }

    public WorldLoader getWorldLoader()
    {
    	return worldLoader;
    }
    
    public Pregenerator getPregenerator()
    {
    	return pregenerator;
    }

    public boolean getCartographerEnabled()
    {   	
    	ForgeWorld world = getOverWorld(); // If overworld is null then the overworld is not an OTG world
    	return world == null ? false : world.getConfigs().getWorldConfig().Cartographer;
    }

    public boolean getDimensionsEnabled()
    { 	
    	ForgeWorld world = getOverWorld(); // If overworld is null then the overworld is not an OTG world    	
    	return world == null ? false : world.getConfigs().getWorldConfig().DimensionsEnabled;
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
    
    public LocalWorld getWorld(World world)
    {
    	if(world.provider.getDimension() > 1)
    	{
			if(world.provider.getDimensionType().getSuffix().equals("OTG"))
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
    	ArrayList<ForgeWorld> unloadedWorlds = new ArrayList();
    	unloadedWorlds.addAll(this.worldLoader.unloadedWorlds.values());
    	return unloadedWorlds;
    }
    
    @Override
    public ArrayList<LocalWorld> getAllWorlds()
    {
    	ArrayList<LocalWorld> worlds = new ArrayList<LocalWorld>();
    	worlds.addAll(this.worldLoader.worlds.values());
    	worlds.addAll(this.worldLoader.unloadedWorlds.values());
    	return worlds;
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

	@Override
	public void addPlatformSpecificDataToPacket(DataOutput stream, BiomeConfig config, boolean isSinglePlayer)
	{
        // TODO: Why exactly do all these need to be sent to the client? (Forge SP stuff?)				
		if(isSinglePlayer)
		{        	        
			try
			{
				ConfigFile.writeStringToStream(stream, WeightedMobSpawnGroup.toJson(config.spawnMonstersMerged));
				ConfigFile.writeStringToStream(stream, WeightedMobSpawnGroup.toJson(config.spawnCreaturesMerged));
				ConfigFile.writeStringToStream(stream, WeightedMobSpawnGroup.toJson(config.spawnWaterCreaturesMerged));
				ConfigFile.writeStringToStream(stream, WeightedMobSpawnGroup.toJson(config.spawnAmbientCreaturesMerged));
			}
			catch (IOException e)
			{
				e.printStackTrace();
				throw new NotImplementedException();
			}
		}
	}
}
