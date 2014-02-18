package com.khorn.terraincontrol;

import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

/**
 * Represents one of Minecraft's materials. Also includes its data value.
 * Immutable.
 * 
 * @see TerrainControlEngine#readMaterial(String)
 * @see TerrainControlEngine#toLocalMaterialData(DefaultMaterial, int)
 */
public interface LocalMaterialData
{

    /**
     * Gets the name of this material. If a {@link #toDefaultMaterial()
     * DefaultMaterial is available,} that name is used, otherwise it's up to
     * the mod that provided this block to name it. Block data is appended to
     * the name, separated with a colon, like "WOOL:2".
     * 
     * @return The name of this material.
     */
    String getName();

    /**
     * Same as {@link #getName()}.
     * 
     * @return The name of this material.
     */
    @Override
    String toString();

    /**
     * Gets the internal block id. At the moment, all of Minecraft's vanilla
     * materials have a static id, but this can change in the future. Mods
     * already have dynamic ids.
     * 
     * @return The internal block id.
     */
    int getBlockId();

    /**
     * Gets the internal block data. Block data represents things like growth
     * stage and rotation.
     * 
     * @return The internal block data.
     */
    byte getBlockData();

    /**
     * Gets whether this material is a liquid, like water or lava.
     * 
     * @return True if this material is a liquid, false otherwise.
     */
    boolean isLiquid();

    /**
     * Gets whether this material is solid. If there is a
     * {@link #toDefaultMaterial() DefaultMaterial available}, this property is
     * defined by {@link DefaultMaterial#isSolid()}. Otherwise, it's up to the
     * mod that provided this block to say whether it's solid or not.
     * 
     * @return True if this material is solid, false otherwise.
     */
    boolean isSolid();

    /**
     * Gets the default material belonging to this material. The block data will
     * be lost. If the material is not one of the vanilla Minecraft materials,
     * {@link DefaultMaterial#UNKNOWN_BLOCK} is returned.
     * 
     * @return The default material.
     */
    DefaultMaterial toDefaultMaterial();

    /**
     * Gets whether snow can fall on this block.
     * 
     * @return True if snow can fall on this block, false otherwise.
     */
    boolean canSnowFallOn();

    /**
     * Gets whether the block is of the given material. Block data is ignored,
     * as {@link DefaultMaterial} doesn't include block data.
     * 
     * @param material
     *            The material to check.
     * @return True if this block is of the given material, false otherwise.
     */
    boolean isMaterial(DefaultMaterial material);

    /**
     * Gets an new instance with the block data changed.
     * 
     * @param newData
     *            The new block data.
     * @return The new instance.
     */
    LocalMaterialData withBlockData(int newData);

    /**
     * Gets whether this material equals another material. The block data is
     * taken into account.
     * 
     * @param other
     *            The other material.
     * @return True if the materials are equal, false otherwise.
     */
    @Override
    boolean equals(Object other);

    /**
     * Gets the hashCode of the material, based on the block id and block data.
     * The hashCode must be unique, which is possible considering that there are
     * only 4096 * 16 possible materials.
     * 
     * @return The unique hashCode.
     */
    @Override
    int hashCode();

    /**
     * Gets the hashCode of the material, based on only the block id. No
     * hashCode returned by this method may be the same as any hashCode returned
     * by {@link #hashCode()}.
     * 
     * @return The unique hashCode.
     */
    int hashCodeWithoutBlockData();

    /**
     * Gets a new material that is rotated 90 degrees. North -> west -> south ->
     * east. If this material cannot be rotated, the material itself is
     * returned.
     * 
     * @return The rotated material.
     */
    LocalMaterialData rotate();

}
