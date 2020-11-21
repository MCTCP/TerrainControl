package com.pg85.otg.util;

public class Pair<F, S>
{
	private final F first;
	private final S second;

	private Pair(final F first, final S second)
	{
		this.first = first;
		this.second = second;
	}

	public F getFirst()
	{
		return first;
	}

	public S getSecond()
	{
		return second;
	}

	public static <F, S> Pair<F, S> of(final F first, final S second)
	{
		return new Pair<>(first, second);
	}
}
