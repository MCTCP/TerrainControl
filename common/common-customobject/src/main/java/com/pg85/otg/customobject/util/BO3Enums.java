package com.pg85.otg.customobject.util;

import com.pg85.otg.constants.Constants;

public class BO3Enums
{
	// The spawn height
	public static enum SpawnHeightEnum
	{
		randomY,
		highestBlock,
		highestSolidBlock
	}

	// How an object should be extended to a surface
	public static enum ExtrudeMode
	{
		None(-1, -1),
		BottomDown(Constants.WORLD_HEIGHT - 1, Constants.WORLD_DEPTH),
		TopUp(Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);

		/**
		 * Defines where calculation should begin
		 */
		private int startingHeight = 0;

		/**
		 * Defines where calculation should end
		 */
		private int endingHeight = 0;

		ExtrudeMode(int heightStart, int heightEnd)
		{
			this.startingHeight = heightStart;
			this.endingHeight = heightEnd;
		}

		public int getStartingHeight()
		{
			return startingHeight;
		}

		public int getEndingHeight()
		{
			return endingHeight;
		}
	}

	// What to do when outside the source block
	public static enum OutsideSourceBlock
	{
		dontPlace,
		placeAnyway
	}
}
