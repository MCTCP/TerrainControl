package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

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
                        world.setBlock(_x, y, _z, TerrainControl.toLocalMaterialData(DefaultMaterial.VINE, 1 << d[OPPOSITE_FACING[direction]]));
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
        LocalMaterialData sourceBlock;
        switch (paramInt4)
        {
            default:
                return false;
            case 1:
                sourceBlock = world.getMaterial(x, y + 1, z);
                break;
            case 2:
                sourceBlock = world.getMaterial(x, y, z + 1);
                break;
            case 3:
                sourceBlock = world.getMaterial(x, y, z - 1);
                break;
            case 5:
                sourceBlock = world.getMaterial(x - 1, y, z);
                break;
            case 4:
                sourceBlock = world.getMaterial(x + 1, y, z);
                break;
        }
        return sourceBlock.isSolid();
    }
    public static final int[] d =
    {
        -1, -1, 2, 0, 1, 3
    };
    public static final int[] OPPOSITE_FACING =
    {
        1, 0, 3, 2, 5, 4
    };

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        material = TerrainControl.toLocalMaterialData(DefaultMaterial.VINE, 0);

        assureSize(4, args);
        frequency = readInt(args.get(0), 1, 100);
        rarity = readRarity(args.get(1));
        minAltitude = readInt(args.get(2), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        maxAltitude = readInt(args.get(3), minAltitude + 1, TerrainControl.WORLD_HEIGHT);
    }

    @Override
    public String makeString()
    {
        return "Vines(" + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final VinesGen compare = (VinesGen) other;
        return this.maxAltitude == compare.maxAltitude
               && this.minAltitude == compare.minAltitude;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + super.hashCode();
        hash = 17 * hash + this.minAltitude;
        hash = 17 * hash + this.maxAltitude;
        return hash;
    }

}
