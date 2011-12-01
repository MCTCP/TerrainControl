package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.*;

import java.util.Random;

public class TreeGen extends ResourceGenBase
{
    private WorldGenTrees Tree = new WorldGenTrees(false);
    private WorldGenBigTree BigTree = new WorldGenBigTree(false);
    private WorldGenForest Forest = new WorldGenForest(false);
    private WorldGenSwampTree SwampTree = new WorldGenSwampTree();
    private WorldGenTaiga1 TaigaTree1 = new WorldGenTaiga1();
    private WorldGenTaiga2 TaigaTree2 = new WorldGenTaiga2(false);
    private WorldGenHugeMushroom HugeMushroom = new WorldGenHugeMushroom();


    public TreeGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {

    }

    @Override
    public void Process(Random _rand, Resource res, int x, int z)
    {
        this.rand = _rand;

        for (int i = 0; i < res.Frequency; i++)
        {

            int _x = x + this.rand.nextInt(16) + 8;
            int _z = z + this.rand.nextInt(16) + 8;
            int _y = this.world.getHighestBlockYAt(_x, _z);

            for (int t = 0; t < res.TreeTypes.length; t++)
                if (this.rand.nextInt(100) < res.TreeChances[t])
                    SelectAndSpawnTree(res.TreeTypes[t], _x, _y, _z);


        }
    }

    private void SelectAndSpawnTree(TreeType type, int x, int y, int z)
    {
        switch (type)
        {
            case Tree:
                Tree.a(this.world, this.rand, x, y, z);
                break;
            case BigTree:
                BigTree.a(1.0D, 1.0D, 1.0D);
                BigTree.a(this.world, this.rand, x, y, z);
                break;
            case Forest:
                Forest.a(this.world, this.rand, x, y, z);
                break;
            case HugeMushroom:
                HugeMushroom.a(1.0D, 1.0D, 1.0D);
                HugeMushroom.a(this.world, this.rand, x, y, z);
                break;
            case SwampTree:
                SwampTree.a(this.world, this.rand, x, y, z);
                break;
            case Taiga1:
                TaigaTree1.a(this.world, this.rand, x, y, z);
                break;
            case Taiga2:
                TaigaTree2.a(this.world, this.rand, x, y, z);
                break;
        }
    }
}
