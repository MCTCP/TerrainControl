import java.util.List;

@SuppressWarnings("ALL")
public class adq
{
    private xd a;
    private boolean b = false;
    private int c = -1;
    private int d;
    private int e;
    private kd f;
    private int g;
    private int h;
    private int i;

    public adq(xd paramxd)
    {
        this.a = paramxd;
        mod_TerrainControl.NewWorldCreated( paramxd);
    }

    public void a() {
        if (this.a.m()) {
            this.c = 0;
            return;
        }

        if (this.c == 2) return;

        if (this.c == 0) {
            float f1 = this.a.c(0.0F);
            if ((f1 < 0.5D) || (f1 > 0.501D)) return;
            this.c = (this.a.r.nextInt(10) == 0 ? 1 : 2);
            this.b = false;
            if (this.c == 2) return;
        }

        if (!this.b) {
            if (b()) this.b = true; else {
                return;
            }
        }

        if (this.e > 0) {
            this.e -= 1;
            return;
        }

        this.e = 2;
        if (this.d > 0) {
            c();
            this.d -= 1;
        } else {
            this.c = 2;
        }
    }

    private boolean b()
    {
        List<yw> localList = this.a.d;
        for (yw localyw : localList) {
            this.f = this.a.A.a((int)localyw.o, (int)localyw.p, (int)localyw.q, 1);
            if ((this.f == null) ||
                    (this.f.c() < 10) ||
                    (this.f.d() < 20) ||
                    (this.f.e() < 20)) {
                continue;
            }
            uh localuh = this.f.a();
            float f1 = this.f.b();

            int j = 0;
            for (int k = 0; k < 10; k++) {
                this.g = (localuh.a + (int)(gk.b(this.a.r.nextFloat() * 3.141593F * 2.0F) * f1 * 0.9D));
                this.h = localuh.b;
                this.i = (localuh.c + (int)(gk.a(this.a.r.nextFloat() * 3.141593F * 2.0F) * f1 * 0.9D));
                j = 0;
                List<kd> localList2 = this.a.A.b();
                for (kd localkd : localList2) {
                    if (localkd != this.f)
                        if (localkd.a(this.g, this.h, this.i)) {
                            j = 1;
                            break;
                        }
                }
                if (j == 0) break;
            }
            if (j != 0) return false;

            bo localbo = a(this.g, this.h, this.i);
            if (localbo == null)
                continue;
            this.e = 0;
            this.d = 20;
            return true;
        }
        return false;
    }

    private boolean c() {
        bo localbo = a(this.g, this.h, this.i);
        if (localbo == null) return false; ajg localajg;
        try {
            localajg = new ajg(this.a);
        } catch (Exception localException) {
            localException.printStackTrace();
            return false;
        }
        localajg.c(localbo.a, localbo.b, localbo.c, this.a.r.nextFloat() * 360.0F, 0.0F);
        this.a.a(localajg);
        uh localuh = this.f.a();
        localajg.b(localuh.a, localuh.b, localuh.c, this.f.b());
        return true;
    }

    private bo a(int paramInt1, int paramInt2, int paramInt3) {
        for (int j = 0; j < 10; j++) {
            int k = paramInt1 + this.a.r.nextInt(16) - 8;
            int m = paramInt2 + this.a.r.nextInt(6) - 3;
            int n = paramInt3 + this.a.r.nextInt(16) - 8;
            if ((this.f.a(k, m, n)) &&
                    (vf.a(acf.a, this.a, k, m, n))) return bo.b(k, m, n);
        }
        return null;
    }
}