package com.pg85.otg.constants;

public class SettingsEnums
{
    public enum TerrainMode
    {
        Normal,
        //OldGenerator,
        TerrainTest,
        NotGenerate //,
        //Default
    }

    public enum ImageMode
    {
        Repeat,
        Mirror,
        ContinueNormal,
        FillEmpty,
    }

    public enum ImageOrientation
    {
        North,
        East,
        South,
        West,
    }

    public enum ConfigMode
    {
        WriteAll,
        WriteDisable,
        WriteWithoutComments
    }
    
    public enum enumBiomeConfigMaterial
    {
    	WATER_BLOCK,
    	ICE_BLOCK,
    	COOLED_LAVA_BLOCK,
    	STONE_BLOCK
    }    
    
    public enum VillageType
    {
        disabled,
        wood,
        sandstone,
        taiga,
        savanna,
        snowy
    }    
    
    public enum MineshaftType
    {
        disabled,
        normal,
        mesa
    }    
    
    public enum RareBuildingType
    {
        disabled,
        desertPyramid,
        jungleTemple,
        swampHut,
        igloo
    }
}
