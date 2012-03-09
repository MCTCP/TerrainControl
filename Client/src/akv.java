import com.Khorn.TerrainControl.Configuration.TCDefaultValues;
import com.Khorn.TerrainControl.Configuration.WorldConfig;

import java.io.*;
import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public abstract class akv
{
    //wz  - world


    public wz a;
    public vt b;
    public ro c;
    public boolean d = false;
    public boolean e = false;
    public float[] f = new float[16];
    public int g = 0;

    private SingleWorld world;

    private float[] h = new float[4];

    public final void a(wz paramwz)
    {
        this.a = paramwz;
        this.b = paramwz.B().t();

        SingleWorld.RestoreBiomes();

        TCClient.CheckWorld(this);

        if (this.b == vt.TerrainControl)
        {

            this.world = new SingleWorld(paramwz.x.j());
            //this.world.setHeight(paramwz.b());
            //this.world.setWaterLevel(paramwz.b()/2);

            File worldDir = null;
            try
            {
                Field dirField = ed.class.getDeclaredField("b");  // saveDir in SaveHandler

                dirField.setAccessible(true);

                worldDir = (File) dirField.get((ed)paramwz.A());


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
            this.world.Init(paramwz);

        }

        a();
        i();
    }

    public void InitTCBiomeManager(ChannelPacket packet)
    {

        if (!packet.a.equals(TCDefaultValues.ChannelName.stringValue()))
            return;

        this.world = new SingleWorld(this.a.x.j());
        try
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.c);
            DataInputStream stream = new DataInputStream(inputStream);
            WorldConfig config = new WorldConfig(stream, this.world);

            this.world.setSettings(config);
            this.world.InitM(this.a);

            //this.c = new BiomeManager(this.world);
            System.out.println("TerrainControl: config received");

        } catch (IOException e1)
        {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    protected void i() {
        float f1 = 0.0F;
        for (int i = 0; i <= 15; i++) {
            float f2 = 1.0F - i / 15.0F;
            this.f[i] = ((1.0F - f2) / (f2 * 3.0F + 1.0F) * (1.0F - f1) + f1);
        }
    }

    protected void a() {
        if (this.a.B().t() == vt.c)
            this.c = new fj(abi.c, 0.5F, 0.5F);
        else if (this.b == vt.TerrainControl)
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
                    this.c = new ro(this.a);
                    break;
            }
        } else
            this.c = new ro(this.a);
    }

    public bx b()
    {
        if (this.b == vt.c) {
            return new yd(this.a, this.a.v(), this.a.B().r());
        }
        else if (this.b == vt.TerrainControl)
        {
            if (this.world.getSettings().ModeTerrain != WorldConfig.TerrainMode.Default)
            {
                return this.world.getChunkGenerator();
            } else
                return new als(this.a, this.a.v(), this.a.B().r());
        } else
        return new als(this.a, this.a.v(), this.a.B().r());
    }

    public boolean a(int paramInt1, int paramInt2)
    {
        int i = this.a.b(paramInt1, paramInt2);

        return i == ox.u.bO;
    }

    public float a(long paramLong, float paramFloat)
    {
        int i = (int)(paramLong % 24000L);
        float f1 = (i + paramFloat) / 24000.0F - 0.25F;
        if (f1 < 0.0F) f1 += 1.0F;
        if (f1 > 1.0F) f1 -= 1.0F;
        float f2 = f1;
        f1 = 1.0F - (float)((Math.cos(f1 * 3.141592653589793D) + 1.0D) / 2.0D);
        f1 = f2 + (f1 - f2) / 3.0F;
        return f1;
    }

    public int b(long paramLong, float paramFloat) {
        return (int)(paramLong / 24000L) % 8;
    }

    public boolean e() {
        return true;
    }

    public float[] a(float paramFloat1, float paramFloat2)
    {
        float f1 = 0.4F;
        float f2 = gh.b(paramFloat1 * 3.141593F * 2.0F) - 0.0F;
        float f3 = -0.0F;
        if ((f2 >= f3 - f1) && (f2 <= f3 + f1)) {
            float f4 = (f2 - f3) / f1 * 0.5F + 0.5F;
            float f5 = 1.0F - (1.0F - gh.a(f4 * 3.141593F)) * 0.99F;
            f5 *= f5;
            this.h[0] = (f4 * 0.3F + 0.7F);
            this.h[1] = (f4 * f4 * 0.7F + 0.2F);
            this.h[2] = (f4 * f4 * 0.0F + 0.2F);
            this.h[3] = f5;
            return this.h;
        }

        return null;
    }

    public bm b(float paramFloat1, float paramFloat2) {
        float f1 = gh.b(paramFloat1 * 3.141593F * 2.0F) * 2.0F + 0.5F;
        if (f1 < 0.0F) f1 = 0.0F;
        if (f1 > 1.0F) f1 = 1.0F;

        if (this.world == null)
        {

            float f2 = 0.7529412F;    //r
            float f3 = 0.8470588F;    //g
            float f4 = 1.0F;          //b

            f2 *= (f1 * 0.94F + 0.06F);
            f3 *= (f1 * 0.94F + 0.06F);
            f4 *= (f1 * 0.91F + 0.09F);
            return bm.b(f2, f3, f4);
        } else
        {

            WorldConfig config = this.world.getSettings();

            float red = (config.WorldFogR*f1 + config.WorldNightFogR*(1-f1));
            float green = (config.WorldFogG*f1 + config.WorldNightFogG*(1-f1));
            float blue = (config.WorldFogB*f1 + config.WorldNightFogB*(1-f1));

            return bm.b(red, green, blue);

        }
    }

    public boolean d() {
        return true;
    }

    public static akv a(int paramInt) {
        if (paramInt == -1) return new ajz();
        if (paramInt == 0) return new mp();
        if (paramInt == 1)
            try
            {
                return (akv) Class.forName("if").newInstance();
            } catch (Exception e1)
            {
                e1.printStackTrace();
            }
        return null;
    }

    public float f() {
        return 128.0F;
    }

    public boolean c() {
        return true;
    }

    public ud g() {
        return null;
    }

    public int h() {
        if (this.b == vt.c) {
            return 4;
        }
        return 64;
    }

    public boolean j() {
        return (this.b != vt.c) && (!this.e);
    }

    public double k() {
        if (this.b == vt.c) {
            return 1.0D;
        }
        return 0.03125D;
    }

    public boolean b(int paramInt1, int paramInt2)
    {
        return false;
    }
}