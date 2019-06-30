package com.pg85.otg.forge.gui.dimensions;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SettingEntry<T>
{
	public enum ValueType
	{
		Bool,
		Int,
		Double,
		String
	}
	
	public String name;
	public ValueType valueType;
	public T value;
	public T defaultValue;
	public T minValue;
	public T maxValue;
	public boolean newWorldOnly;
	public boolean overWorldOnly;

	public SettingEntry(String name, T value, T defaultValue, boolean newWorldOnly)
	{
		this(name, value, defaultValue, null, null, newWorldOnly, false);
	}
	
	public SettingEntry(String name, T value, T defaultValue, boolean newWorldOnly, boolean overWorldOnly)
	{
		this(name, value, defaultValue, null, null, newWorldOnly, overWorldOnly);
	}
	
	public SettingEntry(String name, T value, T defaultValue, T minValue, T maxValue, boolean newWorldOnly)
	{
		this(name, value, defaultValue, minValue, maxValue, newWorldOnly, false);
	}
	
	public SettingEntry(String name, T value, T defaultValue, T minValue, T maxValue, boolean newWorldOnly, boolean overWorldOnly)
	{
		this.name = name;
		this.value = value;
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.newWorldOnly = newWorldOnly;
		this.overWorldOnly = overWorldOnly;
			
		if(value == null)
		{
			valueType = ValueType.String;
		}							
		else if(value instanceof Integer)
		{
			valueType = ValueType.Int;
		}
		else if(value instanceof String)
		{
			valueType = ValueType.String;
		}
		else if(value instanceof Double)
		{
			valueType = ValueType.Double;
		}
		else if(value instanceof Boolean)
		{
			valueType = ValueType.Bool;
		} else {
			throw new RuntimeException("This should not happen, please contact team OTG about this crash.");
		}
	}
	
	public String getValueString()
	{
		if(this.valueType == ValueType.Double)
		{
			DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			df.setMaximumFractionDigits(340); // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
			return df.format((Double)this.value);
		}
		
		String output = 
			this.value == null ? "" :
			this.valueType == ValueType.Bool ? ((Boolean)this.value ? "On" : "Off") : 
			this.valueType == ValueType.Int ? (Integer)this.value + "" : 
			(String)this.value;
		
        return output != null ? output : "";     				
	}
}