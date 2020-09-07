package com.pg85.otg.bukkit.util;

import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.sk89q.worldedit.bukkit.selections.Selection;

public abstract class BOCreator {
    protected String name;
    protected boolean includeAir = false;
    protected boolean includeTiles = false;
    protected String author;

    public abstract boolean create(Selection selection, String blockName, boolean branch);

    protected String getTileEntityName(NamedBinaryTag tag)
    {
        NamedBinaryTag idTag = tag.getTag("id");
        if (idTag != null)
        {
            String name = (String) idTag.getValue();

            return name.replace("minecraft:", "").replace(':', '_');
        }
        return "Unknown";
    }

    public void includeAir(boolean include)
    {
        this.includeAir = include;
    }

    public void includeTiles(boolean include)
    {
        this.includeTiles = include;
    }

    public void author(String name) {
        author = name;
    }
}
