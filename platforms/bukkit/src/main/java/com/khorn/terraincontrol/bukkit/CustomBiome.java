package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.util.MobSpawnGroupHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.server.v1_9_R1.BiomeBase;
import net.minecraft.server.v1_9_R1.MinecraftKey;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftBlock;

import java.lang.reflect.Field;
import java.util.List;

public class CustomBiome extends BiomeBase
{
    public final int generationId;

    /**
     * Mojang made the methods on BiomeBase.a protected (so only accessable for
     * classes in the package net.minecraft.world.biome package and for
     * subclasses of BiomeBase.a). To get around this, we have to subclass
     * BiomeBase.a.
     *
     */
    private static class BiomeBase_a extends BiomeBase.a
    {

        public BiomeBase_a(String name, BiomeConfig biomeConfig)
        {
            super(name);

            // Minecraft doesn't like temperatures between 0.1 and 0.2, so avoid
            // them: round them to either 0.1 or 0.2
            float adjustedTemperature = biomeConfig.biomeTemperature;
            if (adjustedTemperature >= 0.1 && adjustedTemperature <= 0.2)
            {
                if (adjustedTemperature >= 1.5)
                    adjustedTemperature = 0.2f;
                else
                    adjustedTemperature = 0.1f;
            }

            c(biomeConfig.biomeHeight);
            d(biomeConfig.biomeVolatility);
            a(adjustedTemperature);
            b(biomeConfig.biomeWetness);
            if (biomeConfig.biomeWetness <= 0.0001)
            {
                a(); // disableRain()
            }
        }
    }

    /**
     * Creates a CustomBiome instance. Minecraft automatically registers those
     * instances in the BiomeBase constructor. We don't want this for virtual
     * biomes (the shouldn't overwrite real biomes), so we restore the old
     * biome, unregistering the virtual biome.
     *
     * @param biomeConfig Settings of the biome
     * @param biomeIds Ids of the biome.
     * @return The CustomBiome instance.
     */
    public static CustomBiome createInstance(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        CustomBiome customBiome = new CustomBiome(biomeConfig);

        if (!biomeIds.isVirtual())
        {
            // Insert the biome in Minecraft's biome mapping
            BiomeBase.REGISTRY_ID.a(biomeIds.getSavedId(), new MinecraftKey(biomeConfig.getName()), customBiome);

            // Insert the biome in CraftBukkit's biome mapping
            try
            {
                Field biomeMapping = CraftBlock.class.getDeclaredField("BIOME_MAPPING");
                biomeMapping.setAccessible(true);
                Biome[] mappingArray = (Biome[]) biomeMapping.get(null);

                mappingArray[biomeIds.getSavedId()] = Biome.OCEAN;
            } catch (Exception e)
            {
                TerrainControl.log(LogMarker.FATAL, "Couldn't update Bukkit's biome mappings!");
                TerrainControl.printStackTrace(LogMarker.FATAL, e);
            }
        }

        return customBiome;
    }

    private CustomBiome(BiomeConfig biomeConfig)
    {
        super(new BiomeBase_a(biomeConfig.getName(), biomeConfig));
        this.generationId = biomeConfig.generationId;

        // Sanity check
        if (this.getHumidity() != biomeConfig.biomeWetness)
        {
            throw new AssertionError("Biome temperature mismatch");
        }

        this.r = ((BukkitMaterialData) biomeConfig.surfaceBlock).internalBlock();
        this.s = ((BukkitMaterialData) biomeConfig.groundBlock).internalBlock();

        // Mob spawning
        addMobs(this.u, biomeConfig.spawnMonsters);
        addMobs(this.v, biomeConfig.spawnCreatures);
        addMobs(this.w, biomeConfig.spawnWaterCreatures);
        addMobs(this.x, biomeConfig.spawnAmbientCreatures);
    }

    // Adds the mobs to the internal list.
    protected void addMobs(List<BiomeMeta> internalList, List<WeightedMobSpawnGroup> configList)
    {
        internalList.clear();
        internalList.addAll(MobSpawnGroupHelper.toMinecraftlist(configList));
    }
}
