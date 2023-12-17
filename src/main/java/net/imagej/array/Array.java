package net.imagej.array;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import org.apache.commons.lang3.ArrayUtils;

public class Array {

    private Object[] array;

    private int dimension;

    public static final Array zeros(int... shape) {
        if (shape.length == 1) {
            return new Array(DoubleStream.generate(() -> 0.0).limit(shape[0]).boxed().toArray(Double[]::new));
        }
        Array[] array = new Array[] {};
        for (int i = 0; i < shape[0]; i++) {
            array = ArrayUtils.add(array, zeros(Arrays.copyOfRange(shape, 1, shape.length)));
        }
        return new Array(array);
    }

    public static Array create(int[] shape, Double... doubles) {
        Verification.shape(shape, doubles);
        if (shape.length == 1) {
            return new Array(doubles);
        }
        int interval = doubles.length / shape[0];
        Array[] array = new Array[] {};
        for (int i = 0; i < shape[0]; i++) {
            int from = i * interval;
            int to = from + interval;
            array = ArrayUtils.add(array,
                    create(Arrays.copyOfRange(shape, 1, shape.length), Arrays.copyOfRange(doubles, from, to)));
        }
        return new Array(array);
    }

    public Array(Array... arrays) {
        Verification.length(arrays);
        Verification.dimension(arrays);
        this.array = arrays;
        dimension = arrays[0].getDimension() + 1;
    }

    public Array(Double... doubles) {
        array = doubles;
        dimension = 1;
    }

    public final int getLength() {
        return array.length;
    }

    public final int getDimension() {
        return dimension;
    }

    public final String asString() {
        if (dimension == 1) {
            return Arrays.toString((Double[]) array);
        }
        StringBuilder builder = new StringBuilder("[");
        Arrays.stream(array).forEach(a -> builder.append(((Array) a).asString()));
        builder.append("]");
        return builder.toString();
    }

    public final int[] getShape() {
        if (dimension == 1) {
            return new int[] { getLength() };
        }
        return ArrayUtils.insert(0, ((Array) array[0]).getShape(), getLength());
    }

    public final Object get(int i) {
        return array[i];
    }

    public final Object get(int... i) {
        Verification.multiIndex(dimension, i);
        if (dimension == 1) {
            return array[i[0]];
        }
        return ((Array) array[i[0]]).get(Arrays.copyOfRange(i, 1, i.length));
    }

    public final void set(Object object, int i) {
        if (dimension != 1) {
            Array array0 = (Array) object;
            Verification.equalsShape((Array) array[i], array0);
            array[i] = array0;
        } else {
            array[i] = (Double) object;
        }
    }

    public final void set(Double d, int... i) {
        Verification.multiIndex(dimension, i);
        if (dimension == 1) {
            array[i[0]] = d;
        } else {
            ((Array) array[i[0]]).set(d, Arrays.copyOfRange(i, 1, i.length));
        }
    }

    public final Array slice(int[] from, int[] to) {
        Verification.multiIndex(dimension, from);
        Verification.multiIndex(dimension, to);
        if (dimension == 1) {
            return new Array(Arrays.copyOfRange((Double[]) array, from[0], to[0]));
        }
        Array[] arrays = new Array[] {};
        for (int i = from[0]; i < to[0]; i++) {
            arrays = ArrayUtils.add(arrays, ((Array) array[i]).slice(Arrays.copyOfRange(from, 1, from.length),
                    Arrays.copyOfRange(to, 1, to.length)));
        }
        return new Array(arrays);
    }

    public final Double[] extractAll() {
        if (dimension == 1) {
            return Arrays.stream(array).map(a -> (Double) a).toArray(Double[]::new);
        }
        Double[] doubles = new Double[] {};
        for (int i = 0; i < getLength(); i++) {
            doubles = ArrayUtils.addAll(doubles, ((Array) array[i]).extractAll());
        }
        return doubles;
    }

    public final Array reshape(int... shape) {
        Double[] doubles = extractAll();
        if (shape.length == 1) {
            array = doubles;
        } else {
            array = new Object[] {};
            for (int i = 0; i < shape[0]; i++) {
                int from = i * doubles.length / shape[0];
                int to = (i + 1) * doubles.length / shape[0];
                array = ArrayUtils.add(array,
                        create(Arrays.copyOfRange(shape, 1, shape.length), Arrays.copyOfRange(doubles, from, to)));
            }
        }
        dimension = shape.length;
        return this;
    }

    public final Array reverse(){
        if (dimension == 1) {
            Double[] doubles = Arrays.copyOf(Arrays.stream(array).map(d -> (Double) d).toArray(Double[]::new), getLength());
            ArrayUtils.reverse(doubles);
            return new Array(doubles);
        }
        Array[] arrays = Arrays.copyOf(Arrays.stream(array).map(a -> (Array) a).toArray(Array[]::new), getLength());
        ArrayUtils.reverse(arrays);
        return new Array(arrays);
    }

    public final Array reverse(int dimension) {
        if (dimension == this.dimension) {
            return reverse();
        }
        Array[] arrays = new Array[]{};
        for (int i = 0; i < getLength(); i++) {
            arrays = ArrayUtils.add(arrays, ((Array) array[i]).reverse(dimension));
        }
        return new Array(arrays);
    }

    public final Array concat(Array array) {
        if (dimension == 1) {
            Double[] doubles = Arrays.copyOf(Arrays.stream(this.array).map(a -> (Double) a).toArray(Double[]::new), getLength());
            for (int i = 0; i < array.getLength(); i ++) {
            doubles = ArrayUtils.add(doubles, (Double) array.get(i));
            }
            return new Array(doubles);
        }
        Verification.equalsShape((Array) this.array[0], (Array) array.get(0));
        Array[] arrays = Arrays.copyOf(Arrays.stream(this.array).map(a -> (Array) a).toArray(Array[]::new), getLength());
        for (int i = 0; i < array.getLength(); i ++) {
            arrays = ArrayUtils.add(arrays, (Array) array.get(i));
        }
        return new Array(arrays);
    }

    public final Array concat(Array array, int dimension) {
        if (this.dimension == dimension || this.dimension == 1) {
            return concat(array);
        }
        Verification.equalsShape((Array) this.array[0], (Array) array.get(0));
        Array[] arrays = new Array[getLength()];
        for (int i = 0; i < getLength(); i ++) {
            arrays[i] = ((Array) this.array[i]).concat((Array) array.get(i), dimension);
        }
        return new Array(arrays);
    }

    public final Array t() {
        Verification.moreThan(this, 1);
        Array[] array = new Array[] {};
        if (dimension == 2) {
            int[] shape = getShape();
            for (int i = 0; i < shape[1]; i++) {
                Array slice = slice(new int[] { 0, i }, new int[] { getLength(), i + 1 });
                array = ArrayUtils.addAll(array, slice.reshape(shape[0]));
            }
            return new Array(array);
        }
        array = Arrays.stream(this.array).map(a -> ((Array) a).t()).toArray(Array[]::new);
        return new Array(array);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new Array(Arrays.stream(array).map(a -> (Array) a).toArray(Array[]::new));
    }

    @Override
    public String toString() {
        return "{shape=" + Arrays.toString(getShape()) + ", array=" + asString() + "}";
    }
}
