package com.khorn.terraincontrol.forge.metrics;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.TCPlugin;
import com.khorn.terraincontrol.forge.metrics.Metrics.Graph;
import com.khorn.terraincontrol.util.helpers.MetricsHelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Create this when we're on a server, and when the world has loaded.
 *
 */
public class ForgeMetricsHelper extends MetricsHelper
{
    private final ModContainer container;
    private final TCPlugin plugin;

    public ForgeMetricsHelper(TCPlugin plugin)
    {
        this.plugin = plugin;
        this.container = FMLCommonHandler.instance().findContainerFor(plugin);

        // Start immediately
        startMetrics();
    }

    private void startMetrics()
    {
        // Get enabled worlds
        Iterable<LocalWorld> loadedWorlds;
        if (plugin.getWorld() == null)
        {
            loadedWorlds = Collections.emptyList();
        } else
        {
            loadedWorlds = Arrays.asList(plugin.getWorld());
        }

        // Calculate the biome mode numbers
        calculateBiomeModes(loadedWorlds);

        // Send them to mcstats.org
        // Thanks to the slightly different package names,
        // this code had to be copy/pasted from the Bukkit side.
        // When you update this method, also check the Bukkit class!
        try
        {
            Metrics metrics = new Metrics(container.getModId(), container.getVersion());

            Graph usedBiomeModesGraph = metrics.createGraph("Biome modes used");

            usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Normal")
            {
                @Override
                public int getValue()
                {
                    return normalMode;
                }
            });
            usedBiomeModesGraph.addPlotter(new Metrics.Plotter("FromImage")
            {
                @Override
                public int getValue()
                {
                    return fromImageMode;
                }
            });
            usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Default")
            {
                @Override
                public int getValue()
                {
                    return vanillaMode;
                }
            });
            usedBiomeModesGraph.addPlotter(new Metrics.Plotter("OldGenerator")
            {
                @Override
                public int getValue()
                {
                    return oldBiomeMode;
                }
            });
            usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Custom / Unknown")
            {
                @Override
                public int getValue()
                {
                    return customMode;
                }
            });

            metrics.start();
        } catch (IOException e)
        {
            // Failed to submit stats
        }
    }
}
