package com.pg85.otg.util.materials;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A material set that accepts special values such as "All" or "Solid". These
 * special values make it almost impossible to know which materials are in
 * this set, and as such, this set can't be iterated over and its size remains
 * unknown.
 */
// TODO: This seems really inefficient and riddiculously overcomplicated, burn with fire.
// Looks like this is optimised mainly for use with blockchecks and BOfunctions, resources like oregen also use it though,
// they shouldn't need any other functionality than containing a list of materials.
public class MaterialSet
{
    /**
     * Keyword that adds all materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    private static final String ALL_MATERIALS = "All";

    /**
     * Keyword that adds all solid materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    public static final String SOLID_MATERIALS = "Solid";

    /**
     * Keyword that adds all non solid materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    private static final String NON_SOLID_MATERIALS = "NonSolid";

    private boolean allMaterials = false;
    private boolean allSolidMaterials = false;
    private boolean allNonSolidMaterials = false;

    private int[] materialIntSet = new int[0];
    public Set<MaterialSetEntry> materials = new LinkedHashSet<MaterialSetEntry>();
    private boolean intSetUpToDate = true;
    private boolean parsed = false;

    /**
     * Adds the given material to the list.
     *
     * <p>If the material is "All", all
     * materials in existence are added to the list. If the material is
     * "Solid", all solid materials are added to the list. Otherwise,
     * {@link OTG#readMaterial(String)} is used to read the
     * material.
     *
     * <p>If the material {@link StringHelper#specifiesBlockData(String)
     * specifies block data}, it will match only materials with exactly that
     * block data. If the material doesn't specify block data, it will match
     * materials with any block data.
     *
     * @param input The name of the material to add.
     * @throws InvalidConfigException If the name is invalid.
     */
    public void parseAndAdd(String input) throws InvalidConfigException
    {
        if (input.equalsIgnoreCase(ALL_MATERIALS))
        {
            this.allMaterials = true;
            return;
        }
        if (input.equalsIgnoreCase(SOLID_MATERIALS))
        {
            this.allSolidMaterials = true;
            return;
        }
        if (input.equalsIgnoreCase(NON_SOLID_MATERIALS))
        {
            this.allNonSolidMaterials = true;
            return;
        }

        LocalMaterialData material = MaterialHelper.readMaterial(input);
        
        boolean checkIncludesBlockData = StringHelper.specifiesBlockData(input);
        
        if(material == null)
        {
        	throw new InvalidConfigException("Invalid block check, material \"" + input + "\" could not be found.");
        }
        
        // Add to set
        add(new MaterialSetEntry(material, checkIncludesBlockData));
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (allMaterials ? 1231 : 1237);
        result = prime * result + (allNonSolidMaterials ? 1231 : 1237);
        result = prime * result + (allSolidMaterials ? 1231 : 1237);
        result = prime * result + materials.hashCode();
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
        if (getClass() != obj.getClass())
        {
            return false;
        }
        MaterialSet other = (MaterialSet) obj;
        if (allMaterials != other.allMaterials)
        {
            return false;
        }
        if (allNonSolidMaterials != other.allNonSolidMaterials)
        {
            return false;
        }
        if (allSolidMaterials != other.allSolidMaterials)
        {
            return false;
        }
        if (!materials.equals(other.materials))
        {
            return false;
        }
        return true;
    }

    /**
     * Adds the entry to this material set.
     *
     * @param entry The entry to add, may not be null.
     */
    private void add(MaterialSetEntry entry)
    {
        // Add the appropriate hashCode
        intSetUpToDate = false;
        materials.add(entry);
    }
    
    public void parseForWorld(LocalWorld world) {
        if (!parsed)
        {
            for (MaterialSetEntry material : materials)
            {
                material.parseForWorld(world);
            }
            parsed = true;
            intSetUpToDate = false;
        }
    }

    /**
     * Updates the int (hashCode) set, so that is is up to date again with the
     * material set.
     */
    private void updateIntSet()
    {
        if (intSetUpToDate)
        {
            // Already up to date
            return;
        }

        // Update the int set
        materialIntSet = new int[materials.size()];
        int i = 0;
        for (MaterialSetEntry entry : materials)
        {
            materialIntSet[i] = entry.hashCode();
            i++;
        }
        // Sort int set so that we can use Arrays.binarySearch
        Arrays.sort(materialIntSet);
        intSetUpToDate = true;
    }

    /**
     * Gets whether the specified material is in this collection. Returns
     * false if the material is null.
     *
     * @param material The material to check.
     * @return True if the material is in this set.
     */
    public boolean contains(LocalMaterialData material)
    {
        if (material == null || material.isEmpty())
        {
            return false;
        }
        if (allMaterials)
        {
            return true;
        }
        if (allSolidMaterials && material.isSolid())
        {
            return true;
        }
        if (allNonSolidMaterials && !material.isSolid())
        {
            return true;
        }

        // Try to update int set
        updateIntSet();

        // Check if the material is included
        if (Arrays.binarySearch(materialIntSet, material.hashCodeWithoutBlockData()) >= 0)
        {
            return true;
        }
        if (Arrays.binarySearch(materialIntSet, material.hashCode()) >= 0)
        {
            return true;
        }
        return false;
    }

    /**
     * Returns a comma (",") seperated list of all materials in this set.
     * Keywords are left intact. No brackets ("[" or "]") are used at the
     * begin and end of the string.
     *
     * @return The string.
     */
    @Override
    public String toString()
    {
        // Check if all materials are included
        if (allMaterials)
        {
            return ALL_MATERIALS;
        }

        StringBuilder builder = new StringBuilder();
        // Check for solid materials
        if (allSolidMaterials)
        {
            builder.append(SOLID_MATERIALS).append(',');
        }
        // Check for non-solid materials
        if (allNonSolidMaterials)
        {
            builder.append(NON_SOLID_MATERIALS).append(',');
        }
        // Add all other materials
        for (MaterialSetEntry material : materials)
        {
            builder.append(material.toString()).append(',');
        }

        // Remove last ','
        if (builder.length() > 0)
        {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    /**
     * Gets a new material set where all blocks are rotated.
     *
     * @return The new material set.
     */
    public MaterialSet rotate()
    {
        MaterialSet rotated = new MaterialSet();
        if (this.allMaterials)
        {
            rotated.allMaterials = true;
        }
        if (this.allSolidMaterials)
        {
            rotated.allSolidMaterials = true;
        }
        if (this.allNonSolidMaterials)
        {
            rotated.allNonSolidMaterials = true;
        }
        rotated.intSetUpToDate = false;
        for (MaterialSetEntry material : this.materials)
        {
            rotated.materials.add(material.rotate());
        }
        return rotated;
    }

}
