package com.pg85.otg.customobject.structures;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.interfaces.IStructuredCustomObject;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Represents CustomObjects that can have other objects attached
 * to it making a structure.
 *
 */
public interface StructuredCustomObject extends CustomObject, IStructuredCustomObject
{
	ObjectType getType();
	CustomObjectConfigFile getConfig();
	BoundingBox getBoundingBox(Rotation north);
}
