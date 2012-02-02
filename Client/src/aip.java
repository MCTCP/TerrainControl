import com.Khorn.TerrainControl.Configuration.TCDefaultValues;
import com.Khorn.TerrainControl.Configuration.WorldConfig;

import java.io.*;
import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public abstract class aip
{
    //world
    public vq a;
    //world type
    public um b;
    public qu c;
    public boolean d = false;
    public boolean e = false;
    public boolean f = false;    // nosky ?
    public float[] g = new float[16];
    public int h = 0;

    private SingleWorld world;

    private float[] i = new float[4];

    public final void a(vq paramvq)
    {
        this.a = paramvq;
        this.b = paramvq.z().t();
        SingleWorld.RestoreBiomes();

        TCClient.CheckWorld(this);

        if (this.b == um.TerrainControl)
        {

            this.world = new SingleWorld(paramvq.C.j());

            File worldDir = null;
            try
            {
                Field dirField = eb.class.getDeclaredField("b");

                dirField.setAccessible(true);

                worldDir = (File) dirField.get(paramvq.B);


            } catch (NoSuchFieldException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            File configDir = new File(worldDir, "TerrainControl");

            if (!configDir.exists())
            {
                System.out.println("TerrainControl: settings does not exist, creating defaults");

                if (!configDir.mkdirs())
                    System.out.println("TerrainControl: cant create folder " + configDir.getName());
            }

            WorldConfig config = new WorldConfig(configDir, world, false);
            this.world.setSettings(config);
            this.world.Init(paramvq);

        }

        a();
        h();
    }

    public void InitTCBiomeManager(ChannelPacket packet)
    {

        if (!packet.a.equals(TCDefaultValues.ChannelName.stringValue()))
            return;

        this.world = new SingleWorld(this.a.C.j());
        try
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.c);
            DataInputStream stream = new DataInputStream(inputStream);
            WorldConfig config = new WorldConfig(stream, this.world);

            this.world.setSettings(config);
            this.world.InitM(this.a);

            this.c = new BiomeManager(this.world);
            System.out.println("TerrainControl: config received");

        } catch (IOException e1)
        {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    protected void h()
    {
        float f1 = 0.0F;
        for (int j = 0; j <= 15; j++)
        {
            float f2 = 1.0F - j / 15.0F;
            this.g[j] = ((1.0F - f2) / (f2 * 3.0F + 1.0F) * (1.0F - f1) + f1);
        }
    }

    // Biome manager
    protected void a()
    {
        if (this.a.z().t() == um.b)
            this.c = new ff(zp.c, 0.5F, 0.5F);
        else if (this.b == um.TerrainControl)
        {
            switch (this.world.getSettings().ModeBiome)
            {

                case Normal:
                    this.c = new BiomeManager(this.world);
                    this.world.setBiomeManager((BiomeManager) this.c);
                    break;
                case OldGenerator:
                    this.c = new BiomeManagerOld(this.world);
                    this.world.setOldBiomeManager((BiomeManagerOld) this.c);
                    break;
                case Default:
                    this.c = new qu(this.a);
                    break;
            }
        } else
            this.c = new qu(this.a);
    }

    //Chunk & object manager
    public bs b()
    {
        if (this.b == um.b)
        {
            return new wo(this.a, this.a.t(), this.a.z().r());
        } else if (this.b == um.TerrainControl)
        {
            if (this.world.getSettings().ModeTerrain != WorldConfig.TerrainMode.Default)
            {
                return this.world.getChunkGenerator();
            } else
                return new aji(this.a, this.a.t(), this.a.z().r());
        } else
            return new aji(this.a, this.a.t(), this.a.z().r());
    }

    public boolean a(int paramInt1, int paramInt2)
    {
        int j = this.a.a(paramInt1, paramInt2);

        return j == oe.w.bO;
    }

    public float a(long paramLong, float paramFloat)
    {
        int j = (int) (paramLong % 24000L);
        float f1 = (j + paramFloat) / 24000.0F - 0.25F;
        if (f1 < 0.0F)
            f1 += 1.0F;
        if (f1 > 1.0F)
            f1 -= 1.0F;
        float f2 = f1;
        f1 = 1.0F - (float) ((Math.cos(f1 * 3.141592653589793D) + 1.0D) / 2.0D);
        f1 = f2 + (f1 - f2) / 3.0F;
        return f1;
    }

    public int b(long paramLong, float paramFloat)
    {
        return (int) (paramLong / 24000L) % 8;
    }

    public float[] a(float paramFloat1, float paramFloat2)
    {
        float f1 = 0.4F;
        float f2 = ga.b(paramFloat1 * 3.141593F * 2.0F) - 0.0F;
        float f3 = -0.0F;
        if ((f2 >= f3 - f1) && (f2 <= f3 + f1))
        {
            float f4 = (f2 - f3) / f1 * 0.5F + 0.5F;
            float f5 = 1.0F - (1.0F - ga.a(f4 * 3.141593F)) * 0.99F;
            f5 *= f5;
            this.i[0] = (f4 * 0.3F + 0.7F);
            this.i[1] = (f4 * f4 * 0.7F + 0.2F);
            this.i[2] = (f4 * f4 * 0.0F + 0.2F);
            this.i[3] = f5;
            return this.i;
        }

        return null;
    }

    public bk b(float paramFloat1, float paramFloat2)
    {
        float f1 = ga.b(paramFloat1 * 3.141593F * 2.0F) * 2.0F + 0.5F;
        if (f1 < 0.0F)
            f1 = 0.0F;
        if (f1 > 1.0F)
            f1 = 1.0F;

        if (this.world == null)
        {

            float f2 = 0.7529412F;    //r
            float f3 = 0.8470588F;    //g
            float f4 = 1.0F;          //b

            f2 *= (f1 * 0.94F + 0.06F);
            f3 *= (f1 * 0.94F + 0.06F);
            f4 *= (f1 * 0.91F + 0.09F);
            return bk.b(f2, f3, f4);
        } else
        {

            WorldConfig config = this.world.getSettings();

            float red = (config.WorldFogR*f1 + config.WorldNightFogR*(1-f1));
            float green = (config.WorldFogG*f1 + config.WorldNightFogG*(1-f1));
            float blue = (config.WorldFogB*f1 + config.WorldNightFogB*(1-f1));

            return bk.b(red, green, blue);

        }


    }

    public boolean d()
    {
        return true;
    }

    public static aip a(int paramInt)
    {
        if (paramInt == -1)
            return new ahv();
        if (paramInt == 0)
            return new ma();
        if (paramInt == 1)
            return new hv();
        return null;
    }

    public float e()
    {
        return this.a.c;
    }

    public boolean c()
    {
        return true;
    }

    public td f()
    {
        return null;
    }

    public int g()
    {
        if (this.b == um.b)
        {
            return 4;
        }
        return this.a.c / 2;
    }

    public boolean i()
    {
        return (this.b != um.b) && (!this.f);
    }

    public double j()
    {
        if (this.b == um.b)
        {
            return 1.0D;
        }
        return 0.03125D;
    }
}