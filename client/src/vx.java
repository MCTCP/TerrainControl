@SuppressWarnings("ALL")
public class vx
{
    public static final vx[] a = new vx[16];

    public static final vx b = new vx(0, "default", 1).f();
    public static final vx c = new vx(1, "flat");
    public static final vx TerrainControl = new vx(4,"TerrainControl", 0);
    

    public static final vx d = new vx(8, "default_1_1", 0).a(false);
    private final String e;
    private final int f;
    private boolean Tc = false;
    private boolean g;
    private boolean h;

    private vx(int paramInt, String paramString)
    {
        this(paramInt, paramString, 0);
    }

    private vx(int paramInt1, String paramString, int paramInt2) {
        this.e = paramString;
        this.f = paramInt2;
        this.g = true;
        if (paramString.equals("TerrainControl"))
            this.Tc = true;
        a[paramInt1] = this;
    }

    public String a() {
        return this.e;
    }

    public String b() {
        if (this.Tc)
            return this.e;
        return "generator." + this.e;
    }

    public int c() {
        return this.f;
    }

    public vx a(int paramInt) {
        if ((this == b) && (paramInt == 0)) {
            return d;
        }
        return this;
    }

    private vx a(boolean paramBoolean) {
        this.g = paramBoolean;
        return this;
    }

    public boolean d() {
        return this.g;
    }

    private vx f() {
        this.h = true;
        return this;
    }

    public boolean e() {
        return this.h;
    }

    public static vx a(String paramString) {
        for (int i = 0; i < a.length; i++) {
            if ((a[i] != null) && (a[i].e.equalsIgnoreCase(paramString))) {
                return a[i];
            }
        }
        return null;
    }
}