package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.util.minecraftTypes.MobNames;

import net.minecraft.server.v1_7_R1.*;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftBlock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CustomBiome extends BiomeBase
{
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public CustomBiome(BiomeIds biomeIds, String name)
    {
        super(biomeIds.getSavedId());
        this.a(name);

        // Insert the biome in CraftBukkit's biome mapping
        try
        {
            Field biomeMapping = CraftBlock.class.getDeclaredField("BIOME_MAPPING");
            biomeMapping.setAccessible(true);
            Biome[] mappingArray = (Biome[]) biomeMapping.get(null);

            mappingArray[id] = Biome.OCEAN;

        } catch (Exception e)
        {
            TerrainControl.log(Level.SEVERE, "Couldn't update Bukkit's biome mappings!");
            TerrainControl.printStackTrace(Level.SEVERE, e);
        }
    }

    @SuppressWarnings("unchecked")
    public void setEffects(BiomeConfig config)
    {
        this.am = config.BiomeHeight;
        this.an = config.BiomeVolatility;
        this.ai = ((BukkitMaterialData)config.surfaceBlock).internalBlock();
        this.ak = ((BukkitMaterialData)config.groundBlock).internalBlock();
        this.temperature = config.biomeTemperature;
        this.humidity = config.biomeWetness;
        if (this.humidity == 0)
        {
            this.b(); // this.disableRain()
        }

        // Mob spawning
        addMobs(this.as, config.spawnMonstersAddDefaults, config.spawnMonsters);
        addMobs(this.at, config.spawnCreaturesAddDefaults, config.spawnCreatures);
        addMobs(this.au, config.spawnWaterCreaturesAddDefaults, config.spawnWaterCreatures);
        addMobs(this.av, config.spawnAmbientCreaturesAddDefaults, config.spawnAmbientCreatures);
    }

    // Adds the mobs to the internal list. Displays a warning for each mob type it doesn't understand
    protected void addMobs(List<BiomeMeta> internalList, boolean addDefaults, List<WeightedMobSpawnGroup> configList)
    {
        if (!addDefaults)
        {
            internalList.clear();
        }
        for (WeightedMobSpawnGroup mobGroup : configList)
        {
            Class<? extends Entity> entityClass = getEntityClass(mobGroup);
            if (entityClass != null)
            {
                internalList.add(new BiomeMeta(entityClass, mobGroup.getWeight(), mobGroup.getMin(), mobGroup.getMax()));
            } else
            {
                // The .toLowerCase() is just a safeguard so that we get notified if this.af is no longer the biome name
                TerrainControl.log(Level.WARNING, "Mob type {0} not found in {1}", new Object[]{mobGroup.getMobName(), this.af.toLowerCase()});
            }
        }
    }

    // Gets the class of the entity.
    @SuppressWarnings("unchecked")
    protected Class<? extends Entity> getEntityClass(WeightedMobSpawnGroup mobGroup)
    {
        String mobName = MobNames.getInternalMinecraftName(mobGroup.getMobName());
        try
        {
            Field entitiesField = EntityTypes.class.getDeclaredField("c");
            entitiesField.setAccessible(true);
            Map<String, Class<? extends Entity>> entitiesList = (Map<String, Class<? extends Entity>>) entitiesField.get(null);
            return entitiesList.get(mobName);
        } catch (Exception e)
        {
            TerrainControl.log(Level.SEVERE, "Someone forgot to update the mob spawning code! Please report!");
            TerrainControl.printStackTrace(Level.SEVERE, e);
            return null;
        }
    }
}
