package com.khorn.terraincontrol.bukkit;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;

import net.minecraft.server.v1_4_6.ChunkProviderServer;
import net.minecraft.server.v1_4_6.ChunkRegionLoader;
import net.minecraft.server.v1_4_6.NBTCompressedStreamTools;
import net.minecraft.server.v1_4_6.NBTTagCompound;
import net.minecraft.server.v1_4_6.RegionFile;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;

import com.khorn.terraincontrol.bukkit.commands.BaseCommand;


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
    public void run()
    {

        if (BiomeReplace.isWorking)
        {
            sender.sendMessage(BaseCommand.ErrorColor + "Another instance of biome replace is running");
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
                    sender.sendMessage(BaseCommand.ErrorColor + "Could not validate folder for world's region files.");
                    return;
                }
            }
        }


        regionFiles = regionFolder.listFiles(new MCAFileFilter(".MCA"));
        if (regionFiles == null || regionFiles.length == 0)
        {
            sender.sendMessage(BaseCommand.ErrorColor + "Could not find any region files.");
            return;
        }

        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) world.getHandle().chunkProvider;
        int chunksRewritten = 0;

        try
        {
            sender.sendMessage(BaseCommand.MessageColor + "Lock chunk load");
            Field chunkLoaderField = ChunkProviderServer.class.getDeclaredField("e");

            chunkLoaderField.setAccessible(true);

            ChunkRegionLoader chunkLoader = (ChunkRegionLoader) chunkLoaderField.get(chunkProviderServer);

            Field chunkLoaderLockField = ChunkRegionLoader.class.getDeclaredField("c");

            chunkLoaderLockField.setAccessible(true);

            Object chunkLoaderLock = chunkLoaderLockField.get(chunkLoader);

            synchronized (chunkLoaderLock)
            {

                sender.sendMessage(BaseCommand.MessageColor + "Unload all chunks");

                chunkProviderServer.a();
                chunkProviderServer.unloadChunks();


                sender.sendMessage(BaseCommand.MessageColor + "Start replace...");
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
                        sender.sendMessage(BaseCommand.MessageColor + Integer.toString(i * 100 / regionFiles.length) + "%");
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
                                    if (biomeArray[t] == BiomeIdFrom)
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
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        sender.sendMessage(BaseCommand.MessageColor + "Done. " + chunksRewritten + " chunks rewritten.");


        isWorking = false;


    }

    private static class MCAFileFilter implements FileFilter
    {
        String ext;

        public MCAFileFilter(String extension)
        {
            this.ext = extension.toLowerCase();
        }

        public boolean accept(File file)
        {
            return (file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(ext));
        }
    }
}
