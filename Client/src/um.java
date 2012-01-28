@SuppressWarnings("ALL")
public enum um
{
    a("DEFAULT", 0, "default"),
    TerrainControl("TerrainControl", 0, "TerrainControl"),
    b("FLAT", 1, "flat");

    private String c;
    private boolean Tc = false;


    private um(String s, int i, String s1)
    {
        this.c = s1;
        if (s1.equals("TerrainControl"))
            this.Tc = true;
    }

    public String a()
    {
        if (this.Tc)
            return this.c;
        return "generator." + this.c;
    }

    public static um a(String s)
    {
        um aenumworldtype[] = values();
        int i = aenumworldtype.length;
        for (int j = 0; j < i; j++)
        {
            um enumworldtype = aenumworldtype[j];
            if (enumworldtype.name().equals(s))
            {
                return enumworldtype;
            }
        }

        return null;
    }
}