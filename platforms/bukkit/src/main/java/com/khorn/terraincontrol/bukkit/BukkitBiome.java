package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.Block;

/**
 * The BukkitBiome is basically a wrapper for the BiomeBase. If you look at
 * the constructor and the method you will see that this is the case.
 */
public class BukkitBiome implements LocalBiome
{
    private BiomeBase biomeBase;
    private boolean isCustom = false;
    private int customID;

    private BiomeIds biomeIds;
    private String name;

    private float temperature;
    private float humidity;

    /**
     * Wraps the vanilla biome into a LocalBiome instance.
     * 
     * @param biome The vanilla biome to wrap.
     * @return The wrapped biome.
     */
    public static BukkitBiome forVanillaBiome(BiomeBase biome)
    {
        return new BukkitBiome(biome);
    }

    /**
     * Wraps the custom biome into a local biome.
     * 
     * @param biomeBase The BiomeBase instance the biome is based on.
     * @param biomeName The biome name. For non-virtual biomes (see
     *            {@link BiomeIds#isVirtual()}) this must match the name
     *            provided in the BiomeBase instance.
     * @param biomeIds The id of the biome. The id used to save to the map
     *            files (see {@link BiomeIds#getSavedId()}) must match the name
     *            provided in the BiomeBase instance.
     * @param customId Increases 1 for every custom biome on the server. First
     *            custom biome has 0, second 1, and so on. Old/legacy/only
     *            kept for compatibility.
     * @return The custom biome.
     * @throws IllegalAgrumentException When the biomeName or biomeId doesn't
     *             match the biomeBase.
     */
    public static BukkitBiome forCustomBiome(BiomeBase biomeBase, String biomeName, BiomeIds biomeIds, int customId)
    {
        if (biomeIds.getSavedId() != biomeBase.id)
        {
            throw new IllegalArgumentException("Passed wrong biome id (BiomeBase: " + biomeBase.id + ", BiomeId: " + biomeIds + ", Name: "
                    + biomeName + ")");
        }
        if (biomeIds.isVirtual())
        {
            return new BukkitBiome(biomeBase, biomeName, customId, biomeIds.getGenerationId());
        } else
        {
            String biomeBaseName = biomeBase.af;
            if (!biomeBaseName.equals(biomeName))
            {
                throw new IllegalArgumentException("For non-virtual biomes, the name must match the BiomeBase name (" + biomeBaseName
                        + " vs " + biomeName + ")");
            }
            return new BukkitBiome(biomeBase, customId);
        }
    }

    // For vanilla biomes
    protected BukkitBiome(BiomeBase biome)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(biomeBase.id);
        this.name = biome.af;

        this.temperature = biome.temperature;
        this.humidity = biome.humidity;
    }

    // For non-virtual custom biomes
    private BukkitBiome(BiomeBase biome, int customId)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(biomeBase.id);
        this.isCustom = true;
        this.customID = customId;
        this.name = biome.af;

        this.temperature = biome.temperature;
        this.humidity = biome.humidity;

    }

    // For virtual custom biomes
    private BukkitBiome(BiomeBase biome, String _name, int customId, int virtualId)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(virtualId, biome.id);
        this.isCustom = true;
        this.customID = customId;
        this.name = _name;

        this.temperature = biome.temperature;
        this.humidity = biome.humidity;

    }

    @Override
    public boolean isCustom()
    {
        return this.isCustom;
    }

    @Override
    public int getCustomId()
    {
        return customID;
    }

    public BiomeBase getHandle()
    {
        return biomeBase;
    }

    @Override
    public void setEffects(BiomeConfig config)
    {
        ((CustomBiome) this.biomeBase).setEffects(config);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public BiomeIds getIds()
    {
        return this.biomeIds;
    }

    @Override
    public float getTemperature()
    {
        return this.temperature;
    }

    @Override
    public float getWetness()
    {
        return this.humidity;
    }

    @Override
    public float getSurfaceHeight()
    {
        return this.biomeBase.am;
    }

    @Override
    public float getSurfaceVolatility()
    {
        return this.biomeBase.an;
    }

    @Override
    public int getSurfaceBlock()
    {
        return Block.b(this.biomeBase.ai);
    }

    @Override
    public int getGroundBlock()
    {
        return Block.b(this.biomeBase.ak);
    }

    @Override
    public float getTemperatureAt(int x, int y, int z)
    {
        return this.biomeBase.a(x, y, z);
    }
}