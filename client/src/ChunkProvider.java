import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;

import java.util.List;

public class ChunkProvider implements ca
{

    private SingleWorld world;
    private xd worldHandle;

    private ChunkProviderTC generator;
    private ObjectSpawner spawner;


    public ChunkProvider(SingleWorld _world)
    {
        //super(_world.getWorld(), _world.getSeed());

        this.world = _world;
        this.worldHandle = _world.getWorld();

        this.generator = new ChunkProviderTC(this.world.getSettings(), this.world);
        this.spawner = new ObjectSpawner(this.world.getSettings(), this.world);


    }


    public boolean a(int i, int i1)
    {
        return true;
    }

    public ack b(int x, int z)
    {

        ack chunk = new ack(this.worldHandle, x, z);

        byte[] BlockArray = this.generator.generate(x, z);
        zg[] sections = chunk.i();

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
                            sections[sectionId] = new zg(sectionId << 4);
                        }
                        sections[sectionId].a(_x, y & 0xF, _z, block);
                    }
                }


        chunk.b();
        return chunk;
    }

    public ack c(int i, int i1)
    {
        return b(i, i1);
    }

    public void a(ca ChunkProvider, int x, int z)
    {
        yp.a = true;
        this.world.LoadChunk(x, z);
        this.spawner.populate(x, z);
        yp.a = false;
    }

    public boolean a(boolean b, rw rs)
    {
        return true;
    }

    public boolean a()
    {
        return false;
    }

    public boolean b()
    {
        return true;
    }

    public String c()
    {
        return "TerrainControlLevelSource";
    }

    public List a(acf paramaca, int paramInt1, int paramInt2, int paramInt3)
    {
        abn localabi = this.worldHandle.a(paramInt1, paramInt3);
        if (localabi == null)
        {
            return null;
        }
        return localabi.a(paramaca);
    }

    public qo a(xd wz, String s, int i, int i1, int i2)
    {
        return null;
    }
}
