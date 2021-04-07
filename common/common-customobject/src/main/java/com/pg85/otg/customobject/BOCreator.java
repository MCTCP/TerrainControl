package com.pg85.otg.customobject;

import com.pg85.otg.util.bo3.NamedBinaryTag;

public abstract class BOCreator
{
	protected static String getTileEntityName(NamedBinaryTag tag)
	{
		NamedBinaryTag idTag = tag.getTag("id");
		if (idTag != null)
		{
			String name = (String) idTag.getValue();

			return name.replace("minecraft:", "").replace(':', '_');
		}
		return "Unknown";
	}

	public static class Corner
	{
		public final int x;
		public final int y;
		public final int z;

		public Corner(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
