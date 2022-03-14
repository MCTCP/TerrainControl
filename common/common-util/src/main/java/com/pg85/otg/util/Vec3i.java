package com.pg85.otg.util;

public record Vec3i(int x, int y, int z) {
    public Vec3i add(int x, int y, int z) {
        return new Vec3i(this.x + x, this.y + y, this.z + z);
    }
}
