package com.pg85.otg.customobjects.bo4;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import com.pg85.otg.config.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;

public class BO4Data
{
    public static boolean bo4DataExists(BO4Config config)
    {
		String filePath = 
			config.getFile().getAbsolutePath().endsWith(".BO4") ? config.getFile().getAbsolutePath().replace(".BO4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo4") ? config.getFile().getAbsolutePath().replace(".bo4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".BO3") ? config.getFile().getAbsolutePath().replace(".BO3", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo3") ? config.getFile().getAbsolutePath().replace(".bo3", ".BO4Data") :
			config.getFile().getAbsolutePath();

        File file = new File(filePath);
        return file.exists();
    }
    
    public static void generateBO4Data(BO4Config config, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        //write to disk
		String filePath = 
			config.getFile().getAbsolutePath().endsWith(".BO4") ? config.getFile().getAbsolutePath().replace(".BO4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo4") ? config.getFile().getAbsolutePath().replace(".bo4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".BO3") ? config.getFile().getAbsolutePath().replace(".BO3", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo3") ? config.getFile().getAbsolutePath().replace(".bo3", ".BO4Data") :
			config.getFile().getAbsolutePath();

        File file = new File(filePath);
        if(!file.exists())
        {
            try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				config.writeToStream(dos, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
				byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray(), spawnLog, logger);
				dos.close();
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos2 = new DataOutputStream(fos);
				dos2.write(compressedBytes, 0, compressedBytes.length);
				dos2.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
