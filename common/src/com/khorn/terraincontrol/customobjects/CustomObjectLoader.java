package com.khorn.terraincontrol.customobjects;

import java.io.File;

public interface CustomObjectLoader
{
    /**
     * Returns a CustomObject with the given name and file. The object shouldn't yet be initialisized.
     *
     * @param objectName
     * @param file
     * @return
     */
    public CustomObject loadFromFile(String objectName, File file);

    /**
     * Called whenever Terrain Control is being shut down / reloaded.
     */
    public void onShutdown();
}
