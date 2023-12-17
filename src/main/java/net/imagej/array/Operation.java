package net.imagej.array;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

public class Operation {
    
    public static final Array map(Array array,ToDoubleFunction<Double> function) {
        int[] shape = array.getShape();
        Double[] doubles = Arrays.stream(array.extractAll()).mapToDouble(function).boxed().toArray(Double[]::new);
        return Array.create(shape, doubles);
    }
    
    public static final Array plus(Array array, double d) {
        return map(array, (a) -> a + d);
    }

    public static final Array minus(Array array, double d) {
        return map(array, (a) -> a - d);
    }

    public static final Array mult(Array array, double d) {
        return map(array, (a) -> a * d);
    }

    public static final Array plus(Array array0, Array array1) {
        Verification.equalsShape(array0, array1);
        Double[] doubles0 = array0.extractAll();
        Double[] doubles1 = array1.extractAll();
        Double[] doubles2 = IntStream.range(0, doubles0.length).mapToDouble(i -> doubles0[i] + doubles1[i]).boxed().toArray(Double[]::new);
        return Array.create(array0.getShape(), doubles2);
    }

    public static final Array minus(Array array0, Array array1) {
        Verification.equalsShape(array0, array1);
        Double[] doubles0 = array0.extractAll();
        Double[] doubles1 = array1.extractAll();
        Double[] doubles2 = IntStream.range(0, doubles0.length).mapToDouble(i -> doubles0[i] - doubles1[i]).boxed().toArray(Double[]::new);
        return Array.create(array0.getShape(), doubles2);
    }

    public static final Array mult(Array array0, Array array1) {
        Verification.equalsShape(array0, array1);
        Double[] doubles0 = array0.extractAll();
        Double[] doubles1 = array1.extractAll();
        Double[] doubles2 = IntStream.range(0, doubles0.length).mapToDouble(i -> doubles0[i] * doubles1[i]).boxed().toArray(Double[]::new);
        return Array.create(array0.getShape(), doubles2);
    }

    public static final Double sum(Array array) {
        return Arrays.stream(array.extractAll()).reduce(0.0, (v, d) -> v + d);
    }

    public static final Double max(Array array) {
        Double[] doubles = array.extractAll();
        return Arrays.stream(doubles, 1, doubles.length).reduce(doubles[0], (v, d) -> Math.max(v, d));
    }

    public static final Double min(Array array) {
        Double[] doubles = array.extractAll();
        return Arrays.stream(doubles, 1, doubles.length).reduce(doubles[0], (v, d) -> Math.min(v, d));
    }

    public static final Array dot(Array array0, Array array1) {
        Verification.dot(array0, array1);
        if (array0.getDimension() > 2) {
            Array[] arrays = new Array[]{};
            for (int i = 0; i < array0.getLength(); i++) {
                arrays = ArrayUtils.add(arrays, dot((Array) array0.get(i), (Array) array1.get(i)));
            }
            return new Array(arrays);
        }
        Double[] doubles = new Double[]{};
        for (int i = 0; i < array0.getLength(); i++) {
            Array slice0 = array0.slice(new int[]{i, 0}, new int[]{i + 1, array0.getShape()[1]});
            for (int j = 0; j < ((Array) array1.get(0)).getLength(); j++) {
                Array slice1 = array1.slice(new int[]{0, j}, new int[]{array1.getShape()[0], j + 1});
                doubles = ArrayUtils.add(doubles, sum(mult(slice0, slice1.t())));
            }
        }
        return Array.create(new int[]{array0.getShape()[0], array1.getShape()[1]}, doubles);
    }
}
