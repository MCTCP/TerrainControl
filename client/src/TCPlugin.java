import com.khorn.terraincontrol.configuration.WorldConfig;

import java.io.File;

public class TCPlugin extends uz
{

    public SingleWorld TCWorld;


    public TCPlugin(int paramInt, String paramString)
    {
        super(paramInt, paramString);
    }

    @Override
    public vo getChunkManager(uo world)
    {
        if ( world instanceof atc)
            return super.getChunkManager(world);

        this.TCWorld = new SingleWorld(world.A.j());

        File worldDir = null;

        try
        {
            worldDir = (File) ModLoader.getPrivateValue(aea.class, world.G(), "b");

        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        File configDir = new File(worldDir, "TerrainControl");

        if (!configDir.exists())
        {
            System.out.println("TerrainControl: settings does not exist, creating defaults");

            if (!configDir.mkdirs())
                System.out.println("TerrainControl: cant create folder " + configDir.getName());
        }

        WorldConfig config = new WorldConfig(configDir, TCWorld, false);
        this.TCWorld.setSettings(config);
        this.TCWorld.Init(world);


        vo ChunkManager = null;

        switch (this.TCWorld.getSettings().ModeBiome)
        {
            case FromImage:
            case Normal:
                ChunkManager = new BiomeManager(this.TCWorld);
                this.TCWorld.setBiomeManager((BiomeManager) ChunkManager);
                break;
            case OldGenerator:
                ChunkManager = new BiomeManagerOld(this.TCWorld);
                this.TCWorld.setOldBiomeManager((BiomeManagerOld) ChunkManager);
                break;
            case Default:
                ChunkManager = super.getChunkManager(world);
                break;
        }


        return ChunkManager;
    }

    @Override
    public wh getChunkGenerator(uo world)
    {
        if (this.TCWorld.getSettings().ModeTerrain != WorldConfig.TerrainMode.Default)
        {
            return this.TCWorld.getChunkGenerator();
        } else
            return super.getChunkGenerator(world);
    }
}
