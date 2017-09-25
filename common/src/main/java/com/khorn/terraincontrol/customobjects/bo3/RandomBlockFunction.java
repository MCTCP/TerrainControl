package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.NamedBinaryTag;

import java.util.List;
import java.util.Random;

public class RandomBlockFunction extends BO3PlaceableFunction
{
    public LocalMaterialData[] blocks;
    public byte[] blockChances;
    public String[] metaDataNames;
    public NamedBinaryTag[] metaDataTags;

    public int blockCount = 0;

    public RandomBlockFunction(BO3Config config, List<String> args) throws InvalidConfigException
    {
        super(config);
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
                if (args.get(i).isEmpty()) {
                    TerrainControl.log(LogMarker.ERROR, "Found empty RandomBlock argument in config {} with args {}.", config.getName(), args);
                } else {
                    // Maybe it's a NBT file?
    
                    // Get the file
                    NamedBinaryTag metaData = BO3Loader.loadMetadata(args.get(i), this.getHolder().directory);
                    if (metaData != null)
                    {
                        metaDataNames[blockCount] = args.get(i);
                        metaDataTags[blockCount] = metaData;
                    }
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

    private RandomBlockFunction(BO3Config config)
    {
        super(config);
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
        RandomBlockFunction rotatedBlock = new RandomBlockFunction(getHolder());
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
    public void spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        for (int i = 0; i < blockCount; i++)
        {
            if (random.nextInt(100) < blockChances[i])
            {
                world.setBlock(x, y, z, blocks[i]);
                if (metaDataTags[i] != null)
                {
                    world.attachMetadata(x, y, z, metaDataTags[i]);
                }
                break;
            }
        }
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BO3Config> other)
    {
        if (!getClass().equals(other.getClass()))
        {
            return false;
        }
        RandomBlockFunction block = (RandomBlockFunction) other;
        return block.x == x && block.y == y && block.z == z;
    }
}
