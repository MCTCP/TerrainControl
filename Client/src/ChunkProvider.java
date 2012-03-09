import com.Khorn.TerrainControl.Generator.ChunkProviderTC;
import com.Khorn.TerrainControl.Generator.ObjectSpawner;

import java.util.List;

public class ChunkProvider implements bx
{

    private SingleWorld world;
    private wz worldHandle;

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

    public acf b(int x, int z)
    {

        acf chunk = new acf(this.worldHandle, x, z);

        byte[] BlockArray = this.generator.generate(x, z);
        zb[] sections = chunk.i();

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
                            sections[sectionId] = new zb(sectionId << 4);
                        }
                        sections[sectionId].a(_x, y & 0xF, _z, block);
                    }
                }


        chunk.b();
        return chunk;
    }

    public acf c(int i, int i1)
    {
        return b(i, i1);
    }

    public void a(bx bx, int x, int z)
    {
        yk.a = true;
        this.world.LoadChunk(x, z);
        this.spawner.populate(x, z);
        yk.a = false;
    }

    public boolean a(boolean b, rs rs)
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

    public List a(aca paramaca, int paramInt1, int paramInt2, int paramInt3)
    {
        abi localabi = this.worldHandle.a(paramInt1, paramInt3);
        if (localabi == null)
        {
            return null;
        }
        return localabi.a(paramaca);
    }

    public qk a(wz wz, String s, int i, int i1, int i2)
    {
        return null;
    }
}
