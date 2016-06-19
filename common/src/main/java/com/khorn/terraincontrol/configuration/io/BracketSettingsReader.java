package com.khorn.terraincontrol.configuration.io;

/**
 * Reads settings from a formatted string like:
 * <code>(SettingName=SettingValue;SettingName2=SettingValue2)</code>.
 *
 */
public class BracketSettingsReader
{

    /**
     * Reads the rawSettings, adds it to settingsMap.
     * @param settingsMap The settings map.
     * @param rawSettings The raw settings, formateed like 
     * <code>(SettingName=SettingValue;SettingName2=SettingValue2)</code>.
     */
    public static void readInto(SettingsMap settingsMap, String rawSettings)
    {
        String[] settings = rawSettings.split(";");
        for (String setting : settings)
        {
            String[] settingParts = setting.split("=", 2);
            String settingName = settingParts[0].toLowerCase().trim();
            if (settingName.isEmpty())
            {
                continue;
            }

            if (settingParts.length == 1)
            {
                // Boolean values
                RawSettingValue settingValue = RawSettingValue.ofPlainSetting(settingName, "true");
                settingsMap.addRawSetting(settingValue);
            } else if (settingParts.length == 2)
            {
                RawSettingValue settingValue = RawSettingValue.ofPlainSetting(settingName, settingParts[1].trim());
                settingsMap.addRawSetting(settingValue);
            }
        }
    }

}
