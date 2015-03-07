package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.util.MobSpawnGroupHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.server.v1_8_R2.BiomeBase;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R2.block.CraftBlock;

import java.lang.reflect.Field;
import java.util.List;

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
            BiomeBase.getBiomes()[biomeIds.getSavedId()] = toRestore;

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

    public void setEffects(BiomeConfig config)
    {
        this.an = config.biomeHeight;
        this.ao = config.biomeVolatility;
        this.ak = ((BukkitMaterialData) config.surfaceBlock).internalBlock();
        this.al = ((BukkitMaterialData) config.groundBlock).internalBlock();
        this.temperature = config.biomeTemperature;
        this.humidity = config.biomeWetness;
        if (this.humidity == 0)
        {
            this.b(); // this.disableRain()
        }

        // Mob spawning
        addMobs(this.at, config.spawnMonsters);
        addMobs(this.au, config.spawnCreatures);
        addMobs(this.av, config.spawnWaterCreatures);
        addMobs(this.aw, config.spawnAmbientCreatures);
    }

    // Adds the mobs to the internal list.
    protected void addMobs(List<BiomeMeta> internalList, List<WeightedMobSpawnGroup> configList)
    {
        internalList.clear();
        internalList.addAll(MobSpawnGroupHelper.toMinecraftlist(configList));
    }
}
