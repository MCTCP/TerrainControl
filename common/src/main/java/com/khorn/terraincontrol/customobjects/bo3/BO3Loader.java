package com.khorn.terraincontrol.customobjects.bo3;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFunctionsManager;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.NamedBinaryTag.Type;

import java.io.*;
import java.nio.file.Files;
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
        ConfigFunctionsManager registry = TerrainControl.getConfigFunctionsManager();
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
        //registry.registerConfigFunction("Particle", ParticleFunction.class);
        //registry.registerConfigFunction("P", ParticleFunction.class);
        //registry.registerConfigFunction("Spawner", SpawnerFunction.class);
        //registry.registerConfigFunction("S", SpawnerFunction.class);
        //registry.registerConfigFunction("ModData", ModDataFunction.class);
        //registry.registerConfigFunction("MD", ModDataFunction.class);
    }

    @Override
    public CustomObject loadFromFile(String objectName, File file)
    {
        return new BO3(objectName, file);
    }

    public static NamedBinaryTag loadMetadata(String name, File bo3Folder)
    {
        if (name.startsWith("{"))
        {
            // Assume inline NBT
            return loadNBTFromString(name);
        }
        if (name.endsWith(".txt") || name.endsWith(".dat") || name.endsWith(".nbt"))
        {
            // Assume file name
            String filePath = bo3Folder + File.separator + name;
            return loadedTags.computeIfAbsent(filePath, BO3Loader::loadNBTFromFile);
        }
        if (name.length() < 30)
        {
            // Assume display name
            return getDisplayNameTag(name);
        }

        TerrainControl.log(LogMarker.WARN, "Invalid NBT specification: {}", name);
        return null;
    }

    private static NamedBinaryTag getDisplayNameTag(String name)
    {
        NamedBinaryTag customName = new NamedBinaryTag(Type.TAG_String, "CustomName", name);
        NamedBinaryTag customNameVisible = new NamedBinaryTag(Type.TAG_Byte, "CustomNameVisible", (byte) 1);
        return new NamedBinaryTag(Type.TAG_Compound, "", new NamedBinaryTag[] {customName, customNameVisible});
    }

    private static NamedBinaryTag loadNBTFromFile(String filePath)
    {
        File file = new File(filePath);

        if (file.getName().endsWith(".txt"))
        {
            return loadNBTFromTextFile(file);
        } else
        {
            return loadNBTFromBinaryFile(file);
        }
    }

    private static NamedBinaryTag loadNBTFromString(String string)
    {
        try
        {
            return TerrainControl.getEngine().toNamedBinaryTag(string);
        } catch (InvalidConfigException e)
        {
            TerrainControl.log(LogMarker.WARN, "Could not parse as NBT: {}", string);
            return null;
        }
    }

    private static NamedBinaryTag loadNBTFromTextFile(File file)
    {
        try
        {
            String fileContents = Files.lines(file.toPath(), UTF_8).collect(joining());
            return loadNBTFromString(fileContents);
        } catch (IOException | UncheckedIOException e)
        {
            TerrainControl.log(LogMarker.WARN, "Could not read file " + file + ": " + e.getLocalizedMessage());
            return null;
        }
    }

    private static NamedBinaryTag loadNBTFromBinaryFile(File path)
    {
        // Load from file
        try (FileInputStream stream = new FileInputStream(path))
        {
            // Get the tag
            return findCorrectTag(NamedBinaryTag.readFrom(stream, true));
        } catch (FileNotFoundException e)
        {
            // File not found
            TerrainControl.log(LogMarker.WARN, "NBT file {} not found", (Object) path);
            return null;
        } catch (IOException e)
        {
            // Not a compressed NBT file, try uncompressed
            try (FileInputStream streamForUncompressed = new FileInputStream(path))
            {
                // Get the tag
                NamedBinaryTag found = findCorrectTag(NamedBinaryTag.readFrom(streamForUncompressed, false));
                streamForUncompressed.close();  // Fix Stream not being closed after completely.
                return found;
            } catch (IOException corruptFile)
            {
                TerrainControl.log(LogMarker.FATAL, "Failed to read NBT meta file: ", e.getMessage());
                TerrainControl.printStackTrace(LogMarker.FATAL, corruptFile);
                return null;
            }
        }
    }

    private static NamedBinaryTag findCorrectTag(NamedBinaryTag rootTag)
    {
        if (rootTag == null)
            return null;

        // The file can be structured in two ways:
        // 1. chest.nbt with all the contents directly in it
        // 2. chest.nbt with a Compound tag in it with all the data

        // Check for type 1 by searching for an id tag
        NamedBinaryTag idTag = rootTag.getTag("id");
        if (idTag != null)
        {
            // Found id tag, so return the root tag
            return rootTag;
        }
        // No id tag found, so check for type 2
        if (rootTag.getValue() instanceof NamedBinaryTag[])
        {
            NamedBinaryTag[] subtags = (NamedBinaryTag[]) rootTag.getValue();
            if (subtags.length != 0)
            {
                return subtags[0];
            }
        }
        // Unknown/bad structure
        TerrainControl.log(LogMarker.WARN, "Structure of NBT file is incorrect.");
        return null;
    }

    @Override
    public void onShutdown()
    {
        // Clean up the cache
        loadedTags.clear();
    }

}
