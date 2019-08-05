package com.pg85.otg.bukkit.metrics;

/**
 * Interface used to collect custom data for a plugin
 */
public abstract class Plotter
{

    /**
     * The plot's name
     */
    private final String name;

    /**
     * Construct a plotter with the default plot name
     */
    public Plotter()
    {
        this("Default");
    }

    /**
     * Construct a plotter with a specific plot name
     *
     * @param name the name of the plotter to use, which will show up on the website
     */
    Plotter(final String name)
    {
        this.name = name;
    }

    /**
     * Get the current value for the plotted point. Since this function defers to an external function it may or may
     * not return immediately thus cannot be guaranteed to be thread friendly or safe. This function can be called
     * from any thread so care should be taken when accessing resources that need to be synchronized.
     *
     * @return the current value for the point to be plotted.
     */
    public abstract int getValue();

    /**
     * Get the column name for the plotted point
     *
     * @return the plotted point's column name
     */
    public String getColumnName()
    {
        return name;
    }

    /**
     * Called after the website graphs have been updated
     */
    void reset()
    {
    }

    @Override
    public int hashCode()
    {
        return getColumnName().hashCode();
    }

    @Override
    public boolean equals(final Object object)
    {
        if (!(object instanceof Plotter))
        {
            return false;
        }

        final Plotter plotter = (Plotter) object;
        return plotter.name.equals(name) && plotter.getValue() == getValue();
    }
}