package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;

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
    public EntityFunction(BO3Config holder) {
        super(holder);
        // TODO Auto-generated constructor stub
    }

    public int x;
    public int y;
    public int z;

    public String mobName = "";
    public int groupSize = 1;
    public String nameTagOrNBTFileName = "";
    public String originalNameTagOrNBTFileName = "";
    public String nameTag = "";

    public EntityFunction(BO3Config config, List<String> args) throws InvalidConfigException
    {
        super(config);
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
            nameTagOrNBTFileName = args.get(5);
            originalNameTagOrNBTFileName = nameTagOrNBTFileName;
        }

        if(nameTagOrNBTFileName != null && nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt"))
        {
            nameTagOrNBTFileName = getHolder().directory.getAbsolutePath() + File.separator + nameTagOrNBTFileName;
        }
    }

    @Override
    public String toString()
    {
        return "Entity(" + x + ',' + y + ',' + z + ',' + mobName + ',' + groupSize + (originalNameTagOrNBTFileName != null && originalNameTagOrNBTFileName.length() > 0 ? ',' + originalNameTagOrNBTFileName : "") + ')';
    }

    @Override
    public EntityFunction rotate()
    {
        EntityFunction rotatedBlock = new EntityFunction(getHolder());
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.mobName = mobName;
        rotatedBlock.groupSize = groupSize;
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
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            metaDataTag = stringbuilder.toString();
        }
        return metaDataTag;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        EntityFunction block = (EntityFunction) other;
        return block.x == x && block.y == y && block.z == z && block.mobName.equalsIgnoreCase(mobName) && block.groupSize == groupSize && block.nameTagOrNBTFileName == nameTagOrNBTFileName;
    }
}