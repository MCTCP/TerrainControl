import com.Khorn.TerrainControl.Configuration.BiomeConfig;

public class CustomBiome extends zp
{
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


    }
}
