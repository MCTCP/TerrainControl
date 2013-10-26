package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BO3Loader implements CustomObjectLoader
{

    /** A list of already loaded meta Tags. The path is the key, a NBT Tag is
     * the value.
     */
    private static Map<String, Tag> loadedTags = new HashMap<String, Tag>();

    public BO3Loader()
    {
        // Register BO3 ConfigFunctions
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("Block", BlockFunction.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("Branch", BranchFunction.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("WeightedBranch", WeightedBranchFunction.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("RandomBlock", RandomBlockFunction.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("BlockCheck", BlockCheck.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("BlockCheckNot", BlockCheckNot.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("LightCheck", LightCheck.class);
    }

    @Override
    public CustomObject loadFromFile(String objectName, File file)
    {
        return new BO3(objectName, file);
    }

    // Actually, we use tryToClose(..) to close the stream
    @SuppressWarnings("resource")
    public static Tag loadMetadata(String name, File bo3File)
    {
        String path = bo3File.getParent() + File.separator + name;

        if (loadedTags.containsKey(path))
        {
            // Found a cached one
            return loadedTags.get(path);
        }

        // Load from file
        Tag metadata;
        FileInputStream stream = null;
        try
        {
            // Read it from a file next to the BO3
            stream = new FileInputStream(path);
            // Get the tag
            metadata = Tag.readFrom(stream, true);
            stream.close();
        } catch (FileNotFoundException e)
        {
            // File not found
            TerrainControl.log(Level.WARNING, "NBT file {0} not found", path);
            tryToClose(stream);
            return null;
        } catch (IOException e)
        {
            // Not a compressed NBT file, try uncompressed
            tryToClose(stream);
            try
            {
                // Read it from a file next to the BO3
                stream = new FileInputStream(path);
                // Get the tag
                metadata = Tag.readFrom(stream, false);
                stream.close();
            } catch (IOException corruptFile)
            {
                TerrainControl.log(Level.SEVERE, "Failed to read NBT meta file: {0}", e.getMessage());
                TerrainControl.printStackTrace(Level.SEVERE, corruptFile);
                tryToClose(stream);
                return null;
            }
        }

        // The file can be structured in two ways:
        // 1. chest.nbt with all the contents directly in it
        // 2. chest.nbt with a Compound tag in it with all the data

        // Check for type 1 by searching for an id tag
        Tag[] values = (Tag[]) metadata.getValue();
        for (Tag subTag : values)
        {
            if (subTag.getName() != null && subTag.getName().equals("id") && subTag.getType().equals(Tag.Type.TAG_String))
            {
                // Found id tag, so return the root tag
                return metadata;
            }
        }
        // No id tag found, so check for type 2
        try
        {
            return registerMetadata(path, ((Tag[]) metadata.getValue())[0]);
        } catch (Exception e)
        {
            TerrainControl.log(Level.WARNING, "Structure of NBT file is incorrect: {0}", e.getMessage());
            return null;
        }

    }

    /**
     * Caches and returns the provided Meta data
     * <p/>
     * @param pathOnDisk The path of the meta data
     * @param metadata   The Tag object to be cached
     * <p/>
     * @return the meta data that was cached
     */
    public static Tag registerMetadata(String pathOnDisk, Tag metadata)
    {
        // Add it to the cache
        loadedTags.put(pathOnDisk, metadata);
        // Return it
        return metadata;
    }

    private static void tryToClose(InputStream stream)
    {
        if (stream != null)
        {
            try
            {
                stream.close();
            } catch (IOException ignored)
            {
                // Ignore
            }
        }
    }

    @Override
    public void onShutdown()
    {
        // Clean up the cache
        loadedTags.clear();
    }

}
