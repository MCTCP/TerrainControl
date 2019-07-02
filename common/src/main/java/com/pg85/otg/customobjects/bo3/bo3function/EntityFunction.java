package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.customstructure.CustomObjectCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.Rotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Represents an entity in a BO3.
 */
public class EntityFunction extends BO3Function
{
	public int x;
    public int y;
    public int z;

    public String mobName = "";
    public int groupSize = 1;
    public String nameTagOrNBTFileName = "";
    public String originalNameTagOrNBTFileName = "";

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
		x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        mobName = args.get(3);
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

    @Override
    public String makeString()
    {
        return "Entity(" + x + ',' + y + ',' + z + ',' + mobName + ',' + groupSize + (originalNameTagOrNBTFileName != null && originalNameTagOrNBTFileName.length() > 0 ? ',' + originalNameTagOrNBTFileName : "") + ')';
    }

    @Override
    public EntityFunction rotate()
    {
    	EntityFunction rotatedBlock = new EntityFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.mobName = mobName;
        rotatedBlock.groupSize = groupSize;
        rotatedBlock.originalNameTagOrNBTFileName = originalNameTagOrNBTFileName;
        rotatedBlock.nameTagOrNBTFileName = nameTagOrNBTFileName;

        return rotatedBlock;
    }

    public EntityFunction rotate(Rotation rotation)
    {
    	EntityFunction rotatedBlock = new EntityFunction();

        CustomObjectCoordinate rotatedCoords = CustomObjectCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.mobName = mobName;
        rotatedBlock.groupSize = groupSize;
        rotatedBlock.originalNameTagOrNBTFileName = originalNameTagOrNBTFileName;
        rotatedBlock.nameTagOrNBTFileName = nameTagOrNBTFileName;

        return rotatedBlock;
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
    public boolean isAnalogousTo(CustomObjectConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        EntityFunction block = (EntityFunction) other;
        return block.x == x && block.y == y && block.z == z && block.mobName.equalsIgnoreCase(mobName) && block.groupSize == groupSize && block.originalNameTagOrNBTFileName == originalNameTagOrNBTFileName;
    }
}
