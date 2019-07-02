package com.pg85.otg.bukkit.metrics;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a custom graph on the website
 */
public class Graph
{

    /**
     * The graph's name, alphanumeric and spaces only :) If it does not comply to the above when submitted, it is
     * rejected
     */
    private final String name;

    /**
     * The set of plotters that are contained within this graph
     */
    private final Set<Plotter> plotters = new LinkedHashSet<Plotter>();

    Graph(final String name)
    {
        this.name = name;
    }

    /**
     * Gets the graph's name
     *
     * @return the Graph's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Add a plotter to the graph, which will be used to plot entries
     *
     * @param plotter the plotter to add to the graph
     */
    public void addPlotter(final Plotter plotter)
    {
        plotters.add(plotter);
    }

    /**
     * Remove a plotter from the graph
     *
     * @param plotter the plotter to remove from the graph
     */
    public void removePlotter(final Plotter plotter)
    {
        plotters.remove(plotter);
    }

    /**
     * Gets an <b>unmodifiable</b> set of the plotter objects in the graph
     *
     * @return an unmodifiable {@link java.util.Set} of the plotter objects
     */
    public Set<Plotter> getPlotters()
    {
        return Collections.unmodifiableSet(plotters);
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object object)
    {
        if (!(object instanceof Graph))
        {
            return false;
        }

        final Graph graph = (Graph) object;
        return graph.name.equals(name);
    }

    /**
     * Called when the server owner decides to opt-out of BukkitMetrics while the server is running.
     */
    protected void onOptOut()
    {
    }
}