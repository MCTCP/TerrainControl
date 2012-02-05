import com.Khorn.TerrainControl.Configuration.BiomeConfig;


public class CustomBiome extends zp
{
    private int skyColor;
    private int grassColor;
    private int foliageColor;

    private boolean grassColorSet = false;
    private boolean foliageColorSet = false;

    public CustomBiome(int id, String name)
    {
        super(id);
        this.a(name);

    }

    public void SetBiome(BiomeConfig config)
    {

        this.B = config.BiomeHeight;
        this.C = config.BiomeVolatility;
        this.y = config.SurfaceBlock;
        this.z = config.GroundBlock;
        this.D = config.BiomeTemperature;
        this.E = config.BiomeWetness;
        this.F = config.WaterColor;
        this.skyColor = config.SkyColor;
        this.grassColor = config.GrassColor;
        this.foliageColor = config.FoliageColor;

        if (this.grassColor != 0xffffff)
            this.grassColorSet = true;

        if (this.foliageColor != 0xffffff)
            this.foliageColorSet = true;

        // color ?
        this.x = 522674;

        // duno.
        this.A = 9154376;


    }

    public void CopyBiome(zp baseBiome)
    {
        this.B = baseBiome.B;
        this.C = baseBiome.C;
        this.y = baseBiome.y;
        this.z = baseBiome.z;
        this.D = baseBiome.D;
        this.E = baseBiome.E;
        this.F = baseBiome.F;


        this.I = baseBiome.I;
        this.H = baseBiome.H;
        this.J = baseBiome.J;

    }


    // Sky color from Temp
    @Override
    public int a(float v)
    {
        return this.skyColor;
    }


    // getGrassColorAtCoords
    public int a(aiw paramaiw, int paramInt1, int paramInt2, int paramInt3)
    {
        if (!this.grassColorSet)
            return super.a(paramaiw, paramInt1, paramInt2, paramInt3);
        double d1 = paramaiw.a().a(paramInt1, paramInt2, paramInt3);
        double d2 = paramaiw.a().b(paramInt1, paramInt3);

        return ((gk.a(d1, d2)& 0xFEFEFE) + this.grassColor) / 2;
    }

    // getFoliageColorAtCoords
    public int b(aiw paramaiw, int paramInt1, int paramInt2, int paramInt3)
    {
        if (!this.foliageColorSet)
            return super.b(paramaiw, paramInt1, paramInt2, paramInt3);
        double d1 = paramaiw.a().a(paramInt1, paramInt2, paramInt3);
        double d2 = paramaiw.a().b(paramInt1, paramInt3);

        return ((gk.a(d1, d2)& 0xFEFEFE)  + this.foliageColor) / 2;
    }
}
