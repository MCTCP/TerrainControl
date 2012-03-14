package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.CustomObjects.CustomObjectGen;

public enum ResourceType
{
    Ore(OreGen.class, false),
    UnderWaterOre(UnderWaterOreGen.class),
    Plant(PlantGen.class),
    Liquid(LiquidGen.class),
    Grass(GrassGen.class),
    Reed(ReedGen.class),
    Cactus(CactusGen.class),
    Dungeon(DungeonGen.class),
    Tree(TreeGen.class),
    CustomObject(CustomObjectGen.class),
    UnderGroundLake(UndergroundLakeGen.class),
    AboveWaterRes(AboveWaterGen.class),
    Vines(VinesGen.class);

    public ResourceGenBase Generator;
    public final boolean CreateNewChunks;


    private ResourceType(Class<? extends ResourceGenBase> c)
    {
        this(c, true);
    }

    private ResourceType(Class<? extends ResourceGenBase> c, boolean createNewChunks)
    {
        this.CreateNewChunks = createNewChunks;
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