package com.pg85.otg.bukkit.world;

import java.util.ArrayList;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.WorldSession;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.util.ChunkCoordinate;

public class BukkitWorldSession extends WorldSession
{
	private ArrayList<ParticleFunction<?>> ParticleFunctions = new ArrayList<ParticleFunction<?>>();

	BukkitWorldSession(LocalWorld world)
	{
		super(world);
	}

	@Override
	public ArrayList<ParticleFunction<?>> getParticleFunctions()
	{
		return ParticleFunctions;
	}

	@Override
	public int getPregenerationRadius()
	{
		return 0;
	}

	@Override
	public int setPregenerationRadius(int value)
	{
		return 0;
	}

	@Override
	public int getPregeneratedBorderLeft()
	{
		return 0;
	}

	@Override
	public int getPregeneratedBorderRight()
	{
		return 0;
	}

	@Override
	public int getPregeneratedBorderTop()
	{
		return 0;
	}

	@Override
	public int getPregeneratedBorderBottom()
	{
		return 0;
	}

	@Override
	public void setPreGeneratorCenterPoint(ChunkCoordinate chunkCoord) { }
	
	@Override
	public ChunkCoordinate getPreGeneratorCenterPoint()
	{
		return ChunkCoordinate.fromBlockCoords(0, 0);
	}

	@Override
	public boolean getPreGeneratorIsRunning()
	{
		return false;
	}
}
