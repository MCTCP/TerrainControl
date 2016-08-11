package com.khorn.terraincontrol.forge;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ClientConfigProvider;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.ReflectionHelper;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;

/**
 * Responsible for loading and unloading the world.
 *
 */
public final class WorldLoader
{

    private final File configsDir;
    private ForgeWorld worldOrNull;

    WorldLoader(File configsDir)
    {
        this.configsDir = Preconditions.checkNotNull(configsDir, "configsDir");
    }

    public ForgeWorld getWorld(String name)
    {
        ForgeWorld world = this.worldOrNull;
        if (world == null)
        {
            return null;
        }
        if (!world.getName().equals(name))
        {
            return null;
        }
        return world;
    }

    public LocalWorld getMainWorld()
    {
        return worldOrNull;
    }

    public File getConfigsFolder()
    {
        return configsDir;
    }

    public LocalWorld getWorld(World world)
    {
        return getWorld(WorldHelper.getName(world));
    }

    public void onWorldAboutToLoad(MinecraftServer server)
    {
        // Empty!
    }

    public void onServerStopped()
    {
        ForgeWorld world = this.worldOrNull;
        if (world != null)
        {
            TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", world.getName());
            markBiomeIdsAsFree(world);
        }

        this.worldOrNull = null;
    }

    /**
     * "Evil" method that forces Forge to reuse the biome ids that we used. On
     * singleplayer this is required to be able to properly load another world
     * with custom biomes.
     * @param world The world to unload.
     */
    private void markBiomeIdsAsFree(ForgeWorld world)
    {
        BitSet biomeIdsInUse = ReflectionHelper.getValueInFieldOfType(Biome.REGISTRY, BitSet.class);
        Collection<Integer> customBiomeIds = world.getConfigs().getWorldConfig().customBiomeGenerationIds.values();
        for (Integer id : customBiomeIds)
        {
            // Allow other worlds to reuse this id
            biomeIdsInUse.clear(id);
        }
    }

    public ForgeWorld demandServerWorld(WorldServer mcWorld)
    {
        ForgeWorld world = new ForgeWorld(WorldHelper.getName(mcWorld));

        TerrainControl.log(LogMarker.INFO, "Loading configs for world \"{}\"...", world.getName());
        File worldConfigsDir = new File(configsDir, "worlds/" + world.getName());
        ServerConfigProvider configs = new ServerConfigProvider(worldConfigsDir, world);
        world.provideConfigs(configs);
        world.provideWorldInstance(mcWorld);

        this.worldOrNull = world;

        return world;
    }

    public void demandClientWorld(WorldClient mcWorld, DataInputStream wrappedStream) throws IOException
    {
        ForgeWorld world = new ForgeWorld(ConfigFile.readStringFromStream(wrappedStream));
        ClientConfigProvider configs = new ClientConfigProvider(wrappedStream, world);
        world.provideClientConfigs(mcWorld, configs);

        if (this.worldOrNull == null)
        {
            this.worldOrNull = world;
        }
    }

}
