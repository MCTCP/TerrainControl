package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.configuration.settingType.Setting;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

@RunWith(JUnit4.class)
public class BracketSettingsReaderTest extends AbstractSettingsReaderTest
{

    @Override
    protected void cleanupCrumbs(SettingsReader reader)
    {
        // Empty!
    }

    @Override
    protected BracketSettingsReader getEmptyReader()
    {
        return new BracketSettingsReader("Test", "");
    }

    @Override
    protected <S> SettingsReader getExistingReader(Setting<S> setting, S value) throws IOException
    {
        return new BracketSettingsReader("Test", setting.getName() + "=" + setting.write(value));
    }
}
