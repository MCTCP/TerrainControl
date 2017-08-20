package com.pg85.otg.forge.util;

import java.io.File;

public class IOHelper
{
	public static void deleteRecursive(File folder)
	{	
	    File[] files = folder.listFiles();
	    if(files!=null)
	    {
	    	//some JVMs return null for empty dirs
	        for(File f: files)
	        {
	            if(f.isDirectory())
	            {
	            	deleteRecursive(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}	
}
