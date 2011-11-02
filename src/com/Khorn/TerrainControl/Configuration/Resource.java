package com.Khorn.TerrainControl.Configuration;


import com.Khorn.TerrainControl.Util.ResourceType;

public class Resource
{
    public ResourceType Type;
    public int MinAltitude;
    public int MaxAltitude;
    public int Size;
    public int BlockId;
    public int SourceBlockId;
    public int Frequency;
    public int Rarity;
    public boolean Done = false;
    public boolean First = true;


    public Resource(ResourceType type)
    {
        Type = type;
    }

    public void ReadFromString(String line)
    {
        try
        {
            String[] Props = line.split(",");
            switch (this.Type)
            {
                case Ore:
                case UnderWaterOre:
                {
                    if (Props.length != 7)
                        return;
                    this.BlockId = Integer.valueOf(Props[0]);
                    this.SourceBlockId = Integer.valueOf(Props[1]);
                    this.Size = Integer.valueOf(Props[2]);
                    this.Frequency = Integer.valueOf(Props[3]);
                    this.Rarity = Integer.valueOf(Props[4]);
                    this.MinAltitude = Integer.valueOf(Props[5]);
                    this.MaxAltitude = Integer.valueOf(Props[6]);
                    break;
                }
                case Liquid:
                {
                    if (Props.length != 6)
                        return;
                    this.BlockId = Integer.valueOf(Props[0]);
                    this.SourceBlockId = Integer.valueOf(Props[1]);
                    this.Frequency = Integer.valueOf(Props[2]);
                    this.Rarity = Integer.valueOf(Props[3]);
                    this.MinAltitude = Integer.valueOf(Props[4]);
                    this.MaxAltitude = Integer.valueOf(Props[5]);
                    break;
                }
                case Flower:
                case Grass:
                case Reed:
                case Cactus:
                case Pumpkin:
                {
                    if (Props.length != 6)
                        return;
                    this.BlockId = Integer.valueOf(Props[0]);
                    this.SourceBlockId = Integer.valueOf(Props[1]);
                    this.Frequency = Integer.valueOf(Props[2]);
                    this.Rarity = Integer.valueOf(Props[3]);
                    this.MinAltitude = Integer.valueOf(Props[4]);
                    this.MaxAltitude = Integer.valueOf(Props[5]);
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
        String output = this.Type.name() + "(";

        switch (this.Type)
        {
            case Ore:
            case UnderWaterOre:
                output += this.BlockId + "," + this.SourceBlockId + "," + this.Size + "," + this.Frequency + "," + this.Rarity + "," + this.MinAltitude + "," + this.MaxAltitude + ")";
                break;
            case Flower:
            case Liquid:
            case Grass:
            case Reed:
            case Cactus:
            case Pumpkin:
                output += this.BlockId + "," + this.SourceBlockId + "," + this.Frequency + "," + this.Rarity + "," + this.MinAltitude + "," + this.MaxAltitude + ")";
                break;
        }
        return output;


    }
}
