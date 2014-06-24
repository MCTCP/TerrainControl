package com.khorn.terraincontrol.bukkit.commands.runnable;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.server.v1_7_R1.*;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;

import java.io.*;
import java.lang.reflect.Field;

public class BiomeReplace implements Runnable
{

    private CraftWorld world;
    private CommandSender sender;
    private byte BiomeIdFrom;
    private byte BiomeIdTo;

    @SuppressWarnings("FieldCanBeLocal")
    private transient File[] regionFiles = null;

    private static boolean isWorking = false;

    public BiomeReplace(CraftWorld _world, int biomeIDFrom, int biomeIDTo, CommandSender _sender)
    {
        this.world = _world;

        this.sender = _sender;
        this.BiomeIdFrom = (byte) biomeIDFrom;
        this.BiomeIdTo = (byte) biomeIDTo;
    }


    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Override
    public void run()
    {

        if (BiomeReplace.isWorking)
        {
            sender.sendMessage(BaseCommand.ERROR_COLOR + "Another instance of biome replace is running");
            return;
        }

        BiomeReplace.isWorking = true;


        File regionFolder = new File(world.getWorldFolder(), "region");
        if (!regionFolder.exists() || !regionFolder.isDirectory())
        {
            regionFolder = new File(world.getWorldFolder(), "DIM-1" + File.separator + "region");  // nether worlds
            if (!regionFolder.exists() || !regionFolder.isDirectory())
            {
                regionFolder = new File(world.getWorldFolder(), "DIM1" + File.separator + "region");  // "the end" worlds; not sure why "DIM1" vs "DIM-1", but that's how it is
                if (!regionFolder.exists() || !regionFolder.isDirectory())
                {
                    sender.sendMessage(BaseCommand.ERROR_COLOR + "Could not validate folder for world's region files.");
                    return;
                }
            }
        }


        regionFiles = regionFolder.listFiles(new MCAFileFilter(".MCA"));
        if (regionFiles == null || regionFiles.length == 0)
        {
            sender.sendMessage(BaseCommand.ERROR_COLOR + "Could not find any region files.");
            return;
        }

        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) world.getHandle().chunkProvider;
        int chunksRewritten = 0;

        try
        {
            sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Lock chunk load");
            Field chunkLoaderField = ChunkProviderServer.class.getDeclaredField("e");

            chunkLoaderField.setAccessible(true);

            ChunkRegionLoader chunkLoader = (ChunkRegionLoader) chunkLoaderField.get(chunkProviderServer);

            Field chunkLoaderLockField = ChunkRegionLoader.class.getDeclaredField("c");

            chunkLoaderLockField.setAccessible(true);

            Object chunkLoaderLock = chunkLoaderLockField.get(chunkLoader);

            synchronized (chunkLoaderLock)
            {

                sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Unload all chunks");

                chunkProviderServer.a();
                chunkProviderServer.unloadChunks();


                sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Start replace...");
                long time = System.currentTimeMillis();


                for (int i = 0; i < regionFiles.length; i++)
                {

                    RegionFile file = new RegionFile(regionFiles[i]);

                    long time2 = System.currentTimeMillis();

                    if (time2 < time)
                    {
                        time = time2;
                    }

                    if (time2 > time + 500L)
                    {
                        sender.sendMessage(BaseCommand.MESSAGE_COLOR + Integer.toString(i * 100 / regionFiles.length) + "%");
                        time = time2;
                    }

                    for (int x = 0; x < 32; x++)
                    {
                        for (int z = 0; z < 32; z++)
                        {
                            if (!file.c(x, z))
                                continue;

                            DataInputStream localDataInputStream = file.a(x, z);

                            NBTTagCompound localNBTTagCompound1 = NBTCompressedStreamTools.a((DataInput) localDataInputStream);

                            localDataInputStream.close();

                            NBTTagCompound chunkTag = localNBTTagCompound1.getCompound("Level");

                            if (chunkTag.hasKey("Biomes"))
                            {
                                byte[] biomeArray = chunkTag.getByteArray("Biomes");

                                boolean needSave = false;

                                for (int t = 0; t < biomeArray.length; t++)
                                    if (BiomeIdFrom == -1 || biomeArray[t] == BiomeIdFrom)
                                    {
                                        biomeArray[t] = BiomeIdTo;
                                        needSave = true;
                                    }

                                if (needSave)
                                {

                                    chunkTag.setByteArray("Biomes", biomeArray);
                                    chunksRewritten++;

                                    DataOutputStream localDataOutputStream = file.b(x, z);
                                    NBTCompressedStreamTools.a(localNBTTagCompound1, (DataOutput) localDataOutputStream);
                                    localDataOutputStream.close();

                                }
                            }

                        }
                    }


                }
            }


        } catch (NoSuchFieldException e)
        {
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        } catch (IllegalAccessException e)
        {
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        } catch (IOException e)
        {
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }

        sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Done. " + chunksRewritten + " chunks rewritten.");


        isWorking = false;


    }

    private static class MCAFileFilter implements FileFilter
    {
        String ext;

        MCAFileFilter(String extension)
        {
            this.ext = extension.toLowerCase();
        }

        @Override
        public boolean accept(File file)
        {
            return (file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(ext));
        }
    }
}
