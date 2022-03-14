package com.pg85.otg.gen.biome.layers.util;

import java.util.Arrays;

import com.pg85.otg.interfaces.ILayerSampler;
import com.pg85.otg.util.helpers.MathHelper;

import it.unimi.dsi.fastutil.HashCommon;

public final class CachingLayerSampler implements ILayerSampler
{
	private final ThreadLocal<SamplerBiomeCache> cache;
	private final int cacheCapacity;

	CachingLayerSampler(int cacheCapacity, LayerOperator operator)
	{
		this.cache = ThreadLocal.withInitial(() -> new SamplerBiomeCache(operator, cacheCapacity));
		this.cacheCapacity = cacheCapacity;
	}

	public int sample(int x, int z)
	{
		return this.cache.get().get(x, z);
	}

	public int getCapacity()
	{
		return this.cacheCapacity;
	}

	private static class SamplerBiomeCache {
		private final long[] keys;
		private final int[] values;
		private final int mask;
		private final LayerOperator operator;

		private SamplerBiomeCache(LayerOperator operator, int size) {
			this.operator = operator;

			size = MathHelper.smallestEncompassingPowerOfTwo(size);
			this.mask = size - 1;

			this.keys = new long[size];
			Arrays.fill(this.keys, Long.MIN_VALUE);
			this.values = new int[size];
		}

		public int get(int x, int z) {
			long key = key(x, z);
			int idx = hash(key) & this.mask;

			// if the entry here has a key that matches ours, we have a cache hit
			if (this.keys[idx] == key)
			{
				return this.values[idx];
			} else {
				// cache miss: sample and put the result into our cache entry

				int value = this.operator.apply(x, z);
				this.keys[idx] = key;
				this.values[idx] = value;

				return value;
			}
		}

		private int hash(long key)
		{
			return (int) HashCommon.mix(key);
		}

		private long key(int x, int z)
		{
			return MathHelper.toLong(x, z);
		}
	}
}
