import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import com.Khorn.TerrainControl.LocalBiome;

public class Biome implements LocalBiome
{
    private abi biomeBase;

    public Biome(abi biome)
    {
        this.biomeBase = biome;
    }

    public boolean isCustom()
    {
        return true;
    }

    public void setCustom(BiomeConfig config)
    {
        ((CustomBiome)this.biomeBase).SetBiome(config);
    }

    public String getName()
    {
        return this.biomeBase.y;
    }

    public int getId()
    {
        return this.biomeBase.M;
    }

    public float getTemperature()
    {
        return this.biomeBase.F;
    }

    public float getWetness()
    {
        return this.biomeBase.G;
    }

    public float getSurfaceHeight()
    {
        return this.biomeBase.D;
    }

    public float getSurfaceVolatility()
    {
        return this.biomeBase.E;
    }

    public byte getSurfaceBlock()
    {
        return this.biomeBase.A;
    }

    public byte getGroundBlock()
    {
        return this.biomeBase.B;
    }
}
