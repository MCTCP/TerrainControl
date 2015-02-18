package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings;
import com.khorn.terraincontrol.customobjects.bo3.BlockFunction;
import com.khorn.terraincontrol.util.MaterialSet;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 */
public class ObjectExtrusionHelper
{
    private int                     extrusionLevelCurrent;
    private BO3Settings.ExtendStyle extendStyle;
    private MaterialSet             extrudeThroughBlocks;
    private ArrayList<BlockFunction> extrusionBlocks = new ArrayList<BlockFunction>();

    public ObjectExtrusionHelper(BO3Settings.ExtendStyle extendStyle, MaterialSet extrudeThroughBlocks)
    {
        this.extendStyle = extendStyle;
        this.extrudeThroughBlocks = extrudeThroughBlocks;
        if (extendStyle == BO3Settings.ExtendStyle.BottomDown)
        {
            extrusionLevelCurrent = 256;
        } else
        {
            extrusionLevelCurrent = 0;
        }
    }

    public void checkAndAdd(BlockFunction block)
    {
        if (extendStyle != BO3Settings.ExtendStyle.None)
        {
            if (extendStyle == BO3Settings.ExtendStyle.BottomDown && block.y < extrusionLevelCurrent)
            {
                extrusionBlocks.clear();
                extrusionLevelCurrent = block.y;
            } else if (extendStyle == BO3Settings.ExtendStyle.TopUp && block.y > extrusionLevelCurrent)
            {
                extrusionBlocks.clear();
                extrusionLevelCurrent = block.y;
            }
            if (block.y == extrusionLevelCurrent)
            {
                extrusionBlocks.add(block);
            }
        }
    }

    public void extrude(LocalWorld world, Random random, int x, int y, int z)
    {
        for (BlockFunction block : extrusionBlocks)
        {
            if (extendStyle == BO3Settings.ExtendStyle.BottomDown)
            {
                for (int yi = y + block.y - 1; yi > 0; --yi)
                {
                    if (extrudeThroughBlocks.contains(world.getMaterial(x + block.x, yi, z + block.z)))
                    {
                        block.spawn(world, random, x + block.x, yi, z + block.z);
                    } else
                    {
                        break;
                    }
                }
            } else if (extendStyle == BO3Settings.ExtendStyle.TopUp)
            {
                for (int yi = y + block.y + 1; yi < 255; ++yi)
                {
                    if (extrudeThroughBlocks.contains(world.getMaterial(x + block.x, yi, z + block.z)))
                    {
                        block.spawn(world, random, x + block.x, yi, z + block.z);
                    } else
                    {
                        break;
                    }
                }
            }
        }
    }


}
