package com.pg85.otg.customobject.bo3.bo3function;

import java.util.List;
import java.util.Random;

import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bo3.BO3Loader;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;

public class BO3RandomBlockFunction extends BO3BlockFunction
{
    public LocalMaterialData[] blocks;
    public byte[] blockChances;
    public String[] metaDataNames;
    public NamedBinaryTag[] metaDataTags;

    public byte blockCount = 0;
	
    public BO3RandomBlockFunction() { }
    
    public BO3RandomBlockFunction(BO3Config holder)
    {
    	super(holder);
    }
    
    public BO3RandomBlockFunction rotate()
    {
        BO3RandomBlockFunction rotatedBlock = new BO3RandomBlockFunction();
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
    public void load(List<String> args, boolean spawnLog, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        assureSize(5, args);
        x = readInt(args.get(0), -100, 100);
        y = (short) readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);

        // Now read the random parts
        int i = 3;
        int size = args.size();

        // Get number of blocks first, params can vary so can't just count.
        while (i < size)
        {           
        	i++;
            if (i >= size)
            {
                throw new InvalidConfigException("Missing chance parameter");
            }
            try
            {
                readInt(args.get(i), 1, 100);
            }
            catch (InvalidConfigException e)
            {
                // Get the chance
                i++;
                if (i >= size)
                {
                    throw new InvalidConfigException("Missing chance parameter");
                }
                readInt(args.get(i), 1, 100);
            }
            i++;
            blockCount++;
        }
        
        this.blocks = new LocalMaterialData[blockCount];
        this.blockChances = new byte[blockCount];
        this.metaDataNames = new String[blockCount];
        this.metaDataTags = new NamedBinaryTag[blockCount];
        
        i = 3;
        blockCount = 0;
        while (i < size)
        {
            // Parse chance and metadata
        	this.blocks[blockCount] = materialReader.readMaterial(args.get(i));
            i++;
            if (i >= size)
            {
                throw new InvalidConfigException("Missing chance parameter");
            }
            try
            {
                blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
            }
            catch (InvalidConfigException e)
            {
                // Maybe it's a NBT file?

                // Get the file
                NamedBinaryTag metaData = BO3Loader.loadMetadata(args.get(i), this.getHolder().getFile(), spawnLog, logger);
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
        String text = "RB(" + x + "," + y + "," + z;
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
    public void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, ChunkCoordinate chunkBeingPopulated, boolean replaceBlock)
    {
        for (int i = 0; i < blockCount; i++)
        {
            if (random.nextInt(100) < blockChances[i])
            {
            	worldGenRegion.setBlock(x, y, z, blocks[i], metaDataTags[i], chunkBeingPopulated, replaceBlock);
                break;
            }
        }
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }
}
