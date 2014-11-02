package com.khorn.terraincontrol.util.helpers;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;

import java.io.File;
import java.util.Collection;

/**
 * Methods for dealing with files and folders.
 *
 */
public final class FileHelper
{
    /**
     * Makes sure all the folders in the collection exist. Folders that don't
     * exist yet are created. Logs a message for each folder that could not be
     * created.
     * 
     * @param folders The folders that must exist.
     * @return True if all folders already existed or were created, false
     * otherwise. In other words: if this method returns true, you can be sure
     * now that all folders exist.
     */
    public static boolean makeFolders(Collection<File> folders)
    {
        boolean allFoldersExist = true;

        for (File directory : folders)
        {
            if (!makeFolder(directory))
            {
                allFoldersExist = false;
            }
        }

        return allFoldersExist;
    }

    /**
     * Makes sure the given folder exists. If it doesn't exist yet it is
     * created. If it could not be created a message is logged.
     * @param folder The folder that must exist.
     * @return True if the folder already existed or was created, false
     * otherwise. In other words: if this method returns true, you can be sure
     * now that this folder exists.
     */
    public static boolean makeFolder(File folder)
    {
        if (!folder.exists() && !folder.mkdirs())
        {
            TerrainControl.log(LogMarker.WARN, "Error creating directory \"{}\".", folder.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     * Migrates an old folder to a new folder. If the old folder does not
     * exist or is empty, no migration is needed. If the new folder does not
     * exist, or exists but is empty, the migration is performed.
     * @param oldFolder The old folder.
     * @param newFolder The new folder.
     * @return True if the migration was successful or wasn't needed, false if
     * the migration was needed but failed.
     */
    public static boolean migrateFolder(File oldFolder, File newFolder)
    {
        // Check if migration is needed
        if (!oldFolder.exists())
        {
            // Nothing to migrate
            return true;
        }
        if (oldFolder.delete())
        {
            // Only empty folders can be deleted, so when this succeeds,
            // there was nothing to migrate
            return true;
        }

        // Check if new location is suitable
        if (newFolder.exists())
        {
            if (!newFolder.delete())
            {
                // Only empty folders can be deleted, so when this fails there
                // are files in both the old and new folders
                // Migration failed
                return false;
            }
        }

        // Migrate
        return oldFolder.renameTo(newFolder);
    }

    private FileHelper()
    {
    }

}
