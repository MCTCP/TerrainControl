package com.pg85.otg.forge.mixin;

import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRespawnLogic.class)
public class MixinSpawnLocationHelper
{
	@Inject(method = "getOverworldRespawnPos", at = @At("HEAD"), cancellable = true)
	private static void fixSpawningInGround(ServerLevel world, int x, int z, boolean needsValidSpawn, CallbackInfoReturnable<BlockPos> cir)
	{
		if (world.getChunkSource().generator instanceof OTGNoiseChunkGenerator)
		{
			LevelChunk chunk = world.getChunk(x >> 4, z >> 4);
			int topY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15);
			int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);
			int floorY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x & 15, z & 15);

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
