package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

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
    private static String ALL_MATERIALS = "All";
    private static String SOLID_MATERIALS = "Solid";

    private boolean allSolidMaterials = false;
    private boolean allMaterials = false;

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

        LocalMaterialData material = TerrainControl.readMaterial(input);

        boolean checkIncludesBlockData = StringHelper.specifiesBlockData(input);

        // Add to set
        add(new MaterialSetEntry(material, checkIncludesBlockData));
    }

    /**
     * Adds the entry to this material set.
     * @param entry The entry to add, may not be null.
     */
    public void add(MaterialSetEntry entry)
    {
        // Add the appropriate hashCode
        intSetUpToDate = false;
        materials.add(entry);
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
        if (material == null)
        {
            return false;
        }
        if (allMaterials && !material.isMaterial(DefaultMaterial.AIR))
        {
            return true;
        }
        if (allSolidMaterials && material.isSolid())
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
        rotated.intSetUpToDate = false;
        for (MaterialSetEntry material : this.materials)
        {
            rotated.materials.add(material.rotate());
        }
        return rotated;
    }

}
