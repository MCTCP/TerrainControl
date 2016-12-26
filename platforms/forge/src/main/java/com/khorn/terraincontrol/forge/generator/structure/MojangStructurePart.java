package com.khorn.terraincontrol.forge.generator.structure;

import java.util.Random;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.SpawnableObject;
import com.khorn.terraincontrol.util.Rotation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

/**
 * Makes a Minecraft {@link Template} useable as a
 * {@link SpawnableObject}.
 *
 */
public final class MojangStructurePart implements SpawnableObject
{
    private final Template spawnObject;
    private final String name;

    public MojangStructurePart(String name, Template spawnObject)
    {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(spawnObject, "spawnObject");
        this.name = name;
        this.spawnObject = spawnObject;
    }

    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        World worldMc = ((ForgeWorld) world).getWorld();
        BlockPos blockPos = new BlockPos(x, y, z);
        net.minecraft.util.Rotation rotationMc = toMinecraftRotation(rotation);

        PlacementSettings spawnSettings = new PlacementSettings().setRotation(rotationMc).setChunk(
                new ChunkPos(blockPos));
        this.spawnObject.addBlocksToWorld(worldMc, blockPos, spawnSettings);
        return true;
    }

    private net.minecraft.util.Rotation toMinecraftRotation(Rotation rotation)
    {
        switch (rotation)
        {
            case EAST:
                return net.minecraft.util.Rotation.CLOCKWISE_90;
            case NORTH:
                return net.minecraft.util.Rotation.NONE;
            case SOUTH:
                return net.minecraft.util.Rotation.CLOCKWISE_180;
            case WEST:
                return net.minecraft.util.Rotation.COUNTERCLOCKWISE_90;
            default:
                throw new IllegalArgumentException("Unknown rotation: " + rotation);
        }
    }

    @Override
    public String getName()
    {
        return this.name;
    }

}
