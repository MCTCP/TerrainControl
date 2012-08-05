import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.LocalBiome;

public class Biome implements LocalBiome
{
    private vj biomeBase;

    public Biome(vj biome)
    {
        this.biomeBase = biome;
    }

    public boolean isCustom()
    {
        return true;
    }
    public int getCustomId()
    {
        return getId();
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
