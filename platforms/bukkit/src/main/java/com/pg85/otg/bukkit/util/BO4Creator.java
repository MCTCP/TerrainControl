package com.pg85.otg.bukkit.util;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.materials.BukkitMaterialData;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.configuration.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.materials.MaterialHelper;
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

        if (includeTiles || branch)
        {
            tileEntitiesFolder.mkdirs();
        }

        World world = selection.getWorld();
        if (world == null) throw new NullPointerException("World was null for WorldEdit selection, could not process");

        Location start = selection.getMinimumPoint();
        Location end = selection.getMaximumPoint();
        LocalMaterialData centerBlock = null;
        if (!blockName.isEmpty())
        {
            try {
                centerBlock = MaterialHelper.readMaterial(blockName);
            } catch (InvalidConfigException e1) {
                // spit out a warning, but keep doing the export. centerBlock should still be null.
                OTG.log(LogMarker.WARN, "export command could not process center block argument: "+e1.getLocalizedMessage());
            }
        }

        int width = selection.getWidth();
        int length = selection.getLength();
        int height = selection.getHeight();

        boolean centerBlockFound = false;
        int centerPointX = 0;
        int centerPointY = 0;
        int centerPointZ = 0;

        LocalMaterialData data;
        Block block;

        ArrayList<BO4BlockFunction> blocksInChunk = new ArrayList<BO4BlockFunction>();
        ChunkCoordinate chunkCoordinates;

        if (centerBlock != null) {
            // This loop looks for the center block
            outer: {
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        for (int z = 0; z < length; ++z)
                        {
                            block = world.getBlockAt(x + start.getBlockX(), y + start.getBlockY(), z + start.getBlockZ());
                            data = BukkitMaterialData.ofBukkitBlock(block);

                            // If we have a match for the center block, update values and end the loop
                            if (centerBlock.equals(data)) {
                                centerPointX = x + start.getBlockX();
                                centerPointY = y + start.getBlockY();
                                centerPointZ = z + start.getBlockZ();
                                centerBlockFound = true;
                                break outer;
                            }
                        }
                    }
                }
            }
        }
        // if there is no center block argument, or if
        if (centerBlock == null || !centerBlockFound)
        {
            centerPointX = (start.getBlockX() + end.getBlockX()) >> 1;
            centerPointY = start.getBlockY();
            centerPointZ = (start.getBlockZ() + end.getBlockZ()) >> 1;
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
                    if (branch)
                    {
                        chunkCoordinates = ChunkCoordinate.fromChunkCoords(
                                (x - start.getBlockX()) >> 4,(z - start.getBlockZ()) >> 4);
                    } else {
                        chunkCoordinates = ChunkCoordinate.fromChunkCoords(0, 0);
                    }
                    // Create new list of blocks if one does not exist for this chunk
                    if (blocksPerChunkArr.containsKey(chunkCoordinates)) {
                        blocksInChunk = blocksPerChunkArr.get(chunkCoordinates);
                    } else {
                        blocksInChunk = new ArrayList<BO4BlockFunction>();
                        blocksPerChunkArr.put(chunkCoordinates, blocksInChunk);
                    }

                    block = world.getBlockAt(x, y, z);
                    material = BukkitMaterialData.ofBukkitBlock(block);

                    // skip air blocks if not set to be included
                    if (!includeAir && material.isAir()) {
                        continue;
                    }

                    // Adds the block to a branch of the BO4 instead of the main
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
                            try
                            {
                                tileEntityFile.createNewFile();
                                fos = new FileOutputStream(tileEntityFile);
                                tag.writeTo(fos);
                                fos.flush();
                                fos.close();
                                blockFunction.metaDataTag = tag;
                                blockFunction.metaDataName = name + "/" + tileEntityName;
                            } catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                    blocksInChunk.add(blockFunction);
                }
            }
        }
        
        // Array with booleans, saying if a given branch exists
        boolean[][] exists;
        // Array with booleans, saying if a given branch has been written yet
        boolean[][] processed;
        List<BO4BranchFunction> masterBranches = new ArrayList<BO4BranchFunction>();;
        if (branch)
        {
            exists = new boolean[width][length];
            processed = new boolean[width][length];
            for (ChunkCoordinate c : blocksPerChunkArr.keySet())
            {
                // A chunk is marked as existing if it has blocks, otherwise it's ignored
                exists[c.getChunkX()][c.getChunkZ()] = !blocksPerChunkArr.get(c).isEmpty();
            }

            for (int x1 = 0; x1 < width; x1++)
            {
                for (int z1 = 0; z1 < length; z1++)
                {
                    // If a branch exists, and has not been processed, add it to master branches and process it
                    if (exists[x1][z1] && !processed[x1][z1])
                    {
                        masterBranches.add(recursiveHelper(exists, processed, blocksPerChunkArr, x1, z1, x1*16, z1*16, tileEntitiesFolder));
                    }
                }
            }
        }
        // write master BO4 to file
        ArrayList<BO4BlockFunction> blocks = !branch ? blocksPerChunkArr.get(ChunkCoordinate.fromChunkCoords(0, 0)) : new ArrayList<>();
        writeToFile(name, blocks, masterBranches, ConfigMode.WriteAll, OTG.getEngine().getGlobalObjectsDirectory());
        return true;
    }

    /** Recursive helper that finds and creates BO4 branches, then returns a master branch
     *
     * @param exists A 2d array with markers for which branches have blocks in them
     * @param processed A 2d array with markers for which branches have already been processed
     * @param blocksPerChunkArr Map with blocks for all chunks, needed for file writing
     * @param x chunk X coordinate for this branch
     * @param z chunk Z coordinate for this branch
     * @param posX Placement relative to parent
     * @param posZ Placement relative to parent
     * @return A branch function for this branch
     */
    private BO4BranchFunction recursiveHelper(boolean[][] exists, boolean[][] processed, Map<ChunkCoordinate, ArrayList<BO4BlockFunction>> blocksPerChunkArr, int x, int z, int posX, int posZ, File folder) {
        // Mark as processed
        processed[x][z] = true;
        // Check if neighbours are eligible, process them if so
        ArrayList<BO4BranchFunction> branches = new ArrayList<>();
        // East
        if (x < exists.length-1 && exists[x+1][z] && !processed[x+1][z])
        {
            branches.add(recursiveHelper(exists, processed, blocksPerChunkArr, x+1, z, 16, 0, folder));
        }
        // South
        if (z < exists[0].length && exists[x][z+1] && !processed[x][z+1])
        {
            branches.add(recursiveHelper(exists, processed, blocksPerChunkArr, x, z+1, 0, 16, folder));
        }
        // North
        if (z > 0 && exists[x][z-1] && !processed[x][z-1])
        {
            branches.add(recursiveHelper(exists, processed, blocksPerChunkArr, x, z-1, 0, -16, folder));
        }
        // West
        if (x > 0 && exists[x-1][z] && !processed[x-1][z])
        {
            branches.add(recursiveHelper(exists, processed, blocksPerChunkArr, x-1, z, -16, 0, folder));
        }
        // write this branch to file
        writeToFile((name + "C" + x + "R" + z), blocksPerChunkArr.get(ChunkCoordinate.fromChunkCoords(x,z)), branches, ConfigMode.WriteWithoutComments, folder);

        // return this branch
        return ((BO4BranchFunction) CustomObjectConfigFunction.create(null, BO4BranchFunction.class, posX, 0, posZ, true, (name + "C" + x + "R" + z), "NORTH", 100, 0));
    }

    /** Writes a BO4 config to file, and registers it to global objects
     *
     * @param filename name of the file to be written (without ".BO4")
     * @param blocks The block functions for this config
     * @param branches The branch functions for this config
     */
    private void writeToFile(String filename, List<BO4BlockFunction> blocks, List<BO4BranchFunction> branches, ConfigMode mode, File directory)
    {
        BO4 bo4;
        try
        {
            File file = new File(directory,filename + ".BO4");
            BO4Config config = new BO4Config(new FileSettingsReaderOTGPlus(name, file), true);
            config.author = author;
            bo4 = new BO4(name, file, config);
        }
        catch (InvalidConfigException e)
        {
            OTG.log(LogMarker.ERROR, "Could not export BO4 file, there was an error in the BO4Config: " + e.getMessage());
            return;
        }
        bo4.getConfig().settingsMode = mode;
        try
        {
            bo4.getConfig().writeWithData(new FileSettingsWriterOTGPlus(bo4.getConfig().getFile()), blocks, branches);
            OTG.getCustomObjectManager().registerGlobalObject(bo4);
        }
        catch (IOException ex)
        {
            OTG.log(LogMarker.ERROR, "Failed to write to file {}", bo4.getConfig().getFile());
            OTG.printStackTrace(LogMarker.ERROR, ex);
        }
    }
}

