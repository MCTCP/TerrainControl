package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.MathHelper;
import net.minecraft.server.World;

public class OreGen extends ResourceGenBase
{


    public OreGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {

        int y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        float f = this.rand.nextFloat() * 3.141593F;

        double d1 = x + 8 + MathHelper.sin(f) * res.Size / 8.0F;
        double d2 = x + 8 - MathHelper.sin(f) * res.Size / 8.0F;
        double d3 = z + 8 + MathHelper.cos(f) * res.Size / 8.0F;
        double d4 = z + 8 - MathHelper.cos(f) * res.Size / 8.0F;

        double d5 = y + this.rand.nextInt(3) - 2;
        double d6 = y + this.rand.nextInt(3) - 2;

        for (int i = 0; i <= res.Size; i++)
        {
            double d7 = d1 + (d2 - d1) * i / res.Size;
            double d8 = d5 + (d6 - d5) * i / res.Size;
            double d9 = d3 + (d4 - d3) * i / res.Size;

            double d10 = this.rand.nextDouble() * res.Size / 16.0D;
            double d11 = (MathHelper.sin(i * 3.141593F / res.Size) + 1.0F) * d10 + 1.0D;
            double d12 = (MathHelper.sin(i * 3.141593F / res.Size) + 1.0F) * d10 + 1.0D;

            int j = MathHelper.floor(d7 - d11 / 2.0D);
            int k = MathHelper.floor(d8 - d12 / 2.0D);
            int m = MathHelper.floor(d9 - d11 / 2.0D);

            int n = MathHelper.floor(d7 + d11 / 2.0D);
            int i1 = MathHelper.floor(d8 + d12 / 2.0D);
            int i2 = MathHelper.floor(d9 + d11 / 2.0D);

            for (int i3 = j; i3 <= n; i3++)
            {
                double d13 = (i3 + 0.5D - d7) / (d11 / 2.0D);
                if (d13 * d13 < 1.0D)
                {
                    for (int i4 = k; i4 <= i1; i4++)
                    {
                        double d14 = (i4 + 0.5D - d8) / (d12 / 2.0D);
                        if (d13 * d13 + d14 * d14 < 1.0D)
                        {
                            for (int i5 = m; i5 <= i2; i5++)
                            {
                                double d15 = (i5 + 0.5D - d9) / (d11 / 2.0D);
                                if ((d13 * d13 + d14 * d14 + d15 * d15 < 1.0D) && res.CheckSourceId(this.GetRawBlockId(i3, i4, i5)))
                                    this.SetRawBlockId(i3, i4, i5, res.BlockId);
                            }
                        }
                    }
                }

            }

        }
    }


}
