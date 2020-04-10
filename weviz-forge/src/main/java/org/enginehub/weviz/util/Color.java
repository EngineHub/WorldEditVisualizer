package org.enginehub.weviz.util;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Color {

    private static final double FACTOR = 0.8;

    public static Color of(int r, int g, int b, int a) {
        return new AutoValue_Color(r, g, b, a);
    }

    Color() {
    }

    public abstract int getRed();

    public abstract int getGreen();

    public abstract int getBlue();

    public abstract int getAlpha();

    public final Color darker() {
        return of(Math.max((int) (getRed() * FACTOR), 0),
            Math.max((int) (getGreen() * FACTOR), 0),
            Math.max((int) (getBlue() * FACTOR), 0),
            getAlpha());
    }

}
