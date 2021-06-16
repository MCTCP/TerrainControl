package com.pg85.otg.forge.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.LocateCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocateCommand.class)
public abstract class MixinLocateCommand
{

	@Shadow
	public static int showLocateResult(CommandSource p_241054_0_, String p_241054_1_, BlockPos p_241054_2_, BlockPos p_241054_3_, String p_241054_4_)
	{
		return 0;
	}

	@Shadow @Final private static SimpleCommandExceptionType ERROR_FAILED;

	@Inject(method = "locate", at = @At("HEAD"), cancellable = true)
	private static void searchInSmallerRadius(CommandSource source, Structure<?> structure, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException
	{
		if (source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator)
		{
			BlockPos blockpos = new BlockPos(source.getPosition());
			BlockPos blockpos1 = source.getLevel().findNearestMapFeature(structure, blockpos, 20, false);
			if (blockpos1 == null)
			{
				throw ERROR_FAILED.create();
			} else {
				int ret = showLocateResult(source, structure.getFeatureName(), blockpos, blockpos1, "commands.locate.success");
				cir.setReturnValue(ret);
			}
		}
	}
}
