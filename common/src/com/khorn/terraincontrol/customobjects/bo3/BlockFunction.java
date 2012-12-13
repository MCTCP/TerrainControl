package com.khorn.terraincontrol.customobjects.bo3;

import java.util.ArrayList;
import java.util.List;

import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.exception.InvalidResourceException;
import com.khorn.terraincontrol.util.BlockHelper;

public class BlockFunction extends BO3Function
{
    public int blockId;
    public int blockData;
    public int x;
    public int y;
    public int z;

    public List<Tag> metaDataTags;
    public List<String> metaDataNames;
    public List<Integer> metaDataChances;

    @Override
    public void load(List<String> args) throws InvalidResourceException
    {
        assureSize(4, args);
        blockId = getBlockId(args.get(0));
        blockData = getBlockData(args.get(0));
        x = getInt(args.get(1), -100, 100);
        y = getInt(args.get(2), -100, 100);
        z = getInt(args.get(3), -100, 100);
        metaDataTags = new ArrayList<Tag>();
        metaDataChances = new ArrayList<Integer>();
        metaDataNames = new ArrayList<String>();
        for (int i = 4; i < args.size() - 1; i += 2)
        {
            Tag tag = BO3Loader.loadMetadata(args.get(i), getHolder().file);
            if (tag != null)
            {
                metaDataTags.add(tag);
                metaDataNames.add(args.get(i));
                metaDataChances.add(getInt(args.get(i + 1), 1, 100));
            }
        }
    }

    @Override
    public String makeString()
    {
        String start = "Block(" + makeMaterial(blockId, blockData) + "," + x + "," + y + "," + z;
        for(int i = 0; i < metaDataNames.size(); i++)
        {
            start += "," + metaDataNames.get(i) + "," + metaDataChances.get(i);
        }
        return start + ")";
    }

    /**
     * Returns a new BlockFunction that is rotated 90 degrees.
     * <p />
     * Note: the metadata has a magical link: if you change it on the rotated
     * one, it also changes on the original and vice versa.
     * 
     * @return A new BlockFunction that is rotated 90 degrees.
     */
    public BlockFunction rotate()
    {
        BlockFunction block = new BlockFunction();
        block.x = z;
        block.y = y;
        block.z = -x;
        block.blockId = blockId;
        block.blockData = BlockHelper.RotateData(blockId, blockData);
        block.metaDataTags = metaDataTags;
        block.metaDataNames = metaDataNames;
        block.metaDataChances = metaDataChances;

        return block;
    }

}
