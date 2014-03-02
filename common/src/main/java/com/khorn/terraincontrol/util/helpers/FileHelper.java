package com.khorn.terraincontrol.util.helpers;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;

import java.io.File;
import java.util.Collection;

public class FileHelper
{
    /**
     * Makes sure all the folders in the collection exist. Folders that don't
     * exist yet are created. Logs a message for each folder that could not be
     * created.
     * 
     * @param folders The folders that must exist.
     * @return True if all folders were created, false otherwise.
     */
    public static boolean makeFolders(Collection<File> folders)
    {
        boolean allFoldersExist = true;

        for (File directory : folders)
        {
            if (!directory.exists() && !directory.mkdirs())
            {
                TerrainControl.log(LogMarker.WARN, "Error creating directory \"{}\".", new Object[] {directory.getAbsolutePath()});
                allFoldersExist = false;
            }
        }

        return allFoldersExist;
    }

    private FileHelper()
    {
    }

}
