package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.List;
import java.util.Random;

public class VinesGen extends Resource
{
    private int minAltitude;
    private int maxAltitude;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int _x = x;
        int _z = z;
        int y = minAltitude;

        while (y < maxAltitude)
        {
            if (world.isEmpty(_x, y, _z))
            {
                for (int direction = 2; direction <= 5; direction++)
                    if (canPlace(world, _x, y, _z, direction))
                    {
                        world.setBlock(_x, y, _z, DefaultMaterial.VINE.id, 1 << d[OPPOSITE_FACING[direction]]);
                        break;
                    }
            } else
            {
                _x = x + rand.nextInt(4) - rand.nextInt(4);
                _z = z + rand.nextInt(4) - rand.nextInt(4);
            }
            y++;
        }

    }

    public boolean canPlace(LocalWorld world, int x, int y, int z, int paramInt4)
    {
        int id;
        switch (paramInt4)
        {
            default:
                return false;
            case 1:
                id = (world.getTypeId(x, y + 1, z));
                break;
            case 2:
                id = (world.getTypeId(x, y, z + 1));
                break;
            case 3:
                id = (world.getTypeId(x, y, z - 1));
                break;
            case 5:
                id = (world.getTypeId(x - 1, y, z));
                break;
            case 4:
                id = (world.getTypeId(x + 1, y, z));
                break;
        }
        return DefaultMaterial.getMaterial(id).isSolid();
    }

    public static final int[] d = {-1, -1, 2, 0, 1, 3};
    public static final int[] OPPOSITE_FACING = {1, 0, 3, 2, 5, 4};

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        blockId = DefaultMaterial.VINE.id; // Hardcoded for now

        assureSize(4, args);
        frequency = readInt(args.get(0), 1, 100);
        rarity = readInt(args.get(1), 1, 100);
        minAltitude = readInt(args.get(2), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = readInt(args.get(3), minAltitude + 1, TerrainControl.worldHeight);
    }

    @Override
    public String makeString()
    {
        return "Vines(" + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }
}
