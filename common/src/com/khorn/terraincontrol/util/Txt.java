package com.khorn.terraincontrol.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Txt
{	
	// -------------------------------------------- //
	// STATIC
	// -------------------------------------------- //
	
	public static final Map<String, String> parseReplacements;
	public static final Pattern parsePattern;
	
	static
	{		
		// Create the parce replacements map
		parseReplacements = new HashMap<String, String>();
		
		// Color by name
		parseReplacements.put("<empty>", "");
		parseReplacements.put("<black>", "\u00A70");
		parseReplacements.put("<navy>", "\u00A71");
		parseReplacements.put("<green>", "\u00A72");
		parseReplacements.put("<teal>", "\u00A73");
		parseReplacements.put("<red>", "\u00A74");
		parseReplacements.put("<purple>", "\u00A75");
		parseReplacements.put("<gold>", "\u00A76");
		parseReplacements.put("<silver>", "\u00A77");
		parseReplacements.put("<gray>", "\u00A78");
		parseReplacements.put("<blue>", "\u00A79");
		parseReplacements.put("<lime>", "\u00A7a");
		parseReplacements.put("<aqua>", "\u00A7b");
		parseReplacements.put("<rose>", "\u00A7c");
		parseReplacements.put("<pink>", "\u00A7d");
		parseReplacements.put("<yellow>", "\u00A7e");
		parseReplacements.put("<white>", "\u00A7f");
		
		// Color by semantic functionality
		parseReplacements.put("<l>", "\u00A72");
		parseReplacements.put("<logo>", "\u00A72");
		parseReplacements.put("<a>", "\u00A76");
		parseReplacements.put("<art>", "\u00A76");
		parseReplacements.put("<n>", "\u00A77");
		parseReplacements.put("<notice>", "\u00A77");
		parseReplacements.put("<i>", "\u00A7e");
		parseReplacements.put("<info>", "\u00A7e");
		parseReplacements.put("<g>", "\u00A7a");
		parseReplacements.put("<good>", "\u00A7a");
		parseReplacements.put("<b>", "\u00A7c");
		parseReplacements.put("<bad>", "\u00A7c");
		
		parseReplacements.put("<k>", "\u00A7b");
		parseReplacements.put("<key>", "\u00A7b");
		
		parseReplacements.put("<v>", "\u00A7d");
		parseReplacements.put("<value>", "\u00A7d");
		parseReplacements.put("<h>", "\u00A7d");
		parseReplacements.put("<highlight>", "\u00A7d");
		
		parseReplacements.put("<c>", "\u00A7b");
		parseReplacements.put("<command>", "\u00A7b");
		parseReplacements.put("<p>", "\u00A73");
		parseReplacements.put("<parameter>", "\u00A73");
		parseReplacements.put("&&", "&");
		
		// Color by number/char
		for (int i = 48; i <= 122; i++)
		{
			char c = (char)i;
			parseReplacements.put("§"+c, "\u00A7"+c);
			parseReplacements.put("&"+c, "\u00A7"+c);
			if (i == 57) i = 96;
		}
		
		// Build the parse pattern and compile it
		StringBuilder patternStringBuilder = new StringBuilder();
		for (String find : parseReplacements.keySet())
		{
			patternStringBuilder.append('(');
			patternStringBuilder.append(Pattern.quote(find));
			patternStringBuilder.append(")|");
		}
		String patternString = patternStringBuilder.toString();
		patternString = patternString.substring(0, patternString.length()-1); // Remove the last |
		parsePattern = Pattern.compile(patternString);
	}
	
	// -------------------------------------------- //
	// CONSTRUCTOR (FORBIDDEN)
	// -------------------------------------------- //
	
	private Txt()
	{
		
	}
	
	// -------------------------------------------- //
	// PARSE
	// -------------------------------------------- //
	
	public static String parse(String string)
	{
		StringBuffer ret = new StringBuffer();
		Matcher matcher = parsePattern.matcher(string);
		while (matcher.find())
		{
			matcher.appendReplacement(ret, parseReplacements.get(matcher.group(0)));
		}
		matcher.appendTail(ret);
		return ret.toString();
	}
	
	public static String parse(String string, Object... args)
	{
		return String.format(parse(string), args);
	}
	
	public static ArrayList<String> parse(Collection<String> strings)
	{
		ArrayList<String> ret = new ArrayList<String>(strings.size());
		for (String string : strings)
		{
			ret.add(parse(string));
		}
		return ret;
	}
	
	// -------------------------------------------- //
	// Standard utils like UCFirst, implode and repeat.
	// -------------------------------------------- //
	
	public static String upperCaseFirst(String string)
	{
		return string.substring(0, 1).toUpperCase()+string.substring(1);
	}
	
	public static String repeat(String string, int times)
	{
	    if (times <= 0) return "";
	    else return string + repeat(string, times-1);
	}
	
	public static String implode(final Collection<? extends Object> coll, final String glue)
	{
		return implode(coll.toArray(new Object[0]), glue);
	}
	
	public static String implode(final Object[] list, final String glue)
	{
	    StringBuilder ret = new StringBuilder();
	    for (int i=0; i<list.length; i++)
	    {
	        if (i!=0)
	        {
	        	ret.append(glue);
	        }
	        ret.append(list[i]);
	    }
	    return ret.toString();
	}
	
	public static String implodeCommaAndDot(final Collection<? extends Object> objects, final String comma, final String and, final String dot)
	{
	    if (objects.size() == 0) return "";
		if (objects.size() == 1) return objects.iterator().next().toString();
		
		List<Object> ourObjects = new ArrayList<Object>(objects);
		
		String lastItem = ourObjects.get(ourObjects.size()-1).toString();
		String nextToLastItem = ourObjects.get(ourObjects.size()-2).toString();
		String merge = nextToLastItem+and+lastItem;
		ourObjects.set(ourObjects.size()-2, merge);
		ourObjects.remove(ourObjects.size()-1);
		
		return implode(ourObjects, comma)+dot;
	}
	public static String implodeCommaAnd(final Collection<? extends Object> objects, final String comma, final String and)
	{
		return implodeCommaAndDot(objects, comma, and, "");
	}
	public static String implodeCommaAndDot(final Collection<? extends Object> objects, final String color)
	{
	    return implodeCommaAndDot(objects, color+", ", color+" and ", color+".");
	}
	public static String implodeCommaAnd(final Collection<? extends Object> objects, final String color)
	{
	    return implodeCommaAndDot(objects, color+", ", color+" and ", "");
	}
	public static String implodeCommaAndDot(final Collection<? extends Object> objects)
	{
		return implodeCommaAndDot(objects, "");
	}
	public static String implodeCommaAnd(final Collection<? extends Object> objects)
	{
		return implodeCommaAnd(objects, "");
	}
	
	public static Integer indexOfFirstDigit(final String str)
	{
		Integer ret = null;
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			boolean isDigit = (c >= '0' && c <= '9');
			if (isDigit)
			{
				ret = i;
				break;
			}
		}
		return ret;
	}
	
	// -------------------------------------------- //
	// Paging and chrome-tools like titleize
	// -------------------------------------------- //
	
	private final static String titleizeLine = repeat("_", 52);
	private final static int titleizeBalance = -1;
	public static String titleize(String str)
	{
		String center = ".[ "+ parse("<l>") + str + parse("<a>") + " ].";
		int centerlen = center.length();
		int pivot = titleizeLine.length() / 2;
		int eatLeft = (centerlen / 2) - titleizeBalance;
		int eatRight = (centerlen - eatLeft) + titleizeBalance;

		if (eatLeft < pivot)
			return parse("<a>")+titleizeLine.substring(0, pivot - eatLeft) + center + titleizeLine.substring(pivot + eatRight);
		else
			return parse("<a>")+center;
	}
	
	public static ArrayList<String> getPage(List<String> lines, int pageHumanBased, String title)
	{
		ArrayList<String> ret = new ArrayList<String>();
		int pageZeroBased = pageHumanBased - 1;
		int pageheight = 9;
		int pagecount = (int)Math.ceil(((double)lines.size()) / pageheight);
		
		ret.add(titleize(title+parse("<a>")+" "+pageHumanBased+"/"+pagecount));
		
		if (pagecount == 0)
		{
			ret.add(parse("<i>Sorry. No Pages available."));
			return ret;
		}
		else if (pageZeroBased < 0 || pageHumanBased > pagecount)
		{
			ret.add(parse("<i>Invalid page. Must be between 1 and "+pagecount));
			return ret;
		}
		
		int from = pageZeroBased * pageheight;
		int to = from+pageheight;
		if (to > lines.size())
		{
			to = lines.size();
		}
		
		ret.addAll(lines.subList(from, to));
		
		return ret;
	}
	
	// -------------------------------------------- //
	// String comparison
	// -------------------------------------------- //
	
	public static String getBestCIStart(Collection<String> candidates, String start)
	{
		String ret = null;
		int best = 0;
		
		start = start.toLowerCase();
		int minlength = start.length();
		for (String candidate : candidates)
		{
			if (candidate.length() < minlength) continue;
			if ( ! candidate.toLowerCase().startsWith(start)) continue;
			
			// The closer to zero the better
			int lendiff = candidate.length() - minlength;
			if (lendiff == 0)
			{
				return candidate;
			}
			if (lendiff < best || best == 0)
			{
				best = lendiff;
				ret = candidate;
			}
		}
		return ret;
	}
}
