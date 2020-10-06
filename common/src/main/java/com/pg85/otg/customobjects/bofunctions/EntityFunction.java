package com.pg85.otg.customobjects.bofunctions;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFile;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.minecraft.defaults.EntityNames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Represents an entity in a BO3.
 */
public abstract class EntityFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T>
{
    public int y;

    public String name = "";
    public int groupSize = 1;
    public String nameTagOrNBTFileName = "";
    public String originalNameTagOrNBTFileName = "";
    public String resourceLocation = "";

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
		x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        processEntityName(args.get(3));
        groupSize = readInt(args.get(4), 0, Integer.MAX_VALUE);

        if(args.size() > 5)
        {
    		originalNameTagOrNBTFileName = args.get(5);
        }

        if(originalNameTagOrNBTFileName != null && originalNameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt"))
        {
        	nameTagOrNBTFileName = getHolder().getFile().getParentFile().getAbsolutePath() + File.separator + originalNameTagOrNBTFileName;
        }
    }

    public void processEntityName(String name) {
        // When loading from file, it will contain either a mob name or a resource location.
        // If a mob name, we get the mob's vanilla resource location
        // If a resource location, we store it and extract a mob name from it
        if (name == null)
            return;
        if (name.contains(":")) {
            resourceLocation = name.toLowerCase().trim();
        } else {
            resourceLocation = EntityNames.toInternalName(name);
            if (!resourceLocation.contains(":")) {
                OTG.log(LogMarker.ERROR, "Could not find entity '"+name+"', are you sure you spelled it correctly?");
            }
        }
        this.name = resourceLocation.split(":")[1];
    }

    @Override
    public String makeString()
    {
        return "Entity(" + x + ',' + y + ',' + z + ',' + resourceLocation + ',' + groupSize + (originalNameTagOrNBTFileName != null && originalNameTagOrNBTFileName.length() > 0 ? ',' + originalNameTagOrNBTFileName : "") + ')';
    }

    private String metaDataTag;
    public String getMetaData()
    {
    	if(nameTagOrNBTFileName != null && nameTagOrNBTFileName.length() > 0 && metaDataTag == null)
    	{
    		File metaDataFile = new File(nameTagOrNBTFileName);
    		StringBuilder stringbuilder = new StringBuilder();
    	    if(metaDataFile.exists())
    	    {
    			try {
    				BufferedReader reader = new BufferedReader(new FileReader(metaDataFile));
    				try {
    					String line = reader.readLine();

    				    while (line != null) {
    				    	stringbuilder.append(line);
    				        //sb.append(System.lineSeparator());
    				        line = reader.readLine();
    				    }
    				} finally {
    					reader.close();
    				}
    			} catch (FileNotFoundException e1) {
    				e1.printStackTrace();
    			}
    			catch (IOException e1) {
    				e1.printStackTrace();
    			}
    	    }

            metaDataTag = stringbuilder.toString();
    	}
    	return metaDataTag;
    }

    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        EntityFunction<T> block = (EntityFunction<T>) other;
        return block.x == x && block.y == y && block.z == z && block.resourceLocation.equalsIgnoreCase(resourceLocation) && block.groupSize == groupSize && block.originalNameTagOrNBTFileName.equalsIgnoreCase(originalNameTagOrNBTFileName);
    }

	public abstract EntityFunction<T> createNewInstance();
}
