package com.khorn.terraincontrol.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import org.junit.Test;

public class ReportTest
{

    @Test(expected = Report.class)
    public void basics()
    {
        RuntimeException cause = new RuntimeException();

        throw Report.of(cause).with("test", "ok");
    }

    @Test
    public void message()
    {
        RuntimeException cause = new RuntimeException();

        Report report = Report.of(cause).with("test", "ok");
        assertEquals("An internal error in " + PluginStandardValues.PLUGIN_NAME + " occured.\ntest: ok",
                report.getMessage());
    }

    @Test
    public void chaining()
    {
        RuntimeException original = new RuntimeException();
        Report earlyReport = Report.of(original);
        earlyReport.with("Test", "Ok");
        Report finalReport = Report.of(earlyReport);
        finalReport.with("Test2", "Ok");

        // Check if extra report layer does not distract from the cause
        assertEquals(original, finalReport.getCause());

        // Check if no details are lost
        assertTrue(finalReport.getMessage().contains("Test: Ok"));
        assertTrue(finalReport.getMessage().contains("Test: Ok"));
    }

    @Test(expected = OutOfMemoryError.class)
    public void outOfMemoryIsNotWrapped()
    {
        OutOfMemoryError e = new OutOfMemoryError();
        throw Report.of(e);
    }

    @Test
    public void nullIsGrudinglyAccepted()
    {
        Report report = Report.of(null).with("foo", "bar");

        // Check if additional details are still preserved
        assertTrue(report.getMessage().contains("foo: bar"));
    }

    @Test
    public void at()
    {
        Report report = Report.of(new RuntimeException()).at("something", null, 4, 6, 7);
        assertTrue(report.getMessage().contains("4, 6, 7"));
    }
}
