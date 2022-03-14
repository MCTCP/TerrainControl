package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.MojangSettings.EntityCategory;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        return new WeightedMobSpawnGroup(fromMinecraftClass(biomeMeta.entityClass), biomeMeta.itemWeight, biomeMeta.minGroupCount, biomeMeta.maxGroupCount);
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
            Class<? extends EntityLiving> entityClass = toMinecraftClass(mobGroup.getInternalName());
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
    static Class<? extends EntityLiving> toMinecraftClass(String mobName)
    {
        Class<? extends Entity> clazz = EntityList.getClass(new ResourceLocation(mobName));
        if (clazz != null && EntityLiving.class.isAssignableFrom(clazz))
        {
            return clazz.asSubclass(EntityLiving.class);
        }
        return null;
    }

    /**
     * Gets the entity name corresponding to the given entity class.
     * @param entityClass The entity class.
     * @return The entity name, or null if not found.
     */
    private static String fromMinecraftClass(Class<? extends Entity> entityClass)
    {
        EntityEntry entry = EntityRegistry.getEntry(entityClass);
        if (entry != null) {
            return entry.getName();
        }

        return null;
    }
}
