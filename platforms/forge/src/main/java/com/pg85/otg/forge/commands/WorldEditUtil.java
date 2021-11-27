package com.pg85.otg.forge.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

public class WorldEditUtil
{
	public static RegionCommand.Region getRegionFromPlayer(ServerPlayer playerEntity)
	{
		if (!WorldEdit.getInstance().getSessionManager().contains(ForgeAdapter.adaptPlayer(playerEntity)))
		{
			return null;
		}
		try
		{
			com.sk89q.worldedit.regions.Region weRegion = WorldEdit.getInstance().getSessionManager()
				.get(ForgeAdapter.adaptPlayer(playerEntity))
				.getSelection(ForgeAdapter.adapt(playerEntity.getLevel()));
			RegionCommand.Region region = new RegionCommand.Region();
			region.setPos(getPosFromVector3(weRegion.getMinimumPoint()));
			region.setPos(getPosFromVector3(weRegion.getMaximumPoint()));
			return region;
		}
		catch (IncompleteRegionException e)
		{
			return null;
		}
	}

	private static BlockPos getPosFromVector3(BlockVector3 vector)
	{
		return new BlockPos(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
	}
}
