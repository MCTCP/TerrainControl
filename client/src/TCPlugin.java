import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.ObjectsStore;

import java.io.File;

public class TCPlugin extends va
{

    public SingleWorld TCWorld;


    public TCPlugin(int paramInt, String paramString)
    {
        super(paramInt, paramString);
    }

    @Override
    public vp getChunkManager(up world)
    {
        if ( world instanceof atd)
            return super.getChunkManager(world);

        this.TCWorld = new SingleWorld(world.A.j());

        File worldDir = null;

        try
        {
            worldDir = (File) ModLoader.getPrivateValue(aeb.class, world.G(), "b");

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

        ObjectsStore.ReadObjects(configDir);

        WorldConfig config = new WorldConfig(configDir, TCWorld, false);
        this.TCWorld.setSettings(config);
        this.TCWorld.Init(world);


        vp ChunkManager = null;

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
    public wi getChunkGenerator(up world)
    {
        if (this.TCWorld.getSettings().ModeTerrain != WorldConfig.TerrainMode.Default)
        {
            return this.TCWorld.getChunkGenerator();
        } else
            return super.getChunkGenerator(world);
    }
}
