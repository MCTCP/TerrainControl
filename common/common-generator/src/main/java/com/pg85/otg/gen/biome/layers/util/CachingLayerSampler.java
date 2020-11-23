package com.pg85.otg.gen.biome.layers.util;

import com.pg85.otg.util.helpers.MathHelper;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;

public final class CachingLayerSampler implements LayerSampler
{
	private final LayerOperator operator;
	private final Long2IntLinkedOpenHashMap cache;
	private final int cacheCapacity;

	CachingLayerSampler(Long2IntLinkedOpenHashMap cache, int cacheCapacity, LayerOperator operator)
	{
		this.cache = cache;
		this.cacheCapacity = cacheCapacity;
		this.operator = operator;
	}

	// TODO: fixed size lossy cache
	public int sample(int x, int z)
	{
		long l = MathHelper.toLong(x, z);
		synchronized (this.cache)
		{
			int i = this.cache.get(l);
			if (i != Integer.MIN_VALUE)
			{
				return i;
			} else {
				int j = this.operator.apply(x, z);
				this.cache.put(l, j);
				if (this.cache.size() > this.cacheCapacity)
				{
					for (int k = 0; k < this.cacheCapacity / 16; ++k)
					{
						this.cache.removeFirstInt();
					}
				}
				return j;
			}
		}
	}

	public int getCapacity()
	{
		return this.cacheCapacity;
	}
}
