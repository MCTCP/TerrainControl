package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bo3.BO3Loader;
import com.pg85.otg.customobjects.customstructure.CustomObjectCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.NamedBinaryTag;
import com.pg85.otg.util.Rotation;

import java.util.List;
import java.util.Random;

public class RandomBlockFunction extends BlockFunction
{
    public LocalMaterialData[] blocks;
    public byte[] blockChances;
    public String[] metaDataNames;
    public NamedBinaryTag[] metaDataTags;

    public int blockCount = 0;

    public RandomBlockFunction()
    {
    	super();
	}

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

        // The arrays are a little bit too large, just to be sure <-- TODO: Why do this? Remove this?
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
    public String makeString()
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

    public BlockFunction rotate(Rotation rotation)
    {
    	RandomBlockFunction rotatedBlock = new RandomBlockFunction();

        CustomObjectCoordinate rotatedCoords = CustomObjectCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.blocks = blocks;

        if(material != null)
        {
        	throw new RuntimeException();
        }

    	// TODO: This makes no sense, why is rotation inverted??? Should be: NORTH:0,WEST:1,SOUTH:2,EAST:3
        LocalMaterialData[] rotatedBlockBlocks = new LocalMaterialData[blockCount];
        for (int a = 0; a < blockCount; a++)
        {
        	rotatedBlockBlocks[a] = rotatedBlock.blocks[a];

		    // Apply rotation
			if(rotation.getRotationId() == 3)
			{
				rotatedBlockBlocks[a] = rotatedBlockBlocks[a].rotate();
			}
			if(rotation.getRotationId() == 2)
			{
				rotatedBlockBlocks[a] = rotatedBlockBlocks[a].rotate();
				rotatedBlockBlocks[a] = rotatedBlockBlocks[a].rotate();
			}
			if(rotation.getRotationId() == 1)
			{
				rotatedBlockBlocks[a] = rotatedBlockBlocks[a].rotate();
				rotatedBlockBlocks[a] = rotatedBlockBlocks[a].rotate();
				rotatedBlockBlocks[a] = rotatedBlockBlocks[a].rotate();
			}
        }
        rotatedBlock.blocks = rotatedBlockBlocks;

    	rotatedBlock.blockCount = blockCount;
    	rotatedBlock.blockChances = blockChances;
        rotatedBlock.metaDataTag = metaDataTag;
        rotatedBlock.metaDataTags = metaDataTags;
        rotatedBlock.metaDataName = metaDataName;
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
