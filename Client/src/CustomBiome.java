import com.Khorn.TerrainControl.Configuration.BiomeConfig;


public class CustomBiome extends abi
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

        this.D = config.BiomeHeight;
        this.E = config.BiomeVolatility;
        this.A = config.SurfaceBlock;
        this.B = config.GroundBlock;
        this.F = config.BiomeTemperature;
        this.G = config.BiomeWetness;
        this.H = config.WaterColor;
        this.skyColor = config.SkyColor;
        this.grassColor = config.GrassColor;
        this.foliageColor = config.FoliageColor;

        if (this.grassColor != 0xffffff)
            this.grassColorSet = true;

        if (this.foliageColor != 0xffffff)
            this.foliageColorSet = true;

        // color ?
        //this.x = 522674;

        // duno.
        //this.A = 9154376;


    }

    public void CopyBiome(abi baseBiome)
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
    public int j()
    {
        if (!this.grassColorSet)
            return super.j();
        double d1 = i();
        double d2 = h();

        return ((zq.a(d1, d2)& 0xFEFEFE) + this.grassColor) / 2;
    }

    // getFoliageColorAtCoords
    public int k()
    {
        if (!this.foliageColorSet)
            return super.k();
        double d1 = i();
        double d2 = h();

        return ((zq.a(d1, d2)& 0xFEFEFE)  + this.foliageColor) / 2;
    }
}
