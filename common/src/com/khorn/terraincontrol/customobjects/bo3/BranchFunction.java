package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.customobjects.Rotation;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BranchFunction extends BO3Function implements Branch
{
    public int x;
    public int y;
    public int z;
    public List<CustomObject> branches;
    public List<Integer> branchChances;
    public List<Rotation> branchRotations;

    @Override
    public BranchFunction rotate()
    {
        BranchFunction rotatedBranch = new BranchFunction();
        rotatedBranch.x = z;
        rotatedBranch.y = y;
        rotatedBranch.z = -x;
        rotatedBranch.branches = branches;
        rotatedBranch.branchChances = branchChances;
        rotatedBranch.branchRotations = new ArrayList<Rotation>();
        for (Rotation rotation : branchRotations)
        {
            rotatedBranch.branchRotations.add(Rotation.next(rotation));
        }
        return rotatedBranch;
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(6, args);

        x = readInt(args.get(0), -32, 32);
        y = readInt(args.get(1), -64, 64);
        z = readInt(args.get(2), -32, 32);
        branches = new ArrayList<CustomObject>();
        branchRotations = new ArrayList<Rotation>();
        branchChances = new ArrayList<Integer>();
        for (int i = 3; i < args.size() - 2; i += 3)
        {
            CustomObject object = getHolder().otherObjectsInDirectory.get(args.get(i).toLowerCase());
            if (object == null)
            {
                throw new InvalidConfigException("The branch " + args.get(i) + " was not found. Make sure to place it in the same directory.");
            }
            branches.add(object);
            branchRotations.add(Rotation.getRotation(args.get(i + 1)));
            branchChances.add(readInt(args.get(i + 2), 1, 100));
        }
    }

    @Override
    public String makeString()
    {
        String output = "Branch(" + x + "," + y + "," + z;
        for (int i = 0; i < branches.size(); i++)
        {
            output += "," + branches.get(i).getName() + "," + branchRotations.get(i) + "," + branchChances.get(i);
        }
        return output + ")";
    }

    @Override
    public CustomObjectCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, int x, int y, int z)
    {
        for (int branchNumber = 0; branchNumber < branches.size(); branchNumber++)
        {
            if (random.nextInt(100) < branchChances.get(branchNumber))
            {
                return new CustomObjectCoordinate(branches.get(branchNumber), branchRotations.get(branchNumber), x + this.x, y + this.y, z + this.z);
            }
        }

        return null;
    }

}
