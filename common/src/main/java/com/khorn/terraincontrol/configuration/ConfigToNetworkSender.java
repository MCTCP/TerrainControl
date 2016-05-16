package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalBiome;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Writes the relevant settings of a configuration file to a network stream.
 *
 * <p>Relevant settings include things like grass color, water color and biome
 * names: basically everything that is needed to display a world properly on the
 * client. Irrelevant settings include things like biome size, ore distribution,
 * etc.: the client doesn't need to generate chunks on its own.</p>
 */
public final class ConfigToNetworkSender
{

    /**
     * Sends the relevant settings in the {@link ConfigProvider} to the given
     * network stream.
     * @param configProvider All the settings of a world.
     * @param stream         Stream to write to.
     * @throws IOException If an IO error occurs.
     */
    public static void send(ConfigProvider configProvider, DataOutput stream) throws IOException
    {
        WorldConfig worldConfig = configProvider.getWorldConfig();
        LocalBiome[] biomes = configProvider.getBiomeArray();

        // General information
        ConfigFile.writeStringToStream(stream, worldConfig.getName());

        stream.writeInt(worldConfig.WorldFog);
        stream.writeInt(worldConfig.WorldNightFog);

        // Fetch all non-virtual biomes
        Collection<LocalBiome> nonVirtualBiomes = new ArrayList<LocalBiome>();
        Collection<LocalBiome> nonVirtualCustomBiomes = new ArrayList<LocalBiome>();
        for (LocalBiome biome : biomes)
        {
            if (biome == null)
                continue;

            if (!biome.getIds().isVirtual())
            {
                nonVirtualBiomes.add(biome);
                if (biome.isCustom())
                {
                    nonVirtualCustomBiomes.add(biome);
                }
            }
        }

        // Write them to the stream
        stream.writeInt(nonVirtualCustomBiomes.size());
        for (LocalBiome biome : nonVirtualCustomBiomes)
        {
            ConfigFile.writeStringToStream(stream, biome.getName());
            stream.writeInt(biome.getIds().getSavedId());
        }

        // BiomeConfigs
        stream.writeInt(nonVirtualBiomes.size());
        for (LocalBiome biome : nonVirtualBiomes)
        {
            if (biome == null)
            {
                continue;
            }
            stream.writeInt(biome.getIds().getSavedId());
            biome.getBiomeConfig().writeToStream(stream);
        }
    }
}
