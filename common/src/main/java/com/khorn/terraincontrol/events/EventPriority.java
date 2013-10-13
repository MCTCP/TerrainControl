package com.khorn.terraincontrol.events;

public enum EventPriority
{
    /**
     * Register with this priority if you want to cancel the event.
     */
    CANCELABLE,

    /**
     * Register with this prioriyt if you want to monitor the event. You cannot
     * cancel the event during this stage.
     */
    MONITOR
}
