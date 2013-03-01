package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.configuration.Tag.Type;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BO3Loader implements CustomObjectLoader
{
    // A list of already loaded meta Tags. The path is the key, a NBT Tag
    // is the value.
    private static Map<String, Tag> loadedTags = new HashMap<String, Tag>();

    public BO3Loader()
    {
        // Register BO3 ConfigFunctions
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("Block", BlockFunction.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("Branch", BranchFunction.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("RandomBlock", RandomBlockFunction.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("BlockCheck", BlockCheck.class);
        TerrainControl.getConfigFunctionsManager().registerConfigFunction("LightCheck", LightCheck.class);
    }

    public CustomObject loadFromFile(String objectName, File file)
    {
        return new BO3(objectName, file);
    }

    @SuppressWarnings("resource") // Actually, we use tryToClose(..) to close the stream
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
            TerrainControl.log(Level.WARNING, "NBT file " + path + " not found");
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
                TerrainControl.log(Level.SEVERE, "Failed to read NBT meta file: " + e.getMessage());
                e.printStackTrace();
                tryToClose(stream);
                return null;
            }
        }

        // The file can be structured in two ways:
        // 1. chest.nbt with all the contents direclty in it
        // 2. chest.nbt with a Compound tag in it with all the data

        // Check for type 1 by searching for an id tag

        Tag idTag = metadata.findTagByName("id");
        if (idTag == null || !idTag.getType().equals(Type.TAG_String))
        {
            // Not found, search for type 2
            try
            {
                metadata = ((Tag[]) metadata.getValue())[0];
            } catch (Exception e)
            {
                TerrainControl.log(Level.WARNING, "Structure of NBT file is incorrect: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        // Add it to the cache
        loadedTags.put(path, metadata);
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
