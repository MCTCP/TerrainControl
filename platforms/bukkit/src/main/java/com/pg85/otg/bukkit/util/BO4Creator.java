package com.pg85.otg.bukkit.util;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.configuration.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.io.SettingsWriterOTGPlus;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BO4Creator extends BOCreator
{
    public BO4Creator(String name)
    {
        this.name = name;
    }

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
            } catch (InvalidConfigException e1) {
                centerBlock = null;
            }
        }

        if(centerBlock != null && !centerBlock.isParsed()) {
            centerBlock = null;
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

        LocalMaterialData data;
        Block block;

        ArrayList<BO4BlockFunction> blocksInChunk = new ArrayList<BO4BlockFunction>();
        ChunkCoordinate chunkCoordinates;

        for (int x = 0; x < width; ++x)
        {
            for (int y = 0; y < height; ++y)
            {
                for (int z = 0; z < length; ++z)
                {
                    block = world.getBlockAt(x + start.getBlockX(), y + start.getBlockY(), z + start.getBlockZ());

                    data = MaterialHelper.toLocalMaterialData(DefaultMaterial.getMaterial(block.getType().toString()), block.getData());

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

        Map<ChunkCoordinate, ArrayList<BO4BlockFunction>> blocksPerChunkArr = new HashMap<ChunkCoordinate, ArrayList<BO4BlockFunction>>();
        LocalMaterialData material;
        BO4BlockFunction blockFunction;
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
                    blocksInChunk = new ArrayList<BO4BlockFunction>();

                    if (branch)
                    {
                        chunkCoordinates = ChunkCoordinate.fromChunkCoords(
                                (int) Math.floor((x - start.getBlockX()) / 16),
                                (int) Math.floor((z - start.getBlockZ()) / 16));
                    } else {
                        chunkCoordinates = ChunkCoordinate.fromChunkCoords(0, 0);
                    }

                    if (blocksPerChunkArr.get(chunkCoordinates) == null)
                    {
                        blocksPerChunkArr.put(chunkCoordinates, blocksInChunk);
                    } else {
                        blocksInChunk = blocksPerChunkArr.get(chunkCoordinates);
                    }

                    block = world.getBlockAt(x, y, z);

                    material = MaterialHelper.toLocalMaterialData(DefaultMaterial.getMaterial(block.getType().toString()), block.getData());

                    if (includeAir || !material.isAir())
                    {
                        if (branch)
                        {
                            blockFunction = (BO4BlockFunction) CustomObjectConfigFunction.create(null,
                                    BO4BlockFunction.class,
                                    x - ((chunkCoordinates.getChunkX() * 16) + start.getBlockX()) - 8, y - centerPointY,
                                    z - ((chunkCoordinates.getChunkZ() * 16) + start.getBlockZ()) - 7, material);
                        } else {
                            blockFunction = (BO4BlockFunction) CustomObjectConfigFunction.create(null,
                                    BO4BlockFunction.class, x - centerPointX, y - centerPointY, z - centerPointZ,
                                    material);
                        }

                        if (includeTiles)
                        {
                            // Look for tile entities
                            tag = NBTHelper.getMetadata(world, x, y, z);
                            if (tag != null)
                            {
                                if (branch)
                                {
                                    tileEntityName = tileEntityCount + "-" + getTileEntityName(
                                            tag) + "C" + chunkCoordinates.getBlockX() + "R" + chunkCoordinates.getBlockZ() + ".nbt";
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

        BO4 bo4 = null;
        boolean isStartBO4 = true;
        ArrayList<BO4BlockFunction> blocks;
        List<BO4BranchFunction> branches;
        File file;
        BO4Config config;
        SettingsWriterOTGPlus writer;

        for (int x1 = 0; x1 <= Math.abs(widthMin - widthMax); x1++)
        {
            for (int z1 = 0; z1 <= Math.abs(lengthMin - lengthMax); z1++)
            {
                blocks = blocksPerChunkArr.get(ChunkCoordinate.fromChunkCoords(x1, z1));

                if (blocks == null || blocks.isEmpty())
                {
                    continue;
                }

                for (int i = 0; i < 1; i++)
                {
                    branches = new ArrayList<BO4BranchFunction>();

                    if (branch)
                    {
                        if (isStartBO4)
                        {
                            branches.add((BO4BranchFunction) CustomObjectConfigFunction.create(null,
                                    BO4BranchFunction.class, 0, 0, 0, true, (name + "C0R0"), "NORTH", 100, 0));
                        }

                        if (!isStartBO4 && blocksPerChunkArr.get(ChunkCoordinate.fromChunkCoords(x1 + 1, z1)) != null)
                        {
                            branches.add(
                                    (BO4BranchFunction) CustomObjectConfigFunction.create(null, BO4BranchFunction.class,
                                            16, 0, 0, true, (name + "C" + (x1 + 1) + "R" + z1), "NORTH", 100, 0));
                        }

                        if (!isStartBO4 && x1 == 0)
                        {
                            if (blocksPerChunkArr.get(ChunkCoordinate.fromChunkCoords(x1, z1 + 1)) != null)
                            {
                                branches.add((BO4BranchFunction) CustomObjectConfigFunction.create(null,
                                        BO4BranchFunction.class, 0, 0, 16, true, (name + "C" + x1 + "R" + (z1 + 1)), "NORTH",
                                        100, 0));
                            }
                        }
                    }

                    if (isStartBO4)
                    {
                        file = new File(OTG.getEngine().getGlobalObjectsDirectory(), name + ".BO4");
                        try {
                            config = new BO4Config(new FileSettingsReaderOTGPlus(name, file), true);
                            bo4 = new BO4(name, file, config);
                        }
                        catch (InvalidConfigException e)
                        {
                            OTG.log(LogMarker.ERROR, "Could not export BO4 file, there was an error in the BO4Config: " + e.getMessage());
                            return false;
                        }
                    } else {
                        file = new File(OTG.getEngine().getGlobalObjectsDirectory(), name + "C" + x1 + "R" + z1 + ".BO4");
                        try {
                            config = new BO4Config(new FileSettingsReaderOTGPlus(name, file), true);
                            bo4 = new BO4(name, file, config);
                        }
                        catch (InvalidConfigException e)
                        {
                            OTG.log(LogMarker.ERROR, "Could not export BO4 file, there was an error in the BO4Config: " + e.getMessage());
                            return false;
                        }
                    }

                    bo4.getConfig().settingsMode = WorldConfig.ConfigMode.WriteAll;

                    try
                    {
                        writer = new FileSettingsWriterOTGPlus(bo4.getConfig().getFile());
                        bo4.getConfig().writeWithData(writer, (!isStartBO4 || !branch) && blocks != null ? blocks : new ArrayList<BO4BlockFunction>(), branches != null ? branches : new ArrayList<BO4BranchFunction>());
                        OTG.getCustomObjectManager().registerGlobalObject(bo4);
                    }
                    catch (IOException ex)
                    {
                        OTG.log(LogMarker.ERROR, "Failed to write to file {}", bo4.getConfig().getFile());
                        OTG.printStackTrace(LogMarker.ERROR, ex);
                        return false;
                    }

                    if (isStartBO4)
                    {
                        isStartBO4 = false;
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

