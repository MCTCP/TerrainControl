package com.pg85.otg.forge.world;

import java.util.ArrayList;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.WorldSession;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.forge.pregenerator.Pregenerator;
import com.pg85.otg.util.ChunkCoordinate;

public class ForgeWorldSession extends WorldSession
{
	private ArrayList<ParticleFunction<?>> particleFunctions = new ArrayList<ParticleFunction<?>>();
	private Pregenerator pregenerator;

	public ForgeWorldSession(LocalWorld world)
	{
		super(world);
		pregenerator = new Pregenerator(world);
	}

	public Pregenerator getPregenerator()
	{
		return pregenerator;
	}

	@Override
	public ArrayList<ParticleFunction<?>> getParticleFunctions()
	{
		return particleFunctions;
	}

	@Override
	public int getPregenerationRadius()
	{
		return pregenerator.getPregenerationRadius();
	}

	@Override
	public int setPregenerationRadius(int value)
	{
		return pregenerator.setPregenerationRadius(value);
	}

	@Override
	public int getPregeneratedBorderLeft()
	{
		return pregenerator.getPregenerationBorderLeft();
	}

	@Override
	public int getPregeneratedBorderRight()
	{
		return pregenerator.getPregenerationBorderRight();
	}

	@Override
	public int getPregeneratedBorderTop()
	{
		return pregenerator.getPregenerationBorderTop();
	}

	@Override
	public int getPregeneratedBorderBottom()
	{
		return pregenerator.getPregenerationBorderBottom();
	}

	@Override
	public void setPreGeneratorCenterPoint(ChunkCoordinate chunkCoord)
	{
		pregenerator.setPreGeneratorCenterPoint(chunkCoord);
	}
	
	@Override
	public ChunkCoordinate getPreGeneratorCenterPoint()
	{
		return pregenerator.getPregenerationCenterPoint();
	}

	@Override
	public boolean getPreGeneratorIsRunning()
	{
		return pregenerator.getPregeneratorIsRunning();
	}
}
