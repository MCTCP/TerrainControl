package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
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
        this.x = readInt(args.get(0), -100, 100);
        this.y = readInt(args.get(1), -100, 100);
        this.z = readInt(args.get(2), -100, 100);

        // Now read the random parts
        int i = 3;
        int size = args.size();

        // The arrays are a little bit too large, just to be sure
        this.blocks = new LocalMaterialData[size / 2 + 1];
        this.blockChances = new byte[size / 2 + 1];
        this.metaDataNames = new String[size / 2 + 1];
        this.metaDataTags = new NamedBinaryTag[size / 2 + 1];

        while (i < size)
        {
            // Parse block id and data
            this.blocks[blockCount] = readMaterial(args.get(i));

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
                NamedBinaryTag metaData = BO3Loader.loadMetadata(args.get(i), this.getHolder().directory);
                if (metaData != null)
                {
                    this.metaDataNames[blockCount] = args.get(i);
                    this.metaDataTags[blockCount] = metaData;
                }

                // Get the chance
                i++;
                if (i >= size)
                {
                    throw new InvalidConfigException("Missing chance parameter");
                }
                this.blockChances[this.blockCount] = (byte) readInt(args.get(i), 1, 100);
            }

            i++;
            this.blockCount++;
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
        for (int i = 0; i < this.blockCount; i++)
        {
            if (this.metaDataTags[i] == null)
            {
                text += "," + this.blocks[i] + "," + this.blockChances[i];
            } else
            {
                text += "," + this.blocks[i] + "," + this.metaDataNames[i] + "," + this.blockChances[i];
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
        rotatedBlock.blockCount = this.blockCount;
        rotatedBlock.blocks = new LocalMaterialData[this.blockCount];
        for (int i = 0; i < this.blockCount; i++)
        {
            rotatedBlock.blocks[i] = this.blocks[i].rotate();
        }
        rotatedBlock.blockChances = this.blockChances;
        rotatedBlock.metaDataTags = this.metaDataTags;
        rotatedBlock.metaDataNames = this.metaDataNames;

        return rotatedBlock;
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        for (int i = 0; i < this.blockCount; i++)
        {
            if (random.nextInt(100) < this.blockChances[i])
            {
                world.setBlock(x, y, z, this.blocks[i]);
                if (this.metaDataTags[i] != null)
                {
                    world.attachMetadata(x, y, z, this.metaDataTags[i]);
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
        return block.x == this.x && block.y == this.y && block.z == this.z;
    }
}
