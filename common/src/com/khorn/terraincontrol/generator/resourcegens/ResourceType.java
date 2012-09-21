package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.customobjects.CustomObjectGen;

public enum ResourceType
{
    Ore(OreGen.class, false, 7),
    UnderWaterOre(UnderWaterOreGen.class, 5),
    Plant(PlantGen.class, 6),
    Liquid(LiquidGen.class, 6),
    Grass(GrassGen.class, 5),
    Reed(ReedGen.class, 6),
    Cactus(CactusGen.class, 6),
    Dungeon(DungeonGen.class, 4),
    Tree(TreeGen.class, 2),
    CustomObject(CustomObjectGen.class, 0),
    UnderGroundLake(UndergroundLakeGen.class, 6),
    AboveWaterRes(AboveWaterGen.class, 3),
    Vines(VinesGen.class, 4),
    SmallLake(SmallLakeGen.class,5);

    public ResourceGenBase Generator;
    public final boolean CreateNewChunks;
    public final int MinProperties;


    private ResourceType(Class<? extends ResourceGenBase> c, int props)
    {
        this(c, true, props);
    }

    private ResourceType(Class<? extends ResourceGenBase> c, boolean createNewChunks, int props)
    {
        this.CreateNewChunks = createNewChunks;
        this.MinProperties = props;
        try
        {
            Generator = c.newInstance();

        } catch (InstantiationException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
}
