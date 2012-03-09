@SuppressWarnings("ALL")
public class vt
{
    public static final vt[] a = new vt[16];

    public static final vt b = new vt(0, "default", 1).f();
    public static final vt c = new vt(1, "flat");
    public static final vt TerrainControl = new vt(4,"TerrainControl", 0);
    

    public static final vt d = new vt(8, "default_1_1", 0).a(false);
    private final String e;
    private final int f;
    private boolean Tc = false;
    private boolean g;
    private boolean h;

    private vt(int paramInt, String paramString)
    {
        this(paramInt, paramString, 0);
    }

    private vt(int paramInt1, String paramString, int paramInt2) {
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

    public vt a(int paramInt) {
        if ((this == b) && (paramInt == 0)) {
            return d;
        }
        return this;
    }

    private vt a(boolean paramBoolean) {
        this.g = paramBoolean;
        return this;
    }

    public boolean d() {
        return this.g;
    }

    private vt f() {
        this.h = true;
        return this;
    }

    public boolean e() {
        return this.h;
    }

    public static vt a(String paramString) {
        for (int i = 0; i < a.length; i++) {
            if ((a[i] != null) && (a[i].e.equalsIgnoreCase(paramString))) {
                return a[i];
            }
        }
        return null;
    }
}