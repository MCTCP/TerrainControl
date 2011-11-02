package com.Khorn.TerrainControl.Configuration;


import com.Khorn.TerrainControl.Util.ResourceType;

public class Resource
{
    public ResourceType Type;
    public int MinAltitude;
    public int MaxAltitude;
    public int Size;
    public int BlockId;
    public int BlockData;
    private int[] SourceBlockId;
    public int Frequency;
    public int Rarity;
    public boolean Done = false;
    public boolean First = true;


    public Resource(ResourceType type)
    {
        Type = type;
    }

    public boolean CheckSourceId(int blockId)
    {
        for (int id : this.SourceBlockId)
            if (blockId == id)
                return true;
        return false;
    }

    public void ReadFromString(String line)
    {
        try
        {
            String[] Props = line.split(",");
            switch (this.Type)
            {
                case Ore:
                    if (Props.length < 7)
                        return;
                    this.BlockId = Integer.valueOf(Props[0]);
                    this.Size = Integer.valueOf(Props[1]);
                    this.Frequency = Integer.valueOf(Props[2]);
                    this.Rarity = Integer.valueOf(Props[3]);
                    this.MinAltitude = Integer.valueOf(Props[4]);
                    this.MaxAltitude = Integer.valueOf(Props[5]);

                    this.SourceBlockId = new int[Props.length - 6];
                    for (int i = 6; i < Props.length; i++)
                        this.SourceBlockId[i - 6] = Integer.valueOf(Props[i]);
                    break;
                case UnderWaterOre:
                    if (Props.length < 5)
                        return;
                    this.BlockId = Integer.valueOf(Props[0]);
                    this.Size = Integer.valueOf(Props[1]);
                    this.Frequency = Integer.valueOf(Props[2]);
                    this.Rarity = Integer.valueOf(Props[3]);


                    this.SourceBlockId = new int[Props.length - 4];
                    for (int i = 4; i < Props.length; i++)
                        this.SourceBlockId[i - 4] = Integer.valueOf(Props[i]);
                    break;
                case Liquid:
                {
                    if (Props.length < 6)
                        return;
                    this.BlockId = Integer.valueOf(Props[0]);
                    this.Frequency = Integer.valueOf(Props[1]);
                    this.Rarity = Integer.valueOf(Props[2]);
                    this.MinAltitude = Integer.valueOf(Props[3]);
                    this.MaxAltitude = Integer.valueOf(Props[4]);

                    this.SourceBlockId = new int[Props.length - 5];
                    for (int i = 5; i < Props.length; i++)
                        this.SourceBlockId[i - 5] = Integer.valueOf(Props[i]);
                    break;
                }
                case Flower:
                case Grass:
                    if (Props.length < 7)
                        return;
                    this.BlockId = Integer.valueOf(Props[0]);
                    this.BlockData = Integer.valueOf(Props[1]);
                    this.Frequency = Integer.valueOf(Props[2]);
                    this.Rarity = Integer.valueOf(Props[3]);
                    this.MinAltitude = Integer.valueOf(Props[4]);
                    this.MaxAltitude = Integer.valueOf(Props[5]);

                    this.SourceBlockId = new int[Props.length - 6];
                    for (int i = 6; i < Props.length; i++)
                        this.SourceBlockId[i - 6] = Integer.valueOf(Props[i]);

                    this.First = false;

                    break;

                case Reed:
                case Cactus:
                case Pumpkin:
                {
                    if (Props.length < 6)
                        return;
                    this.BlockId = Integer.valueOf(Props[0]);
                    this.Frequency = Integer.valueOf(Props[1]);
                    this.Rarity = Integer.valueOf(Props[2]);
                    this.MinAltitude = Integer.valueOf(Props[3]);
                    this.MaxAltitude = Integer.valueOf(Props[4]);

                    this.SourceBlockId = new int[Props.length - 5];
                    for (int i = 5; i < Props.length; i++)
                        this.SourceBlockId[i - 5] = Integer.valueOf(Props[i]);

                    this.First = false;

                    break;
                }
            }
        } catch (NumberFormatException e)
        {
            System.out.println("TerrainControl: wrong resource " + this.Type.name() + "(" + line + ")");
        }

        this.Done = true;


    }

    public String WriteToString()
    {
        String sources = "";
        for (int id : this.SourceBlockId)
            sources += "," + id;
        String output = this.Type.name() + "(";

        switch (this.Type)
        {
            case Ore:
            case UnderWaterOre:
                output += this.BlockId + "," + "," + this.Size + "," + this.Frequency + "," + this.Rarity + "," + this.MinAltitude + "," + this.MaxAltitude + sources + ")";
                break;
            case Flower:
            case Liquid:
            case Grass:
            case Reed:
            case Cactus:
            case Pumpkin:
                output += this.BlockId + "," + "," + this.Frequency + "," + this.Rarity + "," + this.MinAltitude + "," + this.MaxAltitude + sources + ")";
                break;
        }
        return output;


    }
}
