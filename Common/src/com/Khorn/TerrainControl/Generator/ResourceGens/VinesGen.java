package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import com.Khorn.TerrainControl.DefaultMaterial;
import com.Khorn.TerrainControl.LocalWorld;

import java.util.Random;

public class VinesGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int _x = x;
        int _z = z;
        int y = res.MinAltitude;

        while (y < res.MaxAltitude)
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
        return id != 0 && DefaultMaterial.getMaterial(id).isSolid();
    }


    public static final int[] d = {-1, -1, 2, 0, 1, 3};
    public static final int[] OPPOSITE_FACING = {1, 0, 3, 2, 5, 4};

}
