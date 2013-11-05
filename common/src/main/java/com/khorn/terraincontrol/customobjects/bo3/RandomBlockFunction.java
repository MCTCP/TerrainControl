package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.helpers.BlockHelper;

import java.util.List;
import java.util.Random;

public class RandomBlockFunction extends BlockFunction
{
    public int[] blockIds;
    public byte[] blockDatas;
    public byte[] blockChances;
    public String[] metaDataNames;
    public NamedBinaryTag[] metaDataTags;

    public int blockCount = 0;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -100, 100);
        z = readInt(args.get(2), -100, 100);

        // Now read the random parts
        int i = 3;
        int size = args.size();

        // The arrays are a little bit too large, just to be sure
        blockIds = new int[size / 2 + 1];
        blockDatas = new byte[size / 2 + 1];
        blockChances = new byte[size / 2 + 1];
        metaDataNames = new String[size / 2 + 1];
        metaDataTags = new NamedBinaryTag[size / 2 + 1];

        while (i < size)
        {
            // Parse block id and data
            blockIds[blockCount] = readBlockId(args.get(i));
            blockDatas[blockCount] = (byte) readBlockData(args.get(i));

            // Parse chance and metadata
            i++;
            try
            {
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
            } catch (InvalidConfigException e)
            {
                // Maybe it's a NBT file?

                // Get the file
                NamedBinaryTag metaData = BO3Loader.loadMetadata(args.get(i), this.getHolder().file);
                if (metaData != null)
                {
                    metaDataNames[blockCount] = args.get(i);
                    metaDataTags[blockCount] = metaData;
                }

                // Get the chance
                i++;
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
            }

            i++;
            blockCount++;
        }
    }

    @Override
    public String makeString()
    {
        String text = "RandomBlock(" + x + "," + y + "," + z;
        for (int i = 0; i < blockCount; i++)
        {
            if (metaDataTags[i] == null)
            {
                text += "," + makeMaterial(blockIds[i], blockDatas[i]) + "," + blockChances[i];
            } else
            {
                text += "," + makeMaterial(blockIds[i], blockDatas[i]) + "," + metaDataNames[i] + "," + blockChances[i];
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
        rotatedBlock.blockIds = blockIds;
        rotatedBlock.blockDatas = new byte[blockCount];
        for (int i = 0; i < blockCount; i++)
        {
            rotatedBlock.blockDatas[i] = (byte) BlockHelper.rotateData(blockIds[i], blockDatas[i]);
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
                world.setBlock(x, y, z, blockIds[i], blockDatas[i]);
                if (metaDataTags[i] != null)
                {
                    world.attachMetadata(x, y, z, metaDataTags[i]);
                }
                break;
            }
        }
    }
}
