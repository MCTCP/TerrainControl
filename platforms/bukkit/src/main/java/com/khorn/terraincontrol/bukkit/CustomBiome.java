package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.MobNames;
import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.BiomeMeta;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityTypes;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftBlock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class CustomBiome extends BiomeBase
{
    public final int generationId;

    /**
     * Creates a CustomBiome instance. Minecraft automatically registers those
     * instances in the BiomeBase constructor. We don't want this for virtual
     * biomes (the shouldn't overwrite real biomes), so we restore the old
     * biome, unregistering the virtual biome.
     *
     * @param name Name of the biome.
     * @param biomeIds Ids of the biome.
     * @return The CustomBiome instance.
     */
    public static CustomBiome createInstance(String name, BiomeIds biomeIds)
    {
        if (biomeIds.isVirtual())
        {
            // Don't register (the only way to do this on Bukkit is to restore
            // the original biome afterwards)
            BiomeBase toRestore = BiomeBase.getBiome(biomeIds.getSavedId());
            CustomBiome customBiome = new CustomBiome(name, biomeIds);
            BiomeBase.n()[biomeIds.getSavedId()] = toRestore;

            return customBiome;
        } else
        {
            // Just register normally
            return new CustomBiome(name, biomeIds);
        }
    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private CustomBiome(String name, BiomeIds biomeIds)
    {
        super(biomeIds.getSavedId());
        this.generationId = biomeIds.getGenerationId();
        this.a(name);

        // Insert the biome in CraftBukkit's biome mapping
        if (!biomeIds.isVirtual())
        {
            try
            {
                Field biomeMapping = CraftBlock.class.getDeclaredField("BIOME_MAPPING");
                biomeMapping.setAccessible(true);
                Biome[] mappingArray = (Biome[]) biomeMapping.get(null);

                mappingArray[id] = Biome.OCEAN;

            } catch (Exception e)
            {
                TerrainControl.log(LogMarker.FATAL, "Couldn't update Bukkit's biome mappings!");
                TerrainControl.printStackTrace(LogMarker.FATAL, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void setEffects(BiomeConfig config)
    {
        this.am = config.biomeHeight;
        this.an = config.biomeVolatility;
        this.ai = ((BukkitMaterialData) config.surfaceBlock).internalBlock();
        this.ak = ((BukkitMaterialData) config.groundBlock).internalBlock();
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

    // Adds the mobs to the internal list. Displays a warning for each mob
    // type it doesn't understand
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
                // The .toLowerCase() is just a safeguard so that we get
                // notified if this.af is no longer the biome name
                TerrainControl.log(LogMarker.WARN, "Mob type {} not found in {}",
                        new Object[] {mobGroup.getMobName(), this.af.toLowerCase()});
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
            TerrainControl.log(LogMarker.FATAL, "Someone forgot to update the mob spawning code! Please report!");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
            return null;
        }
    }
}
