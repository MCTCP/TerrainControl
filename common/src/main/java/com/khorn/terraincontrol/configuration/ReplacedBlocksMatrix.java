package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReplacedBlocksMatrix
{
    private static final String NO_REPLACE = "None";

    public static class ReplacedBlocksInstruction
    {
        private final LocalMaterialData from;
        private final LocalMaterialData to;
        private final int minHeight;
        private final int maxHeight;

        /**
         * Parses the given instruction string.
         * 
         * @param instruction The instruction string.
         * @param maxAllowedY Maximum allowed y height for the replace
         *            setting, inclusive.
         * @throws InvalidConfigException If the instruction is formatted
         *             incorrectly.
         */
        public ReplacedBlocksInstruction(String instruction, int maxAllowedY) throws InvalidConfigException
        {
            String[] values = instruction.split(",");
            if (values.length == 5)
            {
                // Replace in TC 2.3 style found
                values = new String[] {values[0], values[1] + ":" + values[2], values[3], "" + (Integer.parseInt(values[4]) - 1)};
            }

            if (values.length != 2 && values.length != 4)
            {
                throw new InvalidConfigException("Replace parts must be in the format (from,to) or (from,to,minHeight,maxHeight)");
            }

            from = TerrainControl.readMaterial(values[0]).withBlockData(0);
            to = TerrainControl.readMaterial(values[1]);

            if (values.length == 4)
            {
                minHeight = StringHelper.readInt(values[2], 0, maxAllowedY);
                maxHeight = StringHelper.readInt(values[3], minHeight, maxAllowedY);
            } else
            {
                minHeight = 0;
                maxHeight = maxAllowedY;
            }
        }

        /**
         * Creates a ReplacedBlocksInstruction with the given parameters.
         * Parameters may not be null.
         * 
         * @param from The block that will be replaced.
         * @param to The block that from will be replaced to.
         * @param minHeight Minimum height for this replace, inclusive. Must
         *            be smaller than or equal to 0.
         * @param maxHeight Maximum height for this replace, inclusive. Must
         *            not be larger than
         *            {@link ReplacedBlocksMatrix#maxHeight}.
         */
        public ReplacedBlocksInstruction(LocalMaterialData from, LocalMaterialData to, int minHeight, int maxHeight)
        {
            this.from = from;
            this.to = to;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }

        public LocalMaterialData getFrom()
        {
            return from;
        }

        public LocalMaterialData getTo()
        {
            return to;
        }

        public int getMinHeight()
        {
            return minHeight;
        }

        public int getMaxHeight()
        {
            return maxHeight;
        }
    }

    /**
     * All {@link ReplacedBlocksInstruction}s must have their
     * {@link ReplacedBlocksInstruction#getMaxHeight() maxHeight} smaller than
     * or equal to this.
     */
    public final int maxHeight;
    private List<ReplacedBlocksInstruction> instructions;

    /**
     * The compiled ReplacedBlocks instructions. Don't change this variable.
     * May be null when this biome {@link #hasReplaceSettings() doesn't
     * replace blocks}.
     */
    public LocalMaterialData[][] compiledInstructions;

    public ReplacedBlocksMatrix(String setting, int maxHeight) throws InvalidConfigException
    {
        this.maxHeight = maxHeight;

        // Parse
        if (setting.isEmpty() || setting.equalsIgnoreCase(NO_REPLACE))
        {
            setInstructions(Collections.<ReplacedBlocksInstruction> emptyList());
            return;
        }

        List<ReplacedBlocksInstruction> instructions = new ArrayList<ReplacedBlocksInstruction>();
        String[] keys = StringHelper.readCommaSeperatedString(setting);

        for (String key : keys)
        {
            int start = key.indexOf('(');
            int end = key.lastIndexOf(')');
            if (start != -1 && end != -1)
            {
                String keyWithoutBraces = key.substring(start + 1, end);
                instructions.add(new ReplacedBlocksInstruction(keyWithoutBraces, maxHeight));
            } else
            {
                throw new InvalidConfigException("One of the parts is missing braces around it.");
            }

        }

        // Set
        setInstructions(instructions);
    }

    /**
     * Gets whether this biome has replace settings set. If this returns true,
     * the {@link #compiledInstructions} array won't be null.
     * 
     * @return Whether this biome has replace settings set.
     */
    public boolean hasReplaceSettings()
    {
        return this.compiledInstructions != null;
    }

    /**
     * Gets an immutable list of all ReplacedBlocks instructions.
     * 
     * @return The ReplacedBlocks instructions.
     */
    public List<ReplacedBlocksInstruction> getInstructions()
    {
        // Note that the returned list is immutable, see setInstructions
        return instructions;
    }

    /**
     * Sets the ReplacedBlocks instructions. This method will update the
     * {@link #compiledInstructions} array.
     * 
     * @param instructions The new instructions.
     */
    public void setInstructions(Collection<ReplacedBlocksInstruction> instructions)
    {
        this.instructions = Collections.unmodifiableList(new ArrayList<ReplacedBlocksInstruction>(instructions));

        if (this.instructions.size() == 0)
        {
            this.compiledInstructions = null;
            return;
        }

        this.compiledInstructions = new LocalMaterialData[TerrainControl.SUPPORTED_BLOCK_IDS][];
        for (ReplacedBlocksInstruction instruction : instructions)
        {
            int fromBlockId = instruction.getFrom().getBlockId();
            int minHeight = instruction.getMinHeight();
            int maxHeight = instruction.getMaxHeight();
            LocalMaterialData toBlock = instruction.getTo();

            if (compiledInstructions[fromBlockId] == null)
            {
                compiledInstructions[fromBlockId] = new LocalMaterialData[this.maxHeight + 1];
            }
            for (int y = minHeight; y <= maxHeight; y++)
            {
                compiledInstructions[fromBlockId][y] = toBlock;
            }
        }
    }

    public String toString()
    {
        if (!this.hasReplaceSettings())
        {
            // No replace setting
            return NO_REPLACE;
        }

        StringBuilder builder = new StringBuilder();
        for (ReplacedBlocksInstruction instruction : getInstructions())
        {
            builder.append('(');
            builder.append(instruction.getFrom());
            builder.append(',').append(instruction.getTo());
            if (instruction.getMinHeight() != 0 || instruction.getMaxHeight() != this.maxHeight)
            {
                // Add custom height setting
                builder.append(',').append(instruction.getMinHeight());
                builder.append(',').append(instruction.getMaxHeight());
            }
            builder.append(')').append(',');
        }

        // Remove last ',' and return the result
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * Creates an empty matrix.
     * 
     * @param maxHeight Max height for the replace setting, inclusive.
     * @return The empty matrix.
     */
    public static ReplacedBlocksMatrix createEmptyMatrix(int maxHeight)
    {
        try
        {
            return new ReplacedBlocksMatrix(NO_REPLACE, maxHeight);
        } catch (InvalidConfigException e)
        {
            // Should never happen
            throw new AssertionError(e);
        }
    }
}
