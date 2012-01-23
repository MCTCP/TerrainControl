package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.CustomObjects.CustomObjectGen;

public enum ResourceType
{
    Ore(0, OreGen.class, false),
    UnderWaterOre(1, UnderWaterOreGen.class),
    Plant(2, PlantGen.class),
    Liquid(3, LiquidGen.class),
    Grass(4, GrassGen.class),
    Reed(5, ReedGen.class),
    Cactus(6, CactusGen.class),
    Dungeon(7, DungeonGen.class),
    Tree(8, TreeGen.class),
    CustomObject(9, CustomObjectGen.class),
    UnderGroundLake(10, UndergroundLakeGen.class),
    AboveWaterRes(11, AboveWaterGen.class);


    public final int id;
    public ResourceGenBase Generator;
    public final boolean CreateNewChunks;


    private ResourceType(int ID, Class<? extends ResourceGenBase> c)
    {
        this(ID, c, true);
    }

    private ResourceType(int ID, Class<? extends ResourceGenBase> c, boolean createNewChunks)
    {
        this.id = ID;
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