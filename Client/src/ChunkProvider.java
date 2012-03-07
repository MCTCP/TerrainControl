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

        acf chunk = new acf(this.worldHandle, this.generator.generate(x, z), x, z);

        chunk.b();
        return chunk;
    }

    public acf c(int i, int i1)
    {
        return b(i,i1);
    }

    public void a(bx bx, int x, int z)
    {
        this.world.LoadChunk(x, z);
        this.spawner.populate(x, z);
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
        return  true;
    }

    public String c()
    {
        return "TerrainControlLevelSource";
    }

    public List a(aca paramaca, int paramInt1, int paramInt2, int paramInt3)
    {
        abi localabi = this.worldHandle.a(paramInt1, paramInt3);
        if (localabi == null) {
            return null;
        }
        return localabi.a(paramaca);
    }

    public qk a(wz wz, String s, int i, int i1, int i2)
    {
        return null;
    }
}
