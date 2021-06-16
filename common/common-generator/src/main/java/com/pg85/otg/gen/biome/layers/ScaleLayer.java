package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

class ScaleLayer implements ParentedLayer
{
	public ScaleLayer() { }

	private int transformX(int x)
	{
	  return x >> 1;
	}

	private int transformZ(int y)
	{
	  return y >> 1;
	}

	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
	  // Optimized ScaleLayer implementation from zoom-layer

	  // Sample top left
	  int tl = parent.sample(this.transformX(x), this.transformZ(z));

	  // Get last bit of the x and z
	  int ix = x & 1;
	  int iz = z & 1;

	  if (ix == 0 && iz == 0) return tl;

	  context.initSeed(x & ~1, z & ~1);

	  // Only sample bottom left
	  if (ix == 0) {
		 int bl = parent.sample(this.transformX(x), this.transformZ(z + 1));
		 return context.choose(tl, bl);
	  }

	  // Only sample top right
	  if (iz == 0) {
		 int tr = parent.sample(this.transformX(x + 1), this.transformZ(z));
		 return context.choose(tl, tr);
	  }

	  // Perform regular sampling
	  int bl = parent.sample(this.transformX(x), this.transformZ(z + 1));
	  int tr = parent.sample(this.transformX(x + 1), this.transformZ(z));
	  int br = parent.sample(this.transformX(x + 1), this.transformZ(z + 1));

	  return this.sample(context, tl, tr, bl, br);
	}

	protected int sample(LayerSampleContext<?> context, int i, int j, int k, int l)
	{
	  if (j == k && k == l)
	  {
		 return j;
	  }
	  else if (i == j && i == k)
	  {
		 return i;
	  }
	  else if (i == j && i == l)
	  {
		 return i;
	  }
	  else if (i == k && i == l)
	  {
		 return i;
	  }
	  else if (i == j && k != l)
	  {
		 return i;
	  }
	  else if (i == k && j != l)
	  {
		 return i;
	  }
	  else if (i == l && j != k)
	  {
		 return i;
	  }
	  else if (j == k && i != l)
	  {
		 return j;
	  }
	  else if (j == l && i != k)
	  {
		 return j;
	  } else {
		 return k == l && i != j ? k : context.choose(i, j, k, l);
	  }
	}
}
