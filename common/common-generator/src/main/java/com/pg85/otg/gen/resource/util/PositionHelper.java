package com.pg85.otg.gen.resource.util;

import java.util.Random;

import com.google.common.collect.AbstractIterator;

public class PositionHelper
{
	public static Iterable<int[]> randomBetweenClosed(Random random, int limit, int x1, int y1, int z1, int x2, int y2,
			int z2)
	{
		int width = x2 - x1 + 1;
		int height = y2 - y1 + 1;
		int depth = z2 - z1 + 1;

		return () -> new AbstractIterator<int[]>()
		{
			int counter = limit;

			protected int[] computeNext()
			{
				if (this.counter <= 0)
					return endOfData();

				int[] pos = new int[]
				{ x1 + random.nextInt(width), y1 + random.nextInt(height), z1 + random.nextInt(depth) };

				this.counter--;
				return pos;
			}
		};
	}

	public static Iterable<int[]> betweenClosed(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		int width = x2 - x1 + 1;
		int height = y2 - y1 + 1;
		int depth = z2 - z1 + 1;
		int end = width * height * depth;

		return () -> new AbstractIterator<int[]>()
		{
			private int index;

			protected int[] computeNext()
			{
				if (this.index == end)
					return endOfData();

				int lvt_1_1_ = this.index % width;
				int lvt_2_1_ = this.index / width;
				int lvt_3_1_ = lvt_2_1_ % height;
				int lvt_4_1_ = lvt_2_1_ / height;
				this.index++;

				return new int[]
				{ x1 + lvt_1_1_, y1 + lvt_3_1_, z1 + lvt_4_1_ };
			}
		};
	}

	public static int distManhattan(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		float lvt_2_1_ = Math.abs(x2 - x1);
		float lvt_3_1_ = Math.abs(y2 - y1);
		float lvt_4_1_ = Math.abs(z2 - z1);
		return (int) (lvt_2_1_ + lvt_3_1_ + lvt_4_1_);
	}
}
