package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.bukkit.util.EnumHelper;
import com.khorn.terraincontrol.bukkit.util.MobSpawnGroupHelper;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import net.minecraft.server.v1_9_R1.BiomeBase;
import net.minecraft.server.v1_9_R1.MinecraftKey;
import org.bukkit.block.Biome;

import java.util.List;

public class CustomBiome extends BiomeBase
{
    public final int generationId;

    /**
     * Mojang made the methods on BiomeBase.a protected (so only accessable for
     * classes in the package net.minecraft.world.biome package and for
     * subclasses of BiomeBase.a). To get around this, we have to subclass
     * BiomeBase.a.
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
     * @param biomeIds    Ids of the biome.
     * @return The CustomBiome instance.
     */
    public static CustomBiome createInstance(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        CustomBiome customBiome = new CustomBiome(biomeConfig);

        // Insert the biome in Minecraft's biome mapping
        String biomeNameWithoutSpaces = biomeConfig.getName().replace(' ', '_');
        MinecraftKey biomeKey = new MinecraftKey(PluginStandardValues.PLUGIN_NAME, biomeNameWithoutSpaces);
        int savedBiomeId = biomeIds.getSavedId();
        if (biomeIds.isVirtual())
        {
            // Virtual biomes hack: register, then let original biome overwrite
            // In this way, the id --> biome mapping returns the original biome,
            // and the biome --> id mapping returns savedBiomeId for both the
            // original and custom biome
            BiomeBase existingBiome = BiomeBase.getBiome(savedBiomeId);

            if (existingBiome == null)
            {
                // Original biome not yet registered. This is because it's a
                // custom biome that is loaded after this virtual biome, so it
                // will soon be registered
                BiomeBase.REGISTRY_ID.a(savedBiomeId, biomeKey, customBiome);
            } else
            {
                MinecraftKey existingBiomeKey = BiomeBase.REGISTRY_ID.b(existingBiome);
                BiomeBase.REGISTRY_ID.a(savedBiomeId, biomeKey, customBiome);
                BiomeBase.REGISTRY_ID.a(savedBiomeId, existingBiomeKey, existingBiome);
            }
        } else
        {
            // Normal insertion
            BiomeBase.REGISTRY_ID.a(biomeIds.getSavedId(), biomeKey, customBiome);
            EnumHelper.addEnum(Biome.class, biomeNameWithoutSpaces.toUpperCase(), new Class[0], new Object[0]);
        }

        // Sanity check: check if biome was actually registered
        int registeredSavedId = WorldHelper.getSavedId(customBiome);
        if (registeredSavedId != savedBiomeId)
        {
            throw new AssertionError("Biome " + biomeConfig.getName() + " is not properly registered: got id " + registeredSavedId + ", should be " + savedBiomeId);
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
