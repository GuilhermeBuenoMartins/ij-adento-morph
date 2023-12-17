package net.imagej.nn;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import net.imagej.array.Array;
import net.imagej.array.Operation;

public class Function {

    public static final Array relu(Array array) {
        return Operation.map(array, a -> Math.max(a, 0));
    }

    public static final Array sigmoid(Array array) {
        return Operation.map(array, a -> 1 / (1 + Math.exp(-a)));
    }

    public static final Array tanh(Array array) {
        return Operation.map(array, a -> (Math.exp(a) - Math.exp(-a)) / (Math.exp(a) + Math.exp(-a)));
    }

    public static final int[] outputShape(int[] inputShape, int[] kernelShape, int[] stride) {
        int depth = kernelShape[kernelShape.length - 1];
        int width = Math.floorDiv(inputShape[inputShape.length - 2] - kernelShape[kernelShape.length - 3],
                stride[stride.length - 1]) + 1;
        int height = Math.floorDiv(inputShape[inputShape.length - 3] - kernelShape[kernelShape.length - 4],
                stride[stride.length - 2]) + 1;
        return new int[] { height, width, depth };
    }

    public static final Array getWindow(Array input, int[] from, int[] shape) {
        int[] to = IntStream.range(0, from.length).map(i -> from[i] + shape[i]).toArray();
        return input.slice(from, to);
    }

    public static final Array getMorphKernel(Array kernel, int filter) {
        int[] kernelShape = kernel.getShape();
        int[] from = ArrayUtils.add(Arrays.stream(kernelShape, 0, kernelShape.length - 1).map(i -> 0).toArray(),
                filter);
        int[] to = ArrayUtils.add(Arrays.stream(kernelShape, 0, kernelShape.length - 1).toArray(), filter + 1);
        int[] shape = ArrayUtils.addAll(new int[] { 1 },
                Arrays.stream(kernelShape, 0, kernelShape.length - 1).toArray());
        return kernel.slice(from, to).reshape(shape);
    }

    public static final Double[] dilation(Array window, Array morphKernel) {
        Double[] doubles = new Double[] {};
        Array sum = Operation.plus(window, morphKernel);
        int[] sumShape = sum.getShape();
        // Iterate channel
        for (int i = 0; i < sumShape[sumShape.length - 1]; i++) {
            int[] from = ArrayUtils.add(IntStream.range(0, sumShape.length - 1).map(j -> 0).toArray(), i);
            int[] to = ArrayUtils.add(Arrays.stream(sumShape, 0, sumShape.length - 1).toArray(), i + 1);
            doubles = ArrayUtils.add(doubles, Operation.max(sum.slice(from, to)));
        }
        return doubles;
    }

    public static final Double[] erosion(Array window, Array morphKernel) {
        Double[] doubles = new Double[] {};
        Array sum = Operation.minus(window, morphKernel.reverse(2).reverse(3));
        int[] sumShape = sum.getShape();
        // Iterate channel
        for (int i = 0; i < sumShape[sumShape.length - 1]; i++) {
            int[] from = ArrayUtils.add(IntStream.range(0, sumShape.length - 1).map(j -> 0).toArray(), i);
            int[] to = ArrayUtils.add(Arrays.stream(sumShape, 0, sumShape.length - 1).toArray(), i + 1);
            doubles = ArrayUtils.add(doubles, Operation.min(sum.slice(from, to)));
        }
        return doubles;
    }

    public static final Array padding(Array input, int[] kernelShape, Double padDouble) {
        int[] inputShape = input.getShape();
        int[] pad = new int[] { kernelShape[kernelShape.length - 4] - 1, kernelShape[kernelShape.length - 3] - 1};
        int[] padShape = new int[] { inputShape[inputShape.length - 4],
                inputShape[inputShape.length - 3] + pad[0],
                inputShape[inputShape.length - 2] + pad[1],
                inputShape[inputShape.length - 1] };
        Array padInput = Operation.plus(Array.zeros(padShape), padDouble);
        for (int i = 0; i < inputShape[inputShape.length - 4]; i ++ ) {
            for (int j = 0; j < inputShape[inputShape.length - 3]; j++) {
                for (int k = 0; k < inputShape[inputShape.length - 2]; k++) {
                    ((Array) ((Array) padInput.get(i)).get(j + (int) pad[0] / 2)).set(((Array) ((Array) input.get(i)).get(j)).get(k), k + (int) pad[1] / 2);
                }
            }
        }
        return padInput;
    }
}
