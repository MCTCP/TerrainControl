package com.khorn.terraincontrol.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.khorn.terraincontrol.DefaultMobType;
import com.khorn.terraincontrol.TerrainControl;

/**
 * This class holds data for a bukkit nms.BiomeMeta class. The name does not
 * match but ours make more sense.
 */
public class WeightedMobSpawnGroup
{
    // -------------------------------------------- //
    // FIELDS
    // -------------------------------------------- //
    protected String mob = null;
    
    public WeightedMobSpawnGroup(String mobName, int weight, int min, int max)
    {
        this.mob = mobName;
        this.weight = weight;
        this.min = min;
        this.max = max;
    }

    public String getMobName()
    {
        return this.mob;
    }

    public DefaultMobType getDefaultMobType()
    {
        DefaultMobType ret = DefaultMobType.fromName(this.mob);
        if (ret == null)
        {
            System.err.println("Invalid mob name: " + this.mob);
        }
        return ret;
    }

    public void setMobName(String value)
    {
        this.mob = value;
    }

    protected int weight = 0;

    public int getWeight()
    {
        return this.weight;
    }

    public void setWeight(int value)
    {
        this.weight = value;
    }

    protected int min = 0;

    public int getMin()
    {
        return this.min;
    }

    public void setMin(int value)
    {
        this.min = value;
    }

    protected int max = 0;

    public int getMax()
    {
        return this.max;
    }

    public void setMax(int value)
    {
        this.max = value;
    }

    public static List<WeightedMobSpawnGroup> fromJson(String originalJson)
    {
        // Example: [{"mob": "Sheep", "weight": 12, "min": 4, "max": 4}]
        List<WeightedMobSpawnGroup> mobGroups = new ArrayList<WeightedMobSpawnGroup>();
        
        String json = originalJson.trim();
        if(json.length() <= 2)
        {
            // Empty Json
            return mobGroups;
        }
        // Remove the [..]
        json = removeFirstAndLastChar(json); 
        
        // Every group is seperated by a , but in the group the , is also used.
        // So convert the ( to {, the ) to } and use an existing function to get each group
        json = json.replace('{', '(');
        json = json.replace('}', ')');
        
        String[] groups = ConfigFile.ReadComplexString(json);
        
        try
        {
            for (String group : groups)
            {
                group = group.trim();
                group = removeFirstAndLastChar(group);
                String[] groupParts = group.split(",");
                String mobName = null;
                int weight = 0;
                int min = 0;
                int max = 0;
                for (String option : groupParts)
                {
                    option = option.trim();
                    String[] optionParts = option.split(":");
                    if (optionParts[0].trim().equalsIgnoreCase("\"mob\""))
                    {
                        // Remove the quotes
                        mobName = removeFirstAndLastChar(optionParts[1].trim());
                    }
                    if (optionParts[0].trim().equalsIgnoreCase("\"weight\""))
                    {
                        weight = Integer.parseInt(optionParts[1].trim());
                    }
                    if (optionParts[0].trim().equalsIgnoreCase("\"min\""))
                    {
                        min = Integer.parseInt(optionParts[1].trim());
                    }
                    if (optionParts[0].trim().equalsIgnoreCase("\"max\""))
                    {
                        max = Integer.parseInt(optionParts[1].trim());
                    }
                }
                if (mobName != null && weight > 0 && min > 0 && max >= min)
                {
                    mobGroups.add(new WeightedMobSpawnGroup(mobName, weight, min, max));
                }
            }
        } catch (NumberFormatException e)
        {
            TerrainControl.log(Level.WARNING, "Incorrect number in JSON: " + originalJson);
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e)
        {
            TerrainControl.log(Level.WARNING, "Incorrect JSON: " + originalJson);
        }

        return mobGroups;
    }
    
    public static String toJson(List<WeightedMobSpawnGroup> list)
    {
        StringBuilder json = new StringBuilder("[");
        for(WeightedMobSpawnGroup group: list) {
            json.append("{\"mob\": \"");
            json.append(group.getMobName());
            json.append("\", \"weight\": ");
            json.append(group.getWeight());
            json.append(", \"min\": ");
            json.append(group.getMin());
            json.append(", \"max\": ");
            json.append(group.getMax());
            json.append("}, ");
        }
        // Remove ", " at end
        if(json.length() != 1)
        {
            json.deleteCharAt(json.length() - 1);
            json.deleteCharAt(json.length() - 1); 
        }
        // Add closing bracket
        json.append(']');
        return json.toString();
    }

    private static String removeFirstAndLastChar(String string)
    {
        return string.substring(1, string.length() - 1);
    }
}
