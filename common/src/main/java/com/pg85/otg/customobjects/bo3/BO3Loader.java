package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunctionsManager;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectLoader;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.NamedBinaryTag;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BO3Loader implements CustomObjectLoader
{

    /** A list of already loaded meta Tags. The path is the key, a NBT Tag is
     * the value.
     */
    private static Map<String, NamedBinaryTag> loadedTags = new HashMap<String, NamedBinaryTag>();

    public BO3Loader()
    {
        // Register BO3 ConfigFunctions
        CustomObjectConfigFunctionsManager registry = OTG.getCustomObjectConfigFunctionsManager();
        registry.registerConfigFunction("Block", BlockFunction.class);
        registry.registerConfigFunction("B", BlockFunction.class);
        registry.registerConfigFunction("Branch", BranchFunction.class);
        registry.registerConfigFunction("BR", BranchFunction.class);
        registry.registerConfigFunction("WeightedBranch", WeightedBranchFunction.class);
        registry.registerConfigFunction("WBR", WeightedBranchFunction.class);
        registry.registerConfigFunction("RandomBlock", RandomBlockFunction.class);
        registry.registerConfigFunction("RB", RandomBlockFunction.class);
        registry.registerConfigFunction("MinecraftObject", MinecraftObjectFunction.class);
        registry.registerConfigFunction("MCO", MinecraftObjectFunction.class);
        registry.registerConfigFunction("BlockCheck", BlockCheck.class);
        registry.registerConfigFunction("BC", BlockCheck.class);
        registry.registerConfigFunction("BlockCheckNot", BlockCheckNot.class);
        registry.registerConfigFunction("BCN", BlockCheckNot.class);
        registry.registerConfigFunction("LightCheck", LightCheck.class);
        registry.registerConfigFunction("LC", LightCheck.class);
        registry.registerConfigFunction("Entity", EntityFunction.class);
        registry.registerConfigFunction("E", EntityFunction.class);
        registry.registerConfigFunction("Particle", ParticleFunction.class);
        registry.registerConfigFunction("P", ParticleFunction.class);
        registry.registerConfigFunction("Spawner", SpawnerFunction.class);
        registry.registerConfigFunction("S", SpawnerFunction.class);
        registry.registerConfigFunction("ModData", ModDataFunction.class);
        registry.registerConfigFunction("MD", ModDataFunction.class);
    }

    @Override
    public CustomObject loadFromFile(String objectName, File file)
    {
        return new BO3(objectName, file);
    }

    public static NamedBinaryTag loadMetadata(String name, File bo3Folder)
    {
        String path = bo3Folder.getParent() + File.separator + name;

        if (loadedTags.containsKey(path))
        {
            // Found a cached one
            return loadedTags.get(path);
        }

        NamedBinaryTag tag = loadTileEntityFromNBT(path);
        registerMetadata(path, tag);
        return tag;
    }

    private static NamedBinaryTag loadTileEntityFromNBT(String path)
    {
        // Load from file
        NamedBinaryTag metadata;
        FileInputStream stream = null;
        try
        {
            // Read it from a file next to the BO3
            stream = new FileInputStream(path);
            // Get the tag
            metadata = NamedBinaryTag.readFrom(stream, true);
        } catch (FileNotFoundException e)
        {
            // File not found
        	if(OTG.getPluginConfig().SpawnLog)
        	{
        		OTG.log(LogMarker.WARN, "NBT file {} not found", (Object) path);
        	}
            return null;
        } catch (IOException e)
        {
            tryToClose(stream);

            // Not a compressed NBT file, try uncompressed
            FileInputStream streamForUncompressed = null;
            try
            {
                // Read it from a file next to the BO3
                streamForUncompressed = new FileInputStream(path);
                // Get the tag
                metadata = NamedBinaryTag.readFrom(streamForUncompressed, false);
            }             
            catch (java.lang.ArrayIndexOutOfBoundsException corruptFile)
            {
            	if(OTG.getPluginConfig().SpawnLog)
            	{
	                OTG.log(LogMarker.ERROR, "Failed to read NBT meta file: ", e.getMessage());
	                OTG.printStackTrace(LogMarker.ERROR, corruptFile);
            	}
                return null;
            }
            catch (IOException corruptFile)
            {
            	if(OTG.getPluginConfig().SpawnLog)
            	{
	                OTG.log(LogMarker.ERROR, "Failed to read NBT meta file: ", e.getMessage());
	                OTG.printStackTrace(LogMarker.ERROR, corruptFile);
            	}
                return null;
            } finally
            {
                tryToClose(streamForUncompressed);
            }
        } finally
        {
            tryToClose(stream);
        }

        if(metadata != null)
        {
	        // The file can be structured in two ways:
	        // 1. chest.nbt with all the contents directly in it
	        // 2. chest.nbt with a Compound tag in it with all the data
	
	        // Check for type 1 by searching for an id tag
	        NamedBinaryTag idTag = metadata.getTag("id");
	        if (idTag != null)
	        {
	            // Found id tag, so return the root tag
	            return metadata;
	        }
	        // No id tag found, so check for type 2
	        if (metadata.getValue() instanceof NamedBinaryTag[])
	        {
	            NamedBinaryTag[] subtag = (NamedBinaryTag[]) metadata.getValue();
	            if (subtag.length != 0)
	            {
	                return subtag[0];
	            }
	        }
        }
        // Unknown/bad structure
        OTG.log(LogMarker.WARN, "Structure of NBT file is incorrect: " + path);
        return null;
    }

    /**
     * Caches and returns the provided Meta data
     * @param pathOnDisk The path of the meta data
     * @param metadata   The Tag object to be cached
     * @return the meta data that was cached
     */
    public static NamedBinaryTag registerMetadata(String pathOnDisk, NamedBinaryTag metadata)
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
