package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public static Tag loadMetadata(String name, File bo3File)
    {
        String path = bo3File.getParent() + File.separator + name;

        if (loadedTags.containsKey(path))
        {
            // Found a cached one
            return loadedTags.get(path);
        }

        try
        {
            // Read it from a file next to the BO3
            FileInputStream stream = new FileInputStream(path);
            // Get the first tag in the file
            Tag metadata = ((Tag[]) Tag.readFrom(stream).getValue())[0];
            stream.close();
            // Add it to the cache
            loadedTags.put(path, metadata);
            // Return it
            return metadata;
        } catch (FileNotFoundException e)
        {
            TerrainControl.log("Failed to load NBT meta file: " + e.getMessage());
            return null;
        } catch (IOException e)
        {
            TerrainControl.log(Level.SEVERE, "Failed to read NBT meta file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onShutdown()
    {
        // Clean up the cache
        loadedTags.clear();
    }
}
