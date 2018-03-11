package com.khorn.terraincontrol.forge.asm.mixin.iface;

import com.khorn.terraincontrol.LocalWorld;

public interface IMixinWorld {

    void setTCWorld(LocalWorld world);

    LocalWorld getTCWorld();
}
