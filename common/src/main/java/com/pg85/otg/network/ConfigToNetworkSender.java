package com.pg85.otg.network;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.util.helpers.StreamHelper;

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
    public static void writeConfigsToStream(ConfigProvider configProvider, DataOutput stream, boolean isSinglePlayer) throws IOException
    {
        WorldConfig worldConfig = configProvider.getWorldConfig();
        LocalBiome[] biomes = configProvider.getBiomeArrayByOTGId();
       
        // General information
        StreamHelper.writeStringToStream(stream, worldConfig.getName());

        stream.writeInt(worldConfig.worldFog);
        stream.writeInt(worldConfig.worldNightFog);

        stream.writeInt(worldConfig.waterLevelMax);       

        StreamHelper.writeStringToStream(stream, worldConfig.defaultOceanBiome); // Required for the biome generator
        StreamHelper.writeStringToStream(stream, worldConfig.defaultFrozenOceanBiome); // Required for the biome generator
		
        // Fetch all non-virtual biomes
        Collection<LocalBiome> nonVirtualBiomes = new ArrayList<LocalBiome>();
        Collection<LocalBiome> nonVirtualCustomBiomes = new ArrayList<LocalBiome>();
        for (LocalBiome biome : biomes)
        {
            if (biome == null)
            {
                continue;
            }

            if (!biome.getIds().isVirtual())
            {
                nonVirtualBiomes.add(biome);
                if (biome.isCustom())
                {
                    nonVirtualCustomBiomes.add(biome);
                }
            }
        }

        // BiomeConfigs
        stream.writeInt(nonVirtualBiomes.size());
        for (LocalBiome biome : nonVirtualBiomes)
        {
            if (biome == null)
            {
            	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
            }
            stream.writeInt(biome.getIds().getOTGBiomeId());
            stream.writeInt(biome.getIds().getSavedId());
            biome.getBiomeConfig().writeToStream(stream, isSinglePlayer);
        }
    }
}
