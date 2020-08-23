package com.pg85.otg.forge.asm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.asm.excluded.IOTGASMBiome;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;

import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.DimensionManager;

public class OTGHooks
{	
	static boolean checkedOTGInstall = false;
	static boolean otgIsInstalled = false;
	private static boolean isOTGInstalled()
	{
		if(!checkedOTGInstall)
		{
			checkedOTGInstall = true;
			Object isOTGInstalled = "NoClassDefFoundError";
			try
			{
				if(isOTGInstalled instanceof OTGWorldProvider)
				{

				}
				otgIsInstalled = true;
			}
			catch(NoClassDefFoundError ex)
			{
				// OTG isn't present.
			}
		}
		return otgIsInstalled;
	}
	
	// Make sure that OTG dimensions get initialised by OTGDimensionManager.initDimension
	// Used when a player joins the server and spawns in an unloaded dim.
	// Causes problems for Sponge apparently. <- initOTGDimension should be used only on server load now, hopefully fixes things?
	public static boolean initOTGDimension(int i)
	{
		if(isOTGInstalled() && DimensionManager.isDimensionRegistered(i))
		{
			if(OTGDimensionManager.IsOTGDimension(i))
			{
				OTGDimensionManager.initDimension(i);
				return true;
			}
		}
		return false;
	}
	
	public static int getIDForObject(Biome biome)
	{
		if(isOTGInstalled() && biome instanceof IOTGASMBiome)
		{
			return ((IOTGASMBiome)biome).getSavedId();
		}
		return Biome.getIdForBiome(biome);
	}
	
	public static double getGravityFactor(Entity entity)
	{
		if(isOTGInstalled())
		{
			if(entity.world.provider instanceof OTGWorldProvider)
			{
				return ((OTGWorldProvider)entity.world.provider).getGravityFactor();
			} else {
				return 0.08D;
			}
		}
		return 0.08D;
	}
	
	public static boolean isOTGWorld(Entity entity)
	{
		return isOTGInstalled() && entity.getEntityWorld().provider instanceof OTGWorldProvider;
	}

	public static double getGravityFactorMineCart(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return 0.03999999910593033D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return 0.03999999910593033D;
		}
	}

	public static double getGravityFactorArrow(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return 0.05000000074505806D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return 0.05000000074505806D;
		}
	}

	public static double getGravityFactorBoat(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return -0.03999999910593033D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return -0.03999999910593033D;
		}
	}

	public static double getGravityFactorFallingBlock(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return 0.03999999910593033D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return 0.03999999910593033D;
		}
	}

	public static double getGravityFactorItem(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return 0.03999999910593033D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return 0.03999999910593033D;
		}
	}

	public static double getGravityFactorLlamaSpit(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return 0.05999999865889549D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return 0.05999999865889549D;
		}
	}

	public static double getGravityFactorShulkerBullet(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return 0.04D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return 0.04D;
		}
	}

	public static float getGravityFactorThrowable(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return (float)(0.03F * (double)(gravityFactor / baseGravityFactor));
		} else {
			return 0.03F;
		}
	}

	public static double getGravityFactorTNTPrimed(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return 0.03999999910593033D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return 0.03999999910593033D;
		}
	}

	public static double getGravityFactorXPOrb(Entity entity)
	{
		if(isOTGInstalled() && entity.world.provider instanceof OTGWorldProvider)
		{
	    	double baseGravityFactor = WorldStandardValues.GravityFactor.getDefaultValue();
	    	double gravityFactor = ((OTGWorldProvider)entity.world.provider).getGravityFactor();
	    	return 0.029999999329447746D * (double)(gravityFactor / baseGravityFactor);
		} else {
			return 0.029999999329447746D;
		}
	}

	// When registering dimensions during a session, a client may not have the required biomes registered when trying to load a dimension.
	// TODO: Is that the only time this is needed? Check if Forge also throws the missing registries error when clients are connecting, 
	// the server should be able to send all the biome data itself at that point, since everything should be registered.
	public static int countMissingRegistryEntries(LinkedHashMap<ResourceLocation, Map<ResourceLocation, Integer>> missing)
	{
		// Exclude OTG Biomes.
		if(isOTGInstalled() && missing.containsKey(new ResourceLocation("minecraft", "biomes")))
		{
			Gson gson = new Gson();
			ArrayList<ResourceLocation> biomesToRemove = new ArrayList<ResourceLocation>();
			Map<ResourceLocation, Integer> biomes = missing.get(new ResourceLocation("minecraft", "biomes"));
			for(ResourceLocation biomeResourceLocation : biomes.keySet())
			{
				if(biomeResourceLocation != null)
				{
					// Can't use biomeResourceLocation.getResourceDomain()
					String jsonInString = gson.toJson(biomeResourceLocation);
					if(jsonInString.contains(":\"openterraingenerator\","))
					{
						biomesToRemove.add(biomeResourceLocation);
					}
				}
			}
			for(ResourceLocation biomeResourceLocation : biomesToRemove)
			{
				biomes.remove(biomeResourceLocation);
			}
		}

		int count = missing.values().stream().mapToInt(Map::size).sum();//) - otgBiomesCount;

		if(count > 0)
		{
			System.out.println("Items/Blocks/Biomes appear to be missing from the registry. Forge will show an error message about this and you will not be able to join the world. Forge will display a list of missing registry entries, you can ignore any otg biomes on that list as they do not actually need to be registered. The missing registries error is caused by other mods, not OTG.");
		}
		return count;
	}

	public static WorldBorder getWorldBorder(WorldBorder worldBorder, WorldServer worldIn)
	{
		if(isOTGInstalled() && worldIn.provider instanceof OTGWorldProvider)
		{
			return worldIn.worldBorder;
		}
		return worldBorder;
	}
    
    public static void setPlayerManager(WorldServer[] worldServers)
    {
        worldServers[0].getWorldBorder().addListener(new IBorderListener()
        {
            public void onSizeChanged(WorldBorder border, double newSize)
            {
            	for(int dim : DimensionManager.getIDs())
            	{
            		if(dim == 0 || !OTGDimensionManager.IsOTGDimension(dim))
            		{      
            			worldServers[0].getMinecraftServer().getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_SIZE), dim);            			
            		}
            	}
            }
            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time)
            {
            	for(int dim : DimensionManager.getIDs())
            	{
            		if(dim == 0 || !OTGDimensionManager.IsOTGDimension(dim))
            		{
            			worldServers[0].getMinecraftServer().getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.LERP_SIZE), dim);            			
            		}
            	}
            }
            public void onCenterChanged(WorldBorder border, double x, double z)
            {
            	for(int dim : DimensionManager.getIDs())
            	{
            		if(dim == 0 || !OTGDimensionManager.IsOTGDimension(dim))
            		{
            			worldServers[0].getMinecraftServer().getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_CENTER), dim);            			
            		}
            	}
            }
            public void onWarningTimeChanged(WorldBorder border, int newTime)
            {
            	for(int dim : DimensionManager.getIDs())
            	{
            		if(dim == 0 || !OTGDimensionManager.IsOTGDimension(dim))
            		{
            			worldServers[0].getMinecraftServer().getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_TIME), dim);            			
            		}
            	}            	
            }
            public void onWarningDistanceChanged(WorldBorder border, int newDistance)
            {
            	for(int dim : DimensionManager.getIDs())
            	{
            		if(dim == 0 || !OTGDimensionManager.IsOTGDimension(dim))
            		{
            			worldServers[0].getMinecraftServer().getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_BLOCKS), dim);            			
            		}
            	}              	
            }
            public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
            }
            public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
            }
        });
    }
}
