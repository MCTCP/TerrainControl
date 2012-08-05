import java.util.List;

@SuppressWarnings("ALL")
public class mk
{
    private uo a;
    private boolean b = false;
    private int c = -1;
    private int d;
    private int e;
    private mi f;
    private int g;
    private int h;
    private int i;

    public mk(uo paramuo)
    {
        this.a = paramuo;
        mod_TerrainControl.NewWorldCreated( paramuo);
    }

    public void a()
    {
        if (this.a.r())
        {
            this.c = 0;
            return;
        }

        if (this.c == 2)
            return;

        if (this.c == 0)
        {
            float f1 = this.a.c(0.0F);
            if ((f1 < 0.5D) || (f1 > 0.501D))
                return;
            this.c = (this.a.v.nextInt(10) == 0 ? 1 : 2);
            this.b = false;
            if (this.c == 2)
                return;
        }

        if (!this.b)
        {
            if (b())
                this.b = true;
            else
            {
                return;
            }
        }

        if (this.e > 0)
        {
            this.e -= 1;
            return;
        }

        this.e = 2;
        if (this.d > 0)
        {
            c();
            this.d -= 1;
        } else
        {
            this.c = 2;
        }
    }

    private boolean b()
    {
        List<of> localList = this.a.i;
        for (of localof : localList)
        {
            this.f = this.a.D.a((int) localof.t, (int) localof.u, (int) localof.v, 1);
            if ((this.f == null) ||
                    (this.f.c() < 10) ||
                    (this.f.d() < 20) ||
                    (this.f.e() < 20))
            {
                continue;
            }
            j localj = this.f.a();
            float f1 = this.f.b();

            int j = 0;
            for (int k = 0; k < 10; k++)
            {
                this.g = (localj.a + (int) (ig.b(this.a.v.nextFloat() * 3.141593F * 2.0F) * f1 * 0.9D));
                this.h = localj.b;
                this.i = (localj.c + (int) (ig.a(this.a.v.nextFloat() * 3.141593F * 2.0F) * f1 * 0.9D));
                j = 0;
                List<mi> localMiList = this.a.D.b();
                for (mi localmi : localMiList)
                {
                    if (localmi != this.f)
                        if (localmi.a(this.g, this.h, this.i))
                        {
                            j = 1;
                            break;
                        }
                }
                if (j == 0)
                    break;
            }
            if (j != 0)
                return false;

            ajr localajr = a(this.g, this.h, this.i);
            if (localajr == null)
                continue;
            this.e = 0;
            this.d = 20;
            return true;
        }
        return false;
    }

    private boolean c()
    {
        ajr localajr = a(this.g, this.h, this.i);
        if (localajr == null)
            return false;
        nz localnz;
        try
        {
            localnz = new nz(this.a);
        } catch (Exception localException)
        {
            localException.printStackTrace();
            return false;
        }
        localnz.b(localajr.a, localajr.b, localajr.c, this.a.v.nextFloat() * 360.0F, 0.0F);
        this.a.d(localnz);
        j localj = this.f.a();
        localnz.b(localj.a, localj.b, localj.c, this.f.b());
        return true;
    }

    private ajr a(int paramInt1, int paramInt2, int paramInt3)
    {
        for (int j = 0; j < 10; j++)
        {
            int k = paramInt1 + this.a.v.nextInt(16) - 8;
            int m = paramInt2 + this.a.v.nextInt(6) - 3;
            int n = paramInt3 + this.a.v.nextInt(16) - 8;
            if ((this.f.a(k, m, n)) && (vb.a(jw.a, this.a, k, m, n)))
                return ajr.a().a(k, m, n);
        }
        return null;
    }
}