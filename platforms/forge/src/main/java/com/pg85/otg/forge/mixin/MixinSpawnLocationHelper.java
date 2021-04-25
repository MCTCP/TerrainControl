package com.pg85.otg.forge.mixin;

import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import net.minecraft.entity.player.SpawnLocationHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnLocationHelper.class)
public class MixinSpawnLocationHelper
{
    @Inject(method = "getOverworldRespawnPos", at = @At("HEAD"), cancellable = true)
    private static void fixSpawningInGround(ServerWorld world, int x, int z, boolean needsValidSpawn, CallbackInfoReturnable<BlockPos> cir)
    {
        if (world.getChunkSource().generator instanceof OTGNoiseChunkGenerator)
        {
            Chunk chunk = world.getChunk(x >> 4, z >> 4);
            int topY = chunk.getHeight(Heightmap.Type.MOTION_BLOCKING, x & 15, z & 15);
            int surfaceY = chunk.getHeight(Heightmap.Type.WORLD_SURFACE, x & 15, z & 15);
            int floorY = chunk.getHeight(Heightmap.Type.OCEAN_FLOOR, x & 15, z & 15);

            // We ensure these 3 are the same to make sure that we're not spawning underwater or on a tree.
            if (topY == surfaceY && floorY == surfaceY)
            {
                cir.setReturnValue(new BlockPos(x, topY + 1, z));
            } else {
                cir.setReturnValue(null);
            }
        }
    }
}
