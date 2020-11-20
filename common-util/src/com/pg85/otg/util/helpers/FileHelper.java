package com.pg85.otg.util.helpers;

import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;

import java.io.File;
import java.nio.file.Path;
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
    public static boolean makeFolders(Collection<Path> folders, ILogger logger)
    {
        boolean allFoldersExist = true;

        for (Path directory : folders)
        {
            if (!makeFolder(directory, logger))
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
    private static boolean makeFolder(Path folderPath, ILogger logger)
    {
    	File folder = folderPath.toFile();
        if (!folder.exists() && !folder.mkdirs())
        {
        	logger.log(LogMarker.ERROR, "Error creating directory \"{}\".", folder.getAbsolutePath());
            return false;
        }
        return true;
    }

    private FileHelper() { }
}
