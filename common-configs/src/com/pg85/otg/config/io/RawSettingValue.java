package com.pg85.otg.config.io;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.pg85.otg.config.settingType.Setting;

/**
 * Information about the unparsed value of a setting. It contains what was found
 * in a config file: the line nubmer, the setting value and the comments.
 */
public final class RawSettingValue
{
    /**
     * The type of a setting value.
     */
    public enum ValueType
    {
        PLAIN_SETTING,
        FUNCTION,
        BIG_TITLE,
        SMALL_TITLE
    }

    private final List<String> comments;
    private final int line;
    private final String value;
    private final ValueType valueType;
    
    private RawSettingValue(String value, ValueType valueType, int line, List<String> comments)
    {
        this.value = value;
        this.valueType = valueType;
        this.line = line;
        this.comments = comments;
    }
	
    /**
     * Creates a new setting value object.
     * @param valueType    The type of the setting.
     * @param settingValue The raw value of the setting, like "Foo: Bar" or
     *                     "Foo(Bar)".
     * @return The setting.
     */
    static RawSettingValue create(ValueType valueType, String settingValue)
    {
        if (settingValue == null || valueType == null)
        {
            throw new NullPointerException();
        }
        return new RawSettingValue(settingValue, valueType, -1, Collections.<String> emptyList());
    }

    /**
     * Creates a new setting value object for the given setting and value.
     * @param setting The setting.
     * @param value   The value.
     * @return The setting value object.
     */
    static <S> RawSettingValue ofPlainSetting(Setting<S> setting, S value)
    {
        return ofPlainSetting(setting.getName(), setting.write(value));
    }

    /**
     * Creates a new setting value object for the given setting and value.
     * @param settingName The setting name.
     * @param value       The unparsed value.
     * @return The setting value object.
     */
    private static <S> RawSettingValue ofPlainSetting(String settingName, String value)
    {
        return create(ValueType.PLAIN_SETTING, settingName + ": " + value);
    }

    /**
     * Gets all comments.
     * @return The comments.
     */
    public List<String> getComments()
    {
        return this.comments;
    }

    /**
     * Gets the line number this setting appears on, or -1 if unknown. The first
     * line in a file has a line number of 1.
     * @return The line number.
     */
    public int getLineNumber()
    {
        return line;
    }

    /**
     * Gets the raw value of this setting.
     * @return The raw value.
     */
    public String getRawValue()
    {
        return value;
    }

    /**
     * Gets the value type of this setting.
     * @return The value type.
     */
    public ValueType getType()
    {
        return valueType;
    }

    /**
     * Creates a new {@link RawSettingValue} with the specified comments.
     * @param comments The comments.
     * @return The new {@link RawSettingValue}.
     */
    RawSettingValue withComments(String... comments)
    {
        if (comments.length == 0 && this.comments.isEmpty())
        {
            // No change in comments
            return this;
        }

        List<String> commentsList = Collections.unmodifiableList(Arrays.asList(comments));
        return new RawSettingValue(value, valueType, line, commentsList);
    }

    /**
     * Sets the line number the setting is on. Useful for error messages.
     * @param lineNumber The line number, must be positive. The first line has
     * 1 as the line number.
     * @return This object, for chaining.
     */
    RawSettingValue withLineNumber(int lineNumber)
    {
        if (lineNumber <= 0)
        {
            throw new IllegalArgumentException("Invalid line number: " + lineNumber);
        }
        return new RawSettingValue(this.value, valueType, lineNumber, this.comments);
    }

}
