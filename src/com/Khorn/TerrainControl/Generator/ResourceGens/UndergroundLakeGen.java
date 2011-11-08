package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.Block;
import net.minecraft.server.MathHelper;
import net.minecraft.server.World;

public class UndergroundLakeGen extends ResourceGenBase
{
    public UndergroundLakeGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {
        int y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MaxAltitude;

        if (y >= this.world.getHighestBlockYAt(x, z))
            return;
        int size = this.rand.nextInt(res.MaxSize - res.MinSize) + res.MinSize;

        float mPi = this.rand.nextFloat() * 3.141593F;

        double x1 = x + 8 + MathHelper.sin(mPi) * size / 8.0F;
        double x2 = x + 8 - MathHelper.sin(mPi) * size / 8.0F;
        double z1 = z + 8 + MathHelper.cos(mPi) * size / 8.0F;
        double z2 = z + 8 - MathHelper.cos(mPi) * size / 8.0F;

        double y1 = y + this.rand.nextInt(3) + 2;
        double y2 = y + this.rand.nextInt(3) + 2;

        for (int i = 0; i <= size; i++)
        {
            double xAdjusted = x1 + (x2 - x1) * i / size;
            double yAdjusted = y1 + (y2 - y1) * i / size;
            double zAdjusted = z1 + (z2 - z1) * i / size;

            double horizontalSizeMultiplier = this.rand.nextDouble() * size / 16.0D;
            double verticalSizeMultiplier = this.rand.nextDouble() * size / 32.0D;
            double horizontalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * horizontalSizeMultiplier + 1.0D;
            double verticalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * verticalSizeMultiplier + 1.0D;

            for (int xLake = (int) (xAdjusted - horizontalSize / 2.0D); xLake <= (int) (xAdjusted + horizontalSize / 2.0D); xLake++)
                for (int yLake = (int) (yAdjusted - verticalSize / 2.0D); yLake <= (int) (yAdjusted + verticalSize / 2.0D); yLake++)
                    for (int zLake = (int) (zAdjusted - horizontalSize / 2.0D); zLake <= (int) (zAdjusted + horizontalSize / 2.0D); zLake++)
                    {
                        if (this.GetRawBlockId(xLake, yLake, zLake) == 0)
                            continue;
                        double xBounds = (xLake + 0.5D - xAdjusted) / (horizontalSize / 2.0D);
                        double yBounds = (yLake + 0.5D - yAdjusted) / (verticalSize / 2.0D);
                        double zBounds = (zLake + 0.5D - zAdjusted) / (horizontalSize / 2.0D);
                        if (xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= 1.0D)
                            continue;
                        int uBlock = this.GetRawBlockId(xLake, yLake - 1, zLake);
                        if (uBlock != 0) // not air
                            this.SetRawBlockId(xLake, yLake, zLake, Block.WATER.id);
                        else
                            this.SetRawBlockId(xLake, yLake, zLake, 0); // Air block
                    }
        }
    }
}
