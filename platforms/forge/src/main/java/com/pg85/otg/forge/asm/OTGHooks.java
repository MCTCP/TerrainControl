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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
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
	// TODO: Isn't OTGDimensionManager.initDimension called in all cases already, does this really need to be core-modded?
	// Causes problems for Sponge apparently.	
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
}
