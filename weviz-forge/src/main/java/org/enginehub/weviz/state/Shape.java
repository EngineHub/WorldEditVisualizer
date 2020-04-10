package org.enginehub.weviz.state;

import java.util.Locale;

import org.checkerframework.checker.nullness.qual.Nullable;

public enum Shape {
    CUBOID,
    ELLIPSOID,
    CYLINDER,
    POLYHEDRON,
    POLYGON2D;

    public static @Nullable Shape getById(String id) {
        for (Shape value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        return null;
    }

    private final String id = name().toLowerCase(Locale.ROOT);
}
