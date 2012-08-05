import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;

import java.util.List;

public class ChunkProvider implements wh
{

    private SingleWorld world;
    private uo worldHandle;
    private boolean TestMode = false;

    private ChunkProviderTC generator;
    private ObjectSpawner spawner;


    public ChunkProvider(SingleWorld _world)
    {
        //super(_world.getWorld(), _world.getSeed());

        this.world = _world;
        this.worldHandle = _world.getWorld();

        this.TestMode = world.getSettings().ModeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.generator = new ChunkProviderTC(this.world.getSettings(), this.world);
        this.spawner = new ObjectSpawner(this.world.getSettings(), this.world);


    }


    public boolean a(int i, int i1)
    {
        return true;
    }

    public wk d(int x, int z)
    {

        wk chunk = new wk(this.worldHandle, x, z);

        byte[] BlockArray = this.generator.generate(x, z);
        wl[] sections = chunk.i();

        int i1 = BlockArray.length / 256;
        for (int _x = 0; _x < 16; _x++)
            for (int _z = 0; _z < 16; _z++)
                for (int y = 0; y < i1; y++)
                {
                    int block = BlockArray[(_x << world.getHeightBits() + 4 | _z << world.getHeightBits() | y)];
                    if (block != 0)
                    {
                        int sectionId = y >> 4;
                        if (sections[sectionId] == null)
                        {
                            sections[sectionId] = new wl(sectionId << 4);
                        }
                        sections[sectionId].a(_x, y & 0xF, _z, block);
                    }
                }
        world.FillChunkForBiomes(chunk, x, z);

        chunk.b();
        return chunk;
    }

    public wk c(int i, int i1)
    {
        return d(i, i1);
    }

    public void a(wh ChunkProvider, int x, int z)
    {
        if (this.TestMode)
            return;
        ahp.a = true;
        this.world.LoadChunk(x, z);
        this.spawner.populate(x, z);
        ahp.a = false;
    }

    public boolean a(boolean b, il il)
    {
        return true;
    }

    public boolean b()
    {
        return false;
    }

    public boolean c()
    {
        return true;
    }

    public String d()
    {
        return "TerrainControlLevelSource";
    }

    public List a(jw paramaca, int paramInt1, int paramInt2, int paramInt3)
    {
        vj Biome = this.worldHandle.a(paramInt1, paramInt3);
        if (Biome == null)
        {
            return null;
        }
        return Biome.a(paramaca);
    }

    public vg a(uo world, String s, int x, int y, int z)
    {
        if (("Stronghold".equals(s)) && (this.world.strongholdGen != null))
        {
            return this.world.strongholdGen.a(world, x, y, z);
        }
        return null;
    }

    @Override
    public int e()
    {
        return 0;
    }
}
