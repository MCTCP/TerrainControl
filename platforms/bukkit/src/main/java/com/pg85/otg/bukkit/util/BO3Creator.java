package com.pg85.otg.bukkit.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.materials.BukkitMaterialData;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.io.SettingsWriterOTGPlus;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BranchFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.minecraft.server.v1_12_R1.ChunkCoordIntPair;

public class BO3Creator extends BOCreator
{
    public BO3Creator(String name)
    {
        this.name = name;
    }

    @SuppressWarnings("deprecation")
    public boolean create(Selection selection, String blockName, boolean branch)
    {
        int tileEntityCount = 1;

        File tileEntitiesFolder = new File(OTG.getEngine().getGlobalObjectsDirectory(), name);

        if (includeTiles)
        {
            tileEntitiesFolder.mkdirs();
        }

        World world = selection.getWorld();

        Location start = selection.getMinimumPoint();
        Location end = selection.getMaximumPoint();

        LocalMaterialData centerBlock = null;
        if (!blockName.isEmpty())
        {
            try {
                centerBlock = MaterialHelper.readMaterial(blockName);
            }
			catch (InvalidConfigException e1)
			{
                centerBlock = null;
            }
        }
        
        int width = selection.getWidth();
        int length = selection.getLength();
        int height = selection.getHeight();

        int widthMin = width;
        int heightMin = height;
        int lengthMin = length;
        int widthMax = Integer.MIN_VALUE;
        int heightMax = Integer.MIN_VALUE;
        int lengthMax = Integer.MIN_VALUE;

        boolean centerBlockFound = false;
        int centerPointX = 0;
        int centerPointY = 0;
        int centerPointZ = 0;
        
        Block block;
        LocalMaterialData data;

        for (int x = 0; x < width; ++x)
        {
            for (int y = 0; y < height; ++y)
            {
                for (int z = 0; z < length; ++z)
                {
                    block = world.getBlockAt(x + start.getBlockX(), y + start.getBlockY(), z + start.getBlockZ());
                    data = BukkitMaterialData.ofBukkitBlock(block);

                    if (centerBlock != null && centerBlock.equals(data))
                    {
                        centerPointX = x + start.getBlockX();
                        centerPointY = y + start.getBlockY();
                        centerPointZ = z + start.getBlockZ();
                        centerBlockFound = true;
                    }

                    if (x < widthMin)
                    {
                        widthMin = x;
                    }
                    if (y < heightMin)
                    {
                        heightMin = y;
                    }
                    if (z < lengthMin)
                    {
                        lengthMin = z;
                    }

                    if (x > widthMax)
                    {
                        widthMax = x;
                    }
                    if (y > heightMax)
                    {
                        heightMax = y;
                    }
                    if (z > lengthMax)
                    {
                        lengthMax = z;
                    }

                }
            }
        }

        if (centerBlock == null || !centerBlockFound)
        {
            centerPointX = (int) Math.floor((start.getBlockX() + end.getBlockX()) / 2d);
            centerPointY = start.getBlockY();
            centerPointZ = (int) Math.floor((start.getBlockZ() + end.getBlockZ()) / 2d);
        }

        Map<ChunkCoordIntPair, List<BO3BlockFunction>> blocksPerChunkArr = new HashMap<ChunkCoordIntPair, List<BO3BlockFunction>>();
        List<BO3BlockFunction> blocksInChunk;
        ChunkCoordIntPair chunkCoordinates;
        LocalMaterialData material;
        BO3BlockFunction blockFunction;
        NamedBinaryTag tag;
        String tileEntityName;
        File tileEntityFile;
        FileOutputStream fos;
        
        for (int x = start.getBlockX(); x <= end.getBlockX(); x++)
        {
            for (int y = start.getBlockY(); y <= end.getBlockY(); y++)
            {
                for (int z = start.getBlockZ(); z <= end.getBlockZ(); z++)
                {
                    blocksInChunk = new ArrayList<BO3BlockFunction>();
                    chunkCoordinates = null;

                    if (branch)
                    {
                        chunkCoordinates = new ChunkCoordIntPair((int) Math.floor(
                                (x - start.getBlockX()) / 16), (int) Math.floor((z - start.getBlockZ()) / 16));
                    } else {
                        chunkCoordinates = new ChunkCoordIntPair(0, 0);
                    }

                    if (blocksPerChunkArr.get(chunkCoordinates) == null)
                    {
                        blocksPerChunkArr.put(chunkCoordinates, blocksInChunk);
                    } else {
                        blocksInChunk = blocksPerChunkArr.get(chunkCoordinates);
                    }

                    block = world.getBlockAt(x, y, z);
                    material = BukkitMaterialData.ofBukkitBlock(block);

                    if (includeAir || !material.isAir())
                    {
                        blockFunction = null;

                        if (branch)
                        {
                            blockFunction = (BO3BlockFunction) CustomObjectConfigFunction.create(null,
                                    BO3BlockFunction.class, x - ((chunkCoordinates.x * 16) + start.getBlockX()) - 8,
                                    y - centerPointY, z - ((chunkCoordinates.z * 16) + start.getBlockZ()) - 7,
                                    material);
                        } else {
                            blockFunction = (BO3BlockFunction) CustomObjectConfigFunction.create(null,
                                    BO3BlockFunction.class, x - centerPointX, y - centerPointY, z - centerPointZ,
                                    material);
                        }

                        if (includeTiles)
                        {
                            // Look for tile entities
                            tag = NBTHelper.getMetadata(world, x, y, z);
                            if (tag != null)
                            {
                                tileEntityName = null;

                                if (branch)
                                {
                                    tileEntityName = tileEntityCount + "-" + getTileEntityName(
                                            tag) + "C" + chunkCoordinates.x + "R" + chunkCoordinates.z + ".nbt";
                                } else {
                                    tileEntityName = tileEntityCount + "-" + getTileEntityName(tag) + ".nbt";
                                }

                                tileEntityFile = new File(tileEntitiesFolder, tileEntityName);

                                tileEntityCount++;
                                try {
                                    tileEntityFile.createNewFile();
                                    fos = new FileOutputStream(tileEntityFile);
                                    tag.writeTo(fos);
                                    fos.flush();
                                    fos.close();
                                    blockFunction.metaDataTag = tag;
                                    blockFunction.metaDataName = name + "/" + tileEntityName;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }
                        blocksInChunk.add(blockFunction);
                    }
                }
            }
        }

        BO3 bo3 = null;
        boolean isStartBO3 = true;
        List<BO3BlockFunction> blocks;
        List<BO3BranchFunction> branches;
        SettingsWriterOTGPlus writer;
        
        for (int x1 = 0; x1 <= Math.abs(widthMin - widthMax); x1++)
        {
            for (int z1 = 0; z1 <= Math.abs(lengthMin - lengthMax); z1++)
            {
                blocks = blocksPerChunkArr.get(new ChunkCoordIntPair(x1, z1));

                if (blocks == null || blocks.isEmpty())
                {
                    continue;
                }

                for (int i = 0; i < 1; i++)
                {
                    branches = new ArrayList<BO3BranchFunction>();

                    if (branch)
                    {

                        if (isStartBO3)
                        {
                            branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null,
                                    BO3BranchFunction.class, 0, 0, 0, (name + "C0R0"), "NORTH", 100));
                        }

                        if (!isStartBO3 && blocksPerChunkArr.get(new ChunkCoordIntPair(x1 + 1, z1)) != null)
                        {
                            branches.add(
                                    (BO3BranchFunction) CustomObjectConfigFunction.create(null, BO3BranchFunction.class,
                                            16, 0, 0, (name + "C" + (x1 + 1) + "R" + z1), "NORTH", 100));
                        }

                        if (!isStartBO3 && x1 == 0)
                        {
                            if (blocksPerChunkArr.get(new ChunkCoordIntPair(x1, z1 + 1)) != null)
                            {
                                branches.add((BO3BranchFunction) CustomObjectConfigFunction.create(null,
                                        BO3BranchFunction.class, 0, 0, 16, (name + "C" + x1 + "R" + (z1 + 1)), "NORTH",
                                        100));
                            }
                        }
                    }

                    if (isStartBO3)
                    {
                        bo3 = new BO3(name, new File(OTG.getEngine().getGlobalObjectsDirectory(), name + ".bo3"));
                    } else
                    {
                        bo3 = new BO3(name, new File(OTG.getEngine().getGlobalObjectsDirectory(), name + "C" + x1 + "R" + z1 + ".bo3"));
                    }

                    bo3.onEnable();

                    if (!isStartBO3 || !branch)
                    {
                        bo3.getSettings().extractBlocks(blocks);
                    }

                    if (!branches.isEmpty())
                    {
                        bo3.getSettings().setBranches(branches);
                    }

                    bo3.getSettings().rotateBlocksAndChecks();

                    OTG.getCustomObjectManager().registerGlobalObject(bo3);

                    bo3.getSettings().settingsMode = ConfigMode.WriteAll;

                    bo3.getSettings().author = author;

                    try {
                        writer = new FileSettingsWriterOTGPlus(bo3.getSettings().getFile());
                        bo3.getSettings().write(writer, ConfigMode.WriteAll);
                    } catch (IOException ex)  {
                        OTG.log(LogMarker.ERROR, "Failed to write to file {}", bo3.getSettings().getFile());
                        OTG.printStackTrace(LogMarker.ERROR, ex);
                        return false;
                    }

                    if (isStartBO3)
                    {
                        isStartBO3 = false;
                        if (branch)
                        {
                            i--;
                        }
                    }
                }

            }
        }
        return true;
    }
}
