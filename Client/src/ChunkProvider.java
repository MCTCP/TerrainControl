import com.Khorn.TerrainControl.Generator.ChunkProviderTC;
import com.Khorn.TerrainControl.Generator.ObjectSpawner;

public class ChunkProvider extends aji
{

    private SingleWorld world;
    private vq worldHandle;

    private ChunkProviderTC generator;
    private ObjectSpawner spawner;


    public ChunkProvider(SingleWorld _world)
    {
        super(_world.getWorld(), _world.getSeed(), false);

        this.world = _world;
        this.worldHandle = _world.getWorld();

        this.generator = new ChunkProviderTC(this.world.getSettings(), this.world);
        this.spawner = new ObjectSpawner(this.world.getSettings(), this.world);


    }


    @Override
    public aal b(int x, int z)
    {

        aal localaal = new aal(this.worldHandle, this.generator.generate(x, z), x, z);

        localaal.c();
        return localaal;
    }

    @Override
    public void a(bs bs, int x, int z)
    {
        this.world.LoadChunk(x, z);
        this.spawner.populate(x, z);
    }


    @Override
    public pr a(vq paramvq, String paramString, int paramInt1, int paramInt2, int paramInt3)
    {
        if (("Stronghold".equals(paramString)) && (this.world.getStrongholdGen() != null))
        {
            return this.world.getStrongholdGen().a(paramvq, paramInt1, paramInt2, paramInt3);
        }
        return null;
    }
}
