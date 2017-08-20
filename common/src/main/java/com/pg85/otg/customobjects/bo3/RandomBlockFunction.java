package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.LocalMaterialData;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.configuration.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.NamedBinaryTag;

import java.util.List;
import java.util.Random;

public class RandomBlockFunction extends BlockFunction
{
    LocalMaterialData[] blocks;
    byte[] blockChances;
    String[] metaDataNames;
    NamedBinaryTag[] metaDataTags;

    public int blockCount = 0;

    public RandomBlockFunction()
    {
    	super();
	}    
    
    public RandomBlockFunction(BO3Config config, List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -100, 100);
        z = readInt(args.get(2), -100, 100);

        // Now read the random parts
        int i = 3;
        int size = args.size();

        // The arrays are a little bit too large, just to be sure
        blocks = new LocalMaterialData[size / 2 + 1];
        blockChances = new byte[size / 2 + 1];
        metaDataNames = new String[size / 2 + 1];
        metaDataTags = new NamedBinaryTag[size / 2 + 1];

        while (i < size)
        {
            // Parse block id and data
            blocks[blockCount] = readMaterial(args.get(i));

            // Parse chance and metadata
            i++;
            if (i >= size)
            {
                throw new InvalidConfigException("Missing chance parameter");
            }
            try
            {
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
            } catch (InvalidConfigException e)
            {
                // Maybe it's a NBT file?

                // Get the file
                NamedBinaryTag metaData = BO3Loader.loadMetadata(args.get(i), this.getHolder().getFile());
                if (metaData != null)
                {
                    metaDataNames[blockCount] = args.get(i);
                    metaDataTags[blockCount] = metaData;
                }

                // Get the chance
                i++;
                if (i >= size)
                {
                    throw new InvalidConfigException("Missing chance parameter");
                }
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
            }

            i++;
            blockCount++;
        }
    }

    @Override
    public String toString()
    {
        String text = "RandomBlock(" + x + "," + y + "," + z;
        for (int i = 0; i < blockCount; i++)
        {
            if (metaDataTags[i] == null)
            {
                text += "," + blocks[i] + "," + blockChances[i];
            } else
            {
                text += "," + blocks[i] + "," + metaDataNames[i] + "," + blockChances[i];
            }
        }
        return text + ")";
    }

    @Override
    public RandomBlockFunction rotate()
    {
        RandomBlockFunction rotatedBlock = new RandomBlockFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.blockCount = blockCount;
        rotatedBlock.blocks = new LocalMaterialData[blockCount];
        for (int i = 0; i < blockCount; i++)
        {
            rotatedBlock.blocks[i] = blocks[i].rotate();
        }
        rotatedBlock.blockChances = blockChances;
        rotatedBlock.metaDataTags = metaDataTags;
        rotatedBlock.metaDataNames = metaDataNames;

        return rotatedBlock;
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z, boolean allowOutsidePopulatingArea)
    {
        for (int i = 0; i < blockCount; i++)
        {
            if (random.nextInt(100) < blockChances[i])
            {
                world.setBlock(x, y, z, blocks[i], metaDataTags[i], allowOutsidePopulatingArea);
                break;
            }
        }
    }

    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<BO3Config> other)
    {
        if (!getClass().equals(other.getClass()))
        {
            return false;
        }
        RandomBlockFunction block = (RandomBlockFunction) other;
        return block.x == x && block.y == y && block.z == z;
    }    
}
