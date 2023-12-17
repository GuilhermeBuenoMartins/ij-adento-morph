package net.imagej.array;

import java.util.Arrays;

public class Verification {


    protected final static void shape(int[] shape, Double[] doubles) {
        if (Arrays.stream(shape).reduce(1, (v, s) -> v * s) != doubles.length) {
            throw new IllegalArgumentException("The shape is invalid for quantity of elements.");
        }
    }

    protected final static void length(Array[] arrays) {
        if (arrays.length > 1) {
            if (!Arrays.stream(arrays, 1, arrays.length).allMatch(a -> a.getLength() == arrays[0].getLength())) {
                throw new IllegalArgumentException("The length of arrays are not equals.");
            }
        }
    }

    protected final static void dimension(Array[] arrays) {
        if (arrays.length > 1) {
            if (!Arrays.stream(arrays, 1, arrays.length).allMatch(a -> a.getDimension() == arrays[0].getDimension())) {
                throw new IllegalArgumentException("The dimensions of arrays are not equals.");
            }
        }
    }

    protected final static void multiIndex(int dimension, int... i) {
        if (dimension != i.length) {
            throw new IndexOutOfBoundsException("The number of dimension inferior to quantity of index.");
        }
    }

    protected final static void equalsShape(Array array0, Array array1) {
        if (array0.getShape().equals(array1.getShape())) {
            throw new IllegalArgumentException("Operation element-wise is not allow. Arrays have different shapes.");
        }
    }

    protected final static void moreThan(Array array, int dimensions) {
        if (array.getDimension() <= dimensions) {
            throw new IllegalArgumentException(String.format("Array does not have more than %d dimension to do this operation.", dimensions));
        }
    }

    protected final static void dot(Array array0, Array array1) {
        if (array0.getDimension() != array1.getDimension()) {
            throw new IllegalArgumentException("The arrays` dimensions does not allow matrices multiplication.");
        }
        if (array0.getShape()[array0.getShape().length - 1] != array1.getShape()[array1.getShape().length - 2]) {
            throw new IllegalArgumentException("The arrays` shapes does not allow matrices multiplication.");
        }
    }
}