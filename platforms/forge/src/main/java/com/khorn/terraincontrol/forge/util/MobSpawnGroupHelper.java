package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.MojangSettings.EntityCategory;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Methods for conversion between mob lists in Minecraft and in the plugin.
 *
 */
public final class MobSpawnGroupHelper
{
    private static final Map<Class<? extends Entity>, String> CLASS_TO_NAME_MAP;
    private static final Map<String, Class<? extends Entity>> NAME_TO_CLASS_MAP;

    static
    {
        @SuppressWarnings("unchecked")
        Map<Class<? extends Entity>, String> classToNameMap = (Map<Class<? extends Entity>, String>) EntityList.classToStringMapping;
        CLASS_TO_NAME_MAP = classToNameMap;

        @SuppressWarnings("unchecked")
        Map<String, Class<? extends Entity>> nameToClassMap = (Map<String, Class<? extends Entity>>) EntityList.stringToClassMapping;;
        NAME_TO_CLASS_MAP = nameToClassMap;
    }

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
                return EnumCreatureType.monster;
            case CREATURE:
                return EnumCreatureType.creature;
            case AMBIENT_CREATURE:
                return EnumCreatureType.ambient;
            case WATER_CREATURE:
                return EnumCreatureType.waterCreature;
        }
        throw new AssertionError("Unknown mob type: " + type);
    }

    /**
     * Transforms a single Minecraft BiomeMeta into our type.
     * @param biomeMeta Minecraft's type.
     * @return Our type.
     */
    private static WeightedMobSpawnGroup fromMinecraftGroup(BiomeGenBase.SpawnListEntry biomeMeta)
    {
        return new WeightedMobSpawnGroup(fromMinecraftClass(biomeMeta.entityClass), biomeMeta.itemWeight, biomeMeta.minGroupCount, biomeMeta.maxGroupCount);
    }

    /**
     * Gets the spawn list of the given biome for the given category.
     * @param biome The biome.
     * @param type  The category.
     * @return The spawn list for the given category.
     */
    public static List<WeightedMobSpawnGroup> getListFromMinecraftBiome(BiomeGenBase biome, EntityCategory type)
    {
        @SuppressWarnings("unchecked")
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
            result.add(fromMinecraftGroup(meta));
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
            if (entityClass != null)
            {
                biomeList.add(new SpawnListEntry(entityClass, mobGroup.getWeight(), mobGroup.getMin(), mobGroup.getMax()));
            } else
            {
                // The .toLowerCase() is just a safeguard so that we get
                // notified if this.af is no longer the biome name
                TerrainControl.log(LogMarker.WARN, "Mob type {} not found",
                        mobGroup.getInternalName());
            }
        }
        return biomeList;
    }
    
    /**
     * Gets the entity class corresponding to the given entity name. This
     * method is the inverse of {@link #fromMinecraftClass(Class)}.
     * @param mobName The mob name.
     * @return The entity class, or null if not found.
     */
    static Class<? extends Entity> toMinecraftClass(String mobName)
    {
        return NAME_TO_CLASS_MAP.get(mobName);
    }

    /**
     * Gets the entity name corresponding to the given entity class.
     * @param entityClass The entity class.
     * @return The entity name, or null if not found.
     */
    private static String fromMinecraftClass(Class<?> entityClass)
    {
        return CLASS_TO_NAME_MAP.get(entityClass);
    }
}
