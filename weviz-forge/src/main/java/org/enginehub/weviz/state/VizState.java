package org.enginehub.weviz.state;

import java.util.OptionalDouble;

import com.google.auto.value.AutoValue;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class VizState {

    public static Builder builder() {
        return new AutoValue_VizState.Builder()
            .points(ImmutableList.of())
            .points2d(ImmutableList.of());
    }

    private static <T> ImmutableList<T> insertImmutable(ImmutableList<T> source, int index, T object, T zero) {
        return ImmutableList.copyOf(new AbstractIterator<T>() {

            private final int sourceSize = source.size();
            private int cursor;

            @Override
            protected T computeNext() {
                if (cursor == index) {
                    // emit the replacement object
                    cursor++;
                    return object;
                }
                if (cursor < sourceSize) {
                    // emitting elements from the list
                    T elem = source.get(cursor);
                    cursor++;
                    return elem;
                }
                if (cursor < index) {
                    // emitting filler elements
                    cursor++;
                    return zero;
                }
                // filled
                return endOfData();
            }
        });
    }

    @AutoValue.Builder
    public interface Builder {

        Builder shape(Shape shape);

        Builder points(ImmutableList<Vector3> points);

        ImmutableList<Vector3> getPoints();

        default Builder addPoint(int index, Vector3 point) {
            return points(insertImmutable(
                getPoints(), index, point, Vector3.ZERO
            ));
        }

        Builder points2d(ImmutableList<Vector2> points2d);

        ImmutableList<Vector2> getPoints2d();

        default Builder addPoint2d(int index, Vector2 point) {
            return points2d(insertImmutable(
                getPoints2d(), index, point, Vector2.ZERO
            ));
        }

        ImmutableList.Builder<DoubleList> polygonsBuilder();

        default Builder addPolygon(DoubleList intList) {
            polygonsBuilder().add(intList);
            return this;
        }

        Builder ellipsoidCenter(Vector3 vector);

        Builder ellipsoidRadii(Vector3 vector);

        Builder cylinderCenter(Vector3 vector);

        Builder cylinderRadii(Vector2 vector);

        Builder min(double min);

        Builder max(double max);

        VizState build();

    }

    VizState() {
    }

    public abstract Shape getShape();

    public abstract ImmutableList<Vector3> getPoints();

    public abstract ImmutableList<Vector2> getPoints2d();

    public abstract ImmutableList<DoubleList> getPolygons();

    public abstract @Nullable Vector3 getEllipsoidCenter();

    public abstract @Nullable Vector3 getEllipsoidRadii();

    public abstract @Nullable Vector3 getCylinderCenter();

    public abstract @Nullable Vector2 getCylinderRadii();

    public abstract OptionalDouble getMin();

    public abstract OptionalDouble getMax();

    public abstract Builder toBuilder();

}
