package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.HashSet;

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
    private HashSet<LocalMaterialData> materials = new HashSet<LocalMaterialData>();

    /**
     * Adds the given material to the list. If the material is "All", all
     * materials in existence are added to the list. If the material is
     * "Solid", all solid materials are added to the list.
     * 
     * @param material The name of the material to add.
     * @throws InvalidConfigException If the name is invalid.
     */
    public void parseAndAdd(String material) throws InvalidConfigException
    {
        if (material.equalsIgnoreCase(ALL_MATERIALS))
        {
            this.allMaterials = true;
            return;
        }
        if (material.equalsIgnoreCase(SOLID_MATERIALS))
        {
            this.allSolidMaterials = true;
            return;
        }
        materials.add(TerrainControl.readMaterial(material));
    }

    /**
     * Gets whether the specified material is in this collection. Returns false if the material is null.
     * @param material The material to check.
     * @return True if the material is in this set.
     */
    public boolean contains(LocalMaterialData material)
    {
        if (material == null)
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
        return materials.contains(material);
    }

    /**
     * Returns a comma (",") seperated list of all materials in this set.
     * Keywords are left intact. No brackets ("[" or "]") are used at the
     * begin and end of the string.
     * 
     * @return The string.
     */
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
        for (LocalMaterialData material : materials)
        {
            builder.append(material.getName()).append(',');
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
     * @return The new material set.
     */
    public MaterialSet rotate()
    {
        MaterialSet rotated = new MaterialSet();
        if (this.allMaterials) {
            rotated.allMaterials = true;
        }
        if (this.allSolidMaterials) {
            rotated.allSolidMaterials = true;
        }
        for (LocalMaterialData material : this.materials) {
            rotated.materials.add(material.rotate());
        }
        return rotated;
    }



}
