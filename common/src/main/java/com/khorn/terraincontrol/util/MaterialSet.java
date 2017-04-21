package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A material set that accepts special values such as "All" or "Solid". These
 * special values make it almost impossible to know which materials are in
 * this set, and as such, this set can't be iterated over and its size remains
 * unknown.
 */
public class MaterialSet
{
    /**
     * Keyword that adds all materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    public static final String ALL_MATERIALS = "All";

    /**
     * Keyword that adds all solid materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    public static final String SOLID_MATERIALS = "Solid";

    /**
     * Keyword that adds all non solid materials to the set when used in
     * {@link #parseAndAdd(String)}.
     */
    public static final String NON_SOLID_MATERIALS = "NonSolid";

    private boolean allMaterials = false;
    private boolean allSolidMaterials = false;
    private boolean allNonSolidMaterials = false;

    private int[] materialIntSet = new int[0];
    private Set<MaterialSetEntry> materials = new LinkedHashSet<MaterialSetEntry>();
    private boolean intSetUpToDate = true;

    /**
     * Adds the given material to the list.
     *
     * <p>If the material is "All", all
     * materials in existence are added to the list. If the material is
     * "Solid", all solid materials are added to the list. Otherwise,
     * {@link TerrainControl#readMaterial(String)} is used to read the
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

        LocalMaterialData material = TerrainControl.readMaterial(input);

        boolean checkIncludesBlockData = StringHelper.specifiesBlockData(input);

        // Add to set
        add(new MaterialSetEntry(material, checkIncludesBlockData));
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.allMaterials ? 1231 : 1237);
        result = prime * result + (this.allNonSolidMaterials ? 1231 : 1237);
        result = prime * result + (this.allSolidMaterials ? 1231 : 1237);
        result = prime * result + this.materials.hashCode();
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
        if (this.allMaterials != other.allMaterials)
        {
            return false;
        }
        if (this.allNonSolidMaterials != other.allNonSolidMaterials)
        {
            return false;
        }
        if (this.allSolidMaterials != other.allSolidMaterials)
        {
            return false;
        }
        if (!this.materials.equals(other.materials))
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
    public void add(MaterialSetEntry entry)
    {
        // Add the appropriate hashCode
        this.intSetUpToDate = false;
        this.materials.add(entry);
    }

    /**
     * Updates the int (hashCode) set, so that is is up to date again with the
     * material set.
     */
    private void updateIntSet()
    {
        if (this.intSetUpToDate)
        {
            // Already up to date
            return;
        }

        // Update the int set
        this.materialIntSet = new int[this.materials.size()];
        int i = 0;
        for (MaterialSetEntry entry : this.materials)
        {
            this.materialIntSet[i] = entry.hashCode();
            i++;
        }
        // Sort int set so that we can use Arrays.binarySearch
        Arrays.sort(this.materialIntSet);
        this.intSetUpToDate = true;
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
        if (material == null)
        {
            return false;
        }
        if (this.allMaterials && !material.isAir())
        {
            return true;
        }
        if (this.allSolidMaterials && material.isSolid())
        {
            return true;
        }
        if (this.allNonSolidMaterials && !material.isSolid())
        {
            return true;
        }

        // Try to update int set
        updateIntSet();

        // Check if the material is included
        if (Arrays.binarySearch(this.materialIntSet, material.hashCodeWithoutBlockData()) >= 0)
        {
            return true;
        }
        if (Arrays.binarySearch(this.materialIntSet, material.hashCode()) >= 0)
        {
            return true;
        }
        return false;
    }

    /**
     * Returns a comma (",") separated list of all materials in this set.
     * Keywords are left intact. No brackets ("[" or "]") are used at the
     * begin and end of the string.
     *
     * @return The string.
     */
    @Override
    public String toString()
    {
        // Check if all materials are included
        if (this.allMaterials)
        {
            return ALL_MATERIALS;
        }

        StringBuilder builder = new StringBuilder();
        // Check for solid materials
        if (this.allSolidMaterials)
        {
            builder.append(SOLID_MATERIALS).append(',');
        }
        // Check for non-solid materials
        if (this.allNonSolidMaterials)
        {
            builder.append(NON_SOLID_MATERIALS).append(',');
        }
        // Add all other materials
        for (MaterialSetEntry material : this.materials)
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
