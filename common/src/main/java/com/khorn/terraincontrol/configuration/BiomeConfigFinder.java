package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.configuration.io.SimpleSettingsMap;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class searches for the appropriate file for each biome.
 * You give it a list of folders to search in, and it will find the location of
 * all BiomeConfigs. Files will be created for non-existing BiomeConfigs.
 * 
 */
public final class BiomeConfigFinder
{
    /**
     * A stub for a {@link BiomeConfig}. At this stage, the raw settings are
     * already loaded. Setting reading must not start before the inheritance
     * settings are processed.
     */
    public final class BiomeConfigStub
    {
        private final SettingsMap settings;
        private final File file;
        private final BiomeLoadInstruction loadInstructions;
        public boolean biomeExtendsProcessed = false;

        private BiomeConfigStub(SettingsMap settings, File file, BiomeLoadInstruction loadInstructions)
        {
            super();
            this.settings = settings;
            this.file = file;
            this.loadInstructions = loadInstructions;
        }

        /**
         * Gets the file the biome is stored in.
         * @return The file.
         */
        public File getFile()
        {
            return file;
        }

        /**
         * Gets the instructions used for loading the biome.
         * @return The instructions.
         */
        public BiomeLoadInstruction getLoadInstructions()
        {
            return loadInstructions;
        }

        /**
         * Gets the settings for the biome.
         * @return The settings.
         */
        public SettingsMap getSettings()
        {
            return settings;
        }

        /**
         * Gets the name of this biome.
         * @return The name.
         */
        public String getBiomeName()
        {
            return loadInstructions.getBiomeName();
        }
    }

    private final String preferredBiomeFileExtension;

    /**
     * Constructs a new biome loader.
     * 
     * @param worldConfig The world config, passed to created the biome
     *            configs.
     * @param preferredBiomeFileExtension Biome files that do not exist yet
     *            are created with this extension.
     */
    public BiomeConfigFinder(WorldConfig worldConfig, String preferredBiomeFileExtension)
    {
        this.preferredBiomeFileExtension = preferredBiomeFileExtension;
    }

    /**
     * Finds the biomes in the given directories.
     * 
     * @param directories The directories to search in.
     * @param biomesToLoad The biomes to load.
     *
     * @return A map of biome name --> location on disk.
     */
    public Map<String, BiomeConfigStub> findBiomes(Collection<File> directories, Collection<BiomeLoadInstruction> biomesToLoad)
    {
        Map<String, BiomeConfigStub> biomeConfigsStore = new HashMap<String, BiomeConfigStub>();

        // Switch to a Map<String, LocalBiome>
        Map<String, BiomeLoadInstruction> remainingBiomes = new HashMap<String, BiomeLoadInstruction>();
        for (BiomeLoadInstruction biome : biomesToLoad)
        {
            remainingBiomes.put(biome.getBiomeName(), biome);
        }

        // Search all directories
        for (File directory : directories)
        {
            // Account for the possibility that folder creation failed
            if (directory.exists())
            {
                loadBiomesFromDirectory(biomeConfigsStore, directory, remainingBiomes);
            }
        }

        // Create all biomes that weren't loaded
        File preferredDirectory = directories.iterator().next();
        for (BiomeLoadInstruction localBiome : remainingBiomes.values())
        {
            File newConfigFile = new File(preferredDirectory, toFileName(localBiome));
            boolean isNewConfig = true; // no file exists yet
            SettingsMap settings = new SimpleSettingsMap(localBiome.getBiomeName(), isNewConfig);
            BiomeConfigStub biomeConfigStub = new BiomeConfigStub(settings, newConfigFile, localBiome);
            biomeConfigsStore.put(localBiome.getBiomeName(), biomeConfigStub);
        }

        return biomeConfigsStore;
    }

    /**
     * Loads the biomes from the given directory.
     * 
     * @param biomeConfigsStore Map to store all the found biome configs in.
     * @param directory         The directory to load from.
     * @param remainingBiomes   The biomes that should still be loaded. When a
     *                          biome is found, it is removed from this map.
     */
    private void loadBiomesFromDirectory(Map<String, BiomeConfigStub> biomeConfigsStore, File directory, Map<String, BiomeLoadInstruction> remainingBiomes)
    {
        for (File file : directory.listFiles())
        {
            // Search recursively
            if (file.isDirectory())
            {
                loadBiomesFromDirectory(biomeConfigsStore, file, remainingBiomes);
                continue;
            }

            // Extract name from filename
            String biomeName = toBiomeName(file);
            if (biomeName == null)
            {
                // Not a valid biome file
                continue;
            }

            // Get the correct LocalBiome
            BiomeLoadInstruction biome = remainingBiomes.get(biomeName);
            if (biome == null)
            {
                // Doesn't need to be loaded. Maybe it's in both the global
                // and world folder, maybe it isn't
                // registered in the WorldConfig
                continue;
            }

            // Load biome and remove it from the todo list
            File renamedFile = renameBiomeFile(file, biome);
            SettingsMap settings = FileSettingsReader.read(biomeName, renamedFile);
            BiomeConfigStub biomeConfigStub = new BiomeConfigStub(settings, file, biome);
            biomeConfigsStore.put(biomeName, biomeConfigStub);
            remainingBiomes.remove(biome.getBiomeName());
        }
    }

    /**
     * Tries to rename the config file so that it has the correct extension.
     * Does nothing if the config file already has the correct extension. If
     * the rename fails, a message is printed.
     * 
     * @param toRename The file that should be renamed.
     * @param biome The biome that the file has settings for.
     * @return The renamed file.
     */
    private File renameBiomeFile(File toRename, BiomeLoadInstruction biome)
    {
        String preferredFileName = toFileName(biome);
        if (toRename.getName().equalsIgnoreCase(preferredFileName))
        {
            // No need to rename
            return toRename;
        }

        // Wrong extension, rename
        File newFile = new File(toRename.getParentFile(), preferredFileName);
        if (toRename.renameTo(newFile))
        {
            return newFile;
        } else
        {
            TerrainControl.log(LogMarker.INFO, "Failed to rename biome file {} to {}",
                    new Object[] {toRename.getAbsolutePath(), newFile.getAbsolutePath()});
            return toRename;
        }
    }

    /**
     * Extracts the biome name out of the file name.
     * 
     * @param file The file to extract the biome name out of.
     * @return The biome name, or null if the file is not a biome config file.
     */
    private String toBiomeName(File file)
    {
        String fileName = file.getName();
        for (String extension : BiomeStandardValues.BiomeConfigExtensions)
        {
            if (fileName.endsWith(extension))
            {
                String biomeName = fileName.substring(0, fileName.lastIndexOf(extension));
                return biomeName;
            }
        }

        // Invalid file name
        return null;
    }

    /**
     * Gets the name of the file the biome should be saved in. This will use
     * the extension as defined in the PluginConfig.ini file.
     * 
     * @param biome The biome.
     * @return The name of the file the biome should be saved in.
     */
    private String toFileName(BiomeLoadInstruction biome)
    {
        return biome.getBiomeName() + this.preferredBiomeFileExtension;
    }

}
