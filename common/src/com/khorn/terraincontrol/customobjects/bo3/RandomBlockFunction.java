package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.BlockHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBlockFunction extends BlockFunction
{
    public List<Integer> blockIds = new ArrayList<Integer>();
    public List<Byte> blockDatas = new ArrayList<Byte>();
    public List<Byte> blockChances = new ArrayList<Byte>();
    public List<String> metaDataNames = new ArrayList<String>();
    public List<Tag> metaDataTags = new ArrayList<Tag>();

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
        int groupNumber = 0;

        while (i < size)
        {
            // Parse block id and data
            blockIds.add(readBlockId(args.get(i)));
            blockDatas.add((byte) readBlockData(args.get(i)));

            // Parse chance and metadata
            i++;
            try
            {
                blockChances.add((byte) readInt(args.get(i), 1, 100));
                // If it can read the number at the position, it doesn't have
                // metadata
                metaDataNames.add("");
                metaDataTags.add(null);
            } catch (InvalidConfigException e)
            {
                // Maybe it's a NBT file?

                // Get the file
                Tag metaData = BO3Loader.loadMetadata(args.get(i), this.getHolder().file);
                if (metaData != null)
                {
                    metaDataNames.add(args.get(i));
                    metaDataTags.add(metaData);
                }

                // Get the chance
                i++;
                blockChances.add(groupNumber, (byte) readInt(args.get(i), 1, 100));
            }

            i++;
        }
    }

    @Override
    public String makeString()
    {
        String text = "RandomBlock(" + x + "," + y + "," + z;
        for (int i = 0; i < blockIds.size(); i++)
        {
            if (metaDataTags.get(i) == null)
            {
                text += "," + makeMaterial(blockIds.get(i), blockDatas.get(i)) + "," + blockChances.get(i);
            } else
            {
                text += "," + makeMaterial(blockIds.get(i), blockDatas.get(i)) + "," + metaDataNames.get(i) + "," + blockChances.get(i);
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
        rotatedBlock.blockIds = blockIds;
        rotatedBlock.blockDatas = new ArrayList<Byte>(blockIds.size());
        for (int i = 0; i < blockDatas.size(); i++)
        {
            rotatedBlock.blockDatas.add((byte) BlockHelper.rotateData(blockIds.get(i), blockDatas.get(i)));
        }
        rotatedBlock.metaDataTags = metaDataTags;
        rotatedBlock.metaDataNames = metaDataNames;

        return rotatedBlock;
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        for (int i = 0; i < blockChances.size(); i++)
        {
            if (random.nextInt(100) < blockChances.get(i))
            {
                world.setBlock(x, y, z, blockIds.get(i), blockDatas.get(i));
                if (metaDataTags.get(i) != null)
                {
                    world.attachMetadata(x, y, z, metaDataTags.get(i));
                }
                break;
            }
        }
    }
}
