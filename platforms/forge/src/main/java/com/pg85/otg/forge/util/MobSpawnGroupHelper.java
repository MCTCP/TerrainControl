package com.pg85.otg.forge.util;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.biome.settings.WeightedMobSpawnGroup;
import com.pg85.otg.configuration.standard.MojangSettings.EntityCategory;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.fml.common.registry.EntityEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Methods for conversion between mob lists in Minecraft and in the plugin.
 *
 */
public final class MobSpawnGroupHelper
{
    private static HashMap<String, Class<? extends Entity>> FoundEntitiesByClassName = new HashMap<String, Class<? extends Entity>>();
    private static HashMap<String, Class<? extends Entity>> FoundEntitiesByName = new HashMap<String, Class<? extends Entity>>();
	
    /**
     * Transforms our MobType into Minecraft's EnumCreatureType.
     * @param type Our type.
     * @return Minecraft's type.
     */
    private static EnumCreatureType toEnumCreatureType(EntityCategory type)
    {
        switch (type)
        {
            case MONSTER:
                return EnumCreatureType.MONSTER;
            case CREATURE:
                return EnumCreatureType.CREATURE;
            case AMBIENT_CREATURE:
                return EnumCreatureType.AMBIENT;
            case WATER_CREATURE:
                return EnumCreatureType.WATER_CREATURE;
        }
        throw new AssertionError("Unknown mob type: " + type);
    }

    /**
     * Transforms a single Minecraft BiomeMeta into our type.
     * @param biomeMeta Minecraft's type.
     * @return Our type.
     */
    private static WeightedMobSpawnGroup fromMinecraftGroup(Biome.SpawnListEntry biomeMeta)
    {
    	String mobName = stringFromMinecraftClass(biomeMeta.entityClass);
    	if(mobName == null)
    	{
    		return null;
    	}
        return new WeightedMobSpawnGroup(mobName, biomeMeta.itemWeight, biomeMeta.minGroupCount, biomeMeta.maxGroupCount);
    }

    /**
     * Gets the spawn list of the given biome for the given category.
     * @param biome The biome.
     * @param type  The category.
     * @return The spawn list for the given category.
     */
    public static List<WeightedMobSpawnGroup> getListFromMinecraftBiome(Biome biome, EntityCategory type)
    {
        Collection<SpawnListEntry> mobList = biome.getSpawnableList(toEnumCreatureType(type));
        return fromMinecraftList(mobList);
    }

    /**
     * Converts a BiomeMeta collection to a WeightedMobSpawnGroup list. This
     * method is the inverse of {@link #toMinecraftlist(Collection)}.
     * @param biomeMetas The BiomeMeta collection.
     * @return The WeightedMobSpawnGroup list.
     */
    private static List<WeightedMobSpawnGroup> fromMinecraftList(Collection<SpawnListEntry> biomeMetas)
    {
        List<WeightedMobSpawnGroup> result = new ArrayList<WeightedMobSpawnGroup>();
        for (SpawnListEntry meta : biomeMetas)
        {
        	WeightedMobSpawnGroup wMSG = fromMinecraftGroup(meta);
        	if(wMSG != null)
        	{
        		result.add(wMSG);	
        	}            
        }
        return result;
    }

    /**
     * Converts a WeightedMobSpawnGroup collection to a BiomeMeta collection.
     * This method is the inverse of {@link #fromMinecraftList(Collection)}.
     * @param weightedMobSpawnGroups The WeighedMobSpawnGroup collection.
     * @return The BiomeMeta list.
     */
    public static List<SpawnListEntry> toMinecraftlist(Collection<WeightedMobSpawnGroup> weightedMobSpawnGroups)
    {
        List<SpawnListEntry> biomeList = new ArrayList<SpawnListEntry>();
        for (WeightedMobSpawnGroup mobGroup : weightedMobSpawnGroups)
        {
            Class<? extends Entity> entityClass = toMinecraftClass(mobGroup.getInternalName());
            
            if (entityClass != null && EntityLiving.class.isAssignableFrom(entityClass))
            {
                biomeList.add(new SpawnListEntry((Class<? extends EntityLiving>) entityClass, mobGroup.getWeight(), mobGroup.getMin(), mobGroup.getMax()));
            } else {
            	if(OTG.getPluginConfig().spawnLog)
            	{
	            	entityClass = getEntityByClassName(mobGroup.getInternalName());	            
	            	if(entityClass == null)
	            	{
	            		OTG.log(LogMarker.WARN, "Mob type {} not found", mobGroup.getInternalName());
	            	}
            	}
            }
        }
        return biomeList;
    }
    
    private static Class<? extends Entity> getEntityByClassName(String mobClassName)
    {
    	mobClassName = mobClassName.toLowerCase().trim().replace("entity","").replace("_","").replace(" ","");
    	Class<? extends Entity> entity = FoundEntitiesByClassName.get(mobClassName);
    	if(entity == null)
    	{
	    	List<EntityEntry> entityClasses = net.minecraftforge.fml.common.registry.ForgeRegistries.ENTITIES.getValues();
	    	for(EntityEntry entityClass : entityClasses)
	    	{
	    		String entityName = entityClass.getEntityClass().getSimpleName();
	    		if(entityName.toLowerCase().trim().replace("entity","").replace("_","").replace(" ","").equals(mobClassName))
	    		{
	    			entity = (Class<? extends Entity>) entityClass.getEntityClass();
	    			FoundEntitiesByClassName.put(mobClassName, entity);
	    			break;
	    		}
	    	}
    	}
    	return entity;
    }

    /**
     * Gets the entity class corresponding to the given entity name. This
     * method is the inverse of {@link #fromMinecraftClass(Class)}.
     * @param mobName The mob name.
     * @return The entity class, or null if not found.
     */
	public static Class<? extends Entity> toMinecraftClass(String entityName)
    {
    	ResourceLocation resourceLocation = new ResourceLocation(entityName);
    	entityName = entityName.toLowerCase().trim().replace("entity","").replace("_","").replace(" ","");
    	Class<? extends Entity> entity = FoundEntitiesByName.get(entityName);
    	if(entity == null)
    	{    		
	    	Set<ResourceLocation> mobNames = EntityList.getEntityNameList();
	    	for(ResourceLocation mobName1 : mobNames)
	    	{
	    		if(mobName1.equals(resourceLocation))
	    		{
	    			entity = (Class<? extends Entity>) EntityList.getClass(mobName1);
	    			FoundEntitiesByName.put(entityName, entity);
	    			break;
	    		}
	    	}
	    	if(entity == null)
	    	{
		    	for(ResourceLocation mobName1 : mobNames)
		    	{
		    		if(mobName1.getResourcePath().toLowerCase().trim().replace("entity","").replace("_","").replace(" ","").equals(entityName))
		    		{
		    			entity = (Class<? extends Entity>) EntityList.getClass(mobName1);
		    			FoundEntitiesByName.put(entityName, entity);
		    			break;
		    		}
		    	}
	    	}
    	}
    	
    	if(entity == null)
    	{
    		entity = getEntityByClassName(entityName);
    		if(entity != null)
    		{
    			FoundEntitiesByName.put(entityName, entity);
    		}
    	}
    	
    	return entity;
    }

    /**
     * Gets the entity name corresponding to the given entity class.
     * @param entityClass The entity class.
     * @return The entity name, or null if not found.
     */
    private static String stringFromMinecraftClass(Class<? extends Entity> entityClass)
    {
    	ResourceLocation mobName = EntityList.getKey(entityClass);
    	if(mobName != null)
    	{
    		return mobName.getResourcePath();
    	}

    	if(OTG.getPluginConfig().spawnLog)
    	{
    		OTG.log(LogMarker.DEBUG, "No EntityRegistry entry found for class: " + entityClass);
    	}
        return null;
    }
    
    /**
     * Gets the entity name corresponding to the given entity class.
     * @param entityClass The entity class.
     * @return The entity name, or null if not found.
     */
    public static ResourceLocation resourceLocationFromMinecraftClass(Class<? extends Entity> entityClass)
    {
    	ResourceLocation mobName = EntityList.getKey(entityClass);
    	if(mobName != null)
    	{
    		return mobName;
    	}

    	if(OTG.getPluginConfig().spawnLog)
    	{
    		OTG.log(LogMarker.WARN, "No EntityRegistry entry found for class: " + entityClass);
    	}
        return null;
    }
}
