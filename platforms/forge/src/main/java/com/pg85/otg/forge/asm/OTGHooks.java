package com.pg85.otg.forge.asm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.asm.excluded.IOTGASMBiome;
import com.pg85.otg.forge.dimensions.OTGWorldProvider;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

public class OTGHooks
{	
	public static int getIDForObject(Biome biome)
	{
		if(biome instanceof IOTGASMBiome)
		{
			return ((IOTGASMBiome)biome).getSavedId();
		}
		return Biome.getIdForBiome(biome);
	}
	
	public static double getGravityFactor(Entity entity)
	{
		if(entity.world.provider instanceof OTGWorldProvider)
		{
			return ((OTGWorldProvider)entity.world.provider).getGravityFactor();
		} else {
			return 0.08D;
		}
	}
	
	public static boolean isOTGWorld(Entity entity)
	{
		return entity.getEntityWorld().provider instanceof OTGWorldProvider;
	}

	public static double getGravityFactorMineCart(Entity entity)
	{
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(entity.world.provider instanceof OTGWorldProvider)
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
		if(missing.containsKey(new ResourceLocation("minecraft", "biomes")))
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
					} else {
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
