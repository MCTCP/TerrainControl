package com.khorn.terraincontrol.generator.surface;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.generator.ChunkBuffer;
import com.khorn.terraincontrol.generator.GeneratingChunk;

/**
 * Minecraft 1.7 added block data support to the initial terrain generation.
 * CraftBukkit doesn't support this (yet), so all things with block data have
 * to be moved to the terrain population step.
 * <p />
 * The purpose of classes implementing this interface is to add the block data
 * to the surface blocks and the blocks just below that. At the moment, these
 * are the only blocks that require block data during the initial terrain
 * generation.
 * 
 */
public interface SurfaceGenerator
{

    /**
     * Spawns this surface layer in the world.
     * @param generatingChunk Information about the chunk being generated.
     * @param chunkBuffer     The chunk buffer.
     * @param biomeConfig     The biome config to use for the settings.
     * @param xInWorld        X position in the world.
     * @param zInWorld        Z position in the world.
     */
    void spawn(GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int xInWorld, int zInWorld);

    /**
     * Writes the settings used to a string. There must be a constructor to
     * read this string again.
     * 
     * @return The settings as a string.
     */
    String toString();

}