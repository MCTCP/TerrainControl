package com.pg85.otg.configuration;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.ArrayList;
import java.util.List;

public class ReplaceBlocks
{
    protected final String sourceBlock;
    protected final String targetBlock;

    public ReplaceBlocks(String sourceBlock, String targetBlock)
    {
        this.sourceBlock = sourceBlock;
        this.targetBlock = targetBlock;
    }

    public String getSourceBlock()
    {
        return this.sourceBlock;
    }

    public String getTargetBlock()
    {
        return this.targetBlock;
    }

    public static List<ReplaceBlocks> fromJson(String originalJson) throws InvalidConfigException
    {
    	if(originalJson == null || originalJson.length() == 0)
    	{
    		return null;
    	}
    	
        // Example: [{"mob": "Sheep", "weight": 12, "min": 4, "max": 4}]
        List<ReplaceBlocks> mobGroups = new ArrayList<ReplaceBlocks>();

        String json = originalJson.trim();
        if (json.length() <= 2)
        {
            // Empty Json
            return mobGroups;
        }
        // Remove the [..]
        json = removeFirstAndLastChar(json);

        // Every group is separated by a , but in the group the , is also
        // used.
        // So convert the ( to {, the ) to } and use an existing function to
        // get each group
        json = json.replace('{', '(');
        json = json.replace('}', ')');

        String[] groups = StringHelper.readCommaSeperatedString(json);

        for (String group : groups)
        {
            mobGroups.add(readSingleGroup(group));
        }

        return mobGroups;
    }

    private static ReplaceBlocks readSingleGroup(String json) throws InvalidConfigException
    {
        String group = removeFirstAndLastChar(json.trim());
        String[] groupParts = StringHelper.readCommaSeperatedString(group);
        String sourceBlock = null;
        String targetBlock = null;

        // Read all options
        for (String option : groupParts)
        {
        	option = option.replace(" ", "");
        	String[] optionParts = option.split("\"");
            if (optionParts.length != 4)
            {
            	throw new InvalidConfigException("Invalid JSON structure near " + option);
            }
            //String[] optionParts = option.split(":");
            //if (optionParts.length != 2)
            //{
                //throw new InvalidConfigException("Invalid JSON structure near " + option);
            //}
            sourceBlock = optionParts[1];//.trim();//.replace("\"", "");
            targetBlock = optionParts[3];//.trim();//.replace("\"", "");
        }

        // Check if data is complete and valid
        if (sourceBlock == null || targetBlock == null)
        {
            throw new InvalidConfigException("Invalid JSON: " + json + ". Expected [{\"sourceblock1\":\"targetblock1\"},{\"sourceblock2\":\"targetblock2\"}] etc.");
        }

        return new ReplaceBlocks(sourceBlock, targetBlock);
    }

    /**
     * Converts a list of mob groups to a single, JSON-formatted string.
     * @param list The list to convert.
     * @return The mob groups.
     */
    public static String toJson(List<ReplaceBlocks> list)
    {
        StringBuilder json = new StringBuilder("[");
        if(list != null)
        {
	        for (ReplaceBlocks group : list)
	        {
	            group.toJson(json);
	            json.append(", ");
	        }
        } else {
        	return "";
        }
        // Remove ", " at end
        if (json.length() != 1)
        {
            json.deleteCharAt(json.length() - 1);
            json.deleteCharAt(json.length() - 1);
        }
        // Add closing bracket
        json.append(']');
        return json.toString();
    }

    /**
     * Converts this group to a JSON string.
     * @param json The {@link StringBuilder} to append the JSON to.
     */
    private void toJson(StringBuilder json)
    {
        json.append("{\"");
        json.append(sourceBlock);
        json.append("\":\"");
        json.append(targetBlock);
        json.append("\"}");
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        toJson(builder);
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        int prime = 31;
        int result = 1;
        result = prime * result + sourceBlock.hashCode();
        result = prime * result + targetBlock.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof ReplaceBlocks))
        {
            return false;
        }
        ReplaceBlocks other = (ReplaceBlocks) obj;
        if (sourceBlock != other.sourceBlock || targetBlock != other.targetBlock)
        {
            return false;
        }
        if (!sourceBlock.equals(other.sourceBlock) || !targetBlock.equals(other.targetBlock))
        {
            return false;
        }
        return true;
    }

    private static String removeFirstAndLastChar(String string)
    {
        return string.substring(1, string.length() - 1);
    }
}
