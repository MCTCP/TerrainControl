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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;

/**
 * Responsible for loading and unloading the world.
 *
 */
public final class WorldLoader
{

    private final File configsDir;
    public final HashMap<String, ForgeWorld> worlds = new HashMap<String, ForgeWorld>();

    WorldLoader(File configsDir)
    {
        this.configsDir = Preconditions.checkNotNull(configsDir, "configsDir");
    }

    public ForgeWorld getWorld(String name)
    {
        return this.worlds.get(name);
    }

    public File getConfigsFolder()
    {
        return this.configsDir;
    }

    public LocalWorld getWorld(World world)
    {
        return getWorld(WorldHelper.getName(world));
    }

    /**
     * For a dedicated server, we need to register custom biomes
     * really early, even before we can know that TerrainControl
     * is the desired world type. As a workaround, we tentatively
     * load the configs if a config folder exists in the usual
     * location.
     * @param server The Minecraft server.
     */
    public void onWorldAboutToLoad(MinecraftServer server)
    {
        if (!server.isDedicatedServer())
        {
            // Registry works differently on singleplayer
            // We cannot load things yet
            return;
        }

        String worldName = server.getFolderName();
        File worldConfigsFolder = this.getWorldDir(worldName);
        if (!worldConfigsFolder.exists())
        {
            // TerrainControl is probably not enabled for this world
            return;
        }

        ForgeWorld world = new ForgeWorld(worldName);
        TerrainControl.log(LogMarker.INFO, "Loading configs for world \"{}\"..", world.getName());
        ServerConfigProvider configs = new ServerConfigProvider(worldConfigsFolder, world);
        world.provideConfigs(configs);
        this.worlds.put(worldName, world);
    }

    public void onServerStopped()
    {
        unloadWorld();
    }

    public void onQuitFromServer()
    {
        unloadWorld();
    }

    private void unloadWorld()
    {
        for (ForgeWorld world : this.worlds.values())
        {
            if (world != null)
            {
                TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", world.getName());
                markBiomeIdsAsFree(world);
            }
        }
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
        String worldName = WorldHelper.getName(mcWorld);
        ForgeWorld world = new ForgeWorld(worldName);

        TerrainControl.log(LogMarker.INFO, "Loading configs for world \"{}\"....", world.getName());

        File worldConfigsDir = getWorldDir(world.getName());
        ServerConfigProvider configs = new ServerConfigProvider(worldConfigsDir, world);
        world.provideConfigs(configs);
        world.provideWorldInstance(mcWorld);
        this.worlds.put(worldName, world);

        return world;
    }

    private File getWorldDir(String worldName)
    {
        return new File(this.configsDir, "worlds/" + worldName);
    }

    @SideOnly(Side.CLIENT)
    public void demandClientWorld(WorldClient mcWorld, DataInputStream wrappedStream) throws IOException
    {
        ForgeWorld world = new ForgeWorld(ConfigFile.readStringFromStream(wrappedStream));
        ClientConfigProvider configs = new ClientConfigProvider(wrappedStream, world);
        world.provideClientConfigs(mcWorld, configs);
        this.worlds.put(world.getName(), world);
    }

}
