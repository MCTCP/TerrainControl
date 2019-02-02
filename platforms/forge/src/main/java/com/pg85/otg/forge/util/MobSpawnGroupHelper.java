package com.pg85.otg.forge.util;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.biome.WeightedMobSpawnGroup;
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
    	String mobName = fromMinecraftClass(biomeMeta.entityClass);
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
    static List<WeightedMobSpawnGroup> fromMinecraftList(Collection<SpawnListEntry> biomeMetas)
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
            Class<? extends EntityLiving> entityClass = toMinecraftClass(mobGroup.getInternalName());
            
            if (entityClass != null)
            {
                biomeList.add(new SpawnListEntry(entityClass, mobGroup.getWeight(), mobGroup.getMin(), mobGroup.getMax()));
            } else {
            	
            	entityClass = getEntityByClassName(mobGroup.getInternalName());
            
            	if(entityClass == null && OTG.getPluginConfig().SpawnLog)
            	{
            		OTG.log(LogMarker.WARN, "Mob type {} not found", mobGroup.getInternalName());
            	}
            }
        }
        return biomeList;
    }
    
    private static HashMap<String, Class<? extends EntityLiving>> foundEntitiesByClassName = new HashMap<String, Class<? extends EntityLiving>>();
    private static Class<? extends EntityLiving> getEntityByClassName(String mobClassName)
    {
    	Class<? extends EntityLiving> mob = foundEntitiesByClassName.get(mobClassName);
    	if(mob == null)
    	{
	    	List<EntityEntry> entityClasses = net.minecraftforge.fml.common.registry.ForgeRegistries.ENTITIES.getValues();
	    	for(EntityEntry entityClass : entityClasses)
	    	{
	    		String entityName = entityClass.getEntityClass().getSimpleName();
	    		if(entityName.toLowerCase().trim().replace("entity","").replace("_","").replace(" ","").equals(mobClassName.toLowerCase().trim().replace("entity","").replace("_","").replace(" ","")))
	    		{
	    			// TODO: This might return an non EnityLiving entity
	    			mob = (Class<? extends EntityLiving>) entityClass.getEntityClass();
	    			foundEntitiesByClassName.put(mobClassName, mob);
	    			break;
	    		}
	    	}
    	}
    	return mob;
    }

    /**
     * Gets the entity class corresponding to the given entity name. This
     * method is the inverse of {@link #fromMinecraftClass(Class)}.
     * @param mobName The mob name.
     * @return The entity class, or null if not found.
     */
    private static HashMap<String, Class<? extends EntityLiving>> foundEntitiesByName = new HashMap<String, Class<? extends EntityLiving>>();
    @SuppressWarnings("unchecked")
	public static Class<? extends EntityLiving> toMinecraftClass(String mobName)
    {
    	Class<? extends EntityLiving> mob = foundEntitiesByName.get(mobName);
    	if(mob == null)
    	{
	    	Set<ResourceLocation> mobNames = EntityList.getEntityNameList();
	    	for(ResourceLocation mobName1 : mobNames)
	    	{
	    		if(mobName1.getResourcePath().toLowerCase().trim().replace("entity","").replace("_","").replace(" ","").equals(mobName.toLowerCase().trim().replace("entity","").replace("_","").replace(" ","")))
	    		{
	    			mob = (Class<? extends EntityLiving>) EntityList.getClass(mobName1);
	    			foundEntitiesByName.put(mobName, mob);
	    			break;
	    		}
	    	}
    	}
    	
    	if(mob == null)
    	{
    		mob = getEntityByClassName(mobName);
    	}
    	
    	return mob;
    }

    /**
     * Gets the entity name corresponding to the given entity class.
     * @param entityClass The entity class.
     * @return The entity name, or null if not found.
     */
    private static String fromMinecraftClass(Class<? extends Entity> entityClass)
    {
    	ResourceLocation mobName = EntityList.getKey(entityClass);
    	if(mobName != null)
    	{
    		return mobName.getResourcePath();
    	}

		OTG.log(LogMarker.DEBUG, "No EntityRegistry entry found for class: " + entityClass);
        return null;
    }
}
