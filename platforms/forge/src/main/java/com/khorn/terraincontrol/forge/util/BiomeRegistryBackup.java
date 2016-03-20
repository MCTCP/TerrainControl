package com.khorn.terraincontrol.forge.util;

import com.google.common.collect.ImmutableList;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.commons.lang3.Validate;

/**
 * Represents an immutable backup of the vanilla biomes in
 * {@link BiomeGenBase#biomeRegistry}.
 *
 */
public final class BiomeRegistryBackup
{
    private static class BiomeBackup
    {
        final BiomeGenBase biome;
        final ResourceLocation biomeName;
        final int biomeId;

        BiomeBackup(int id, ResourceLocation biomeName, BiomeGenBase biome)
        {
            this.biomeId = id;
            this.biomeName = Validate.notNull(biomeName);
            this.biome = Validate.notNull(biome);
        }
    }

    private final ImmutableList<BiomeBackup> backup;

    public static final BiomeRegistryBackup EMPTY_BACKUP = new BiomeRegistryBackup();

    /**
     * Creates a backup of all the vanilla biomes in the given registry.
     *
     * @param registry The registry.
     */
    public BiomeRegistryBackup(RegistryNamespaced<ResourceLocation, BiomeGenBase> registry)
    {
        ImmutableList.Builder<BiomeBackup> builder = ImmutableList.builder();
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            int biomeId = defaultBiome.Id;
            BiomeGenBase biome = registry.getObjectById(biomeId);
            ResourceLocation biomeName = registry.getNameForObject(biome);
            BiomeBackup biomeBackup = new BiomeBackup(biomeId, biomeName, biome);
            builder.add(biomeBackup);
        }
        this.backup = builder.build();
    }

    /**
     * Creates an empty backup.
     *
     * @see #EMPTY_BACKUP
     */
    private BiomeRegistryBackup()
    {
        this.backup = ImmutableList.of();
    }

    /**
     * Restores this backup to the given registry.
     *
     * @param registry The registry to restore into.
     */
    public void restore(RegistryNamespaced<ResourceLocation, BiomeGenBase> registry)
    {
        for (BiomeBackup biomeBackup : this.backup)
        {
            registry.register(biomeBackup.biomeId, biomeBackup.biomeName, biomeBackup.biome);
        }
    }
}
