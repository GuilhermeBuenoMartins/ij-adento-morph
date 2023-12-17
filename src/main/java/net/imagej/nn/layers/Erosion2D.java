package net.imagej.nn.layers;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import net.imagej.array.Array;
import net.imagej.nn.enums.Activation;
import net.imagej.nn.Function;
import net.imagej.nn.Layer;
import net.imagej.nn.enums.Padding;

public class Erosion2D implements Layer{
    
    private Array kernel;

    private Activation activation = Activation.NONE;

    private Padding padding = Padding.VALID;

    public Erosion2D(Array kernel, Activation activation, Padding padding) {
        this.kernel = kernel;
        this.activation = activation;
        this.padding = padding;
    }

    public Erosion2D(Array kernel, Activation activation) {
        this.kernel = kernel;
        this.activation = activation;
    }

    public Erosion2D(Array kernel) {
        this.kernel = kernel;
    }

    private Array activate(Array input) {
        switch (activation) {
            case RELU: return Function.relu(input);
            case SIGMOID: return Function.sigmoid(input);
            case TANH: return Function.tanh(input);
            default: return input;
        }
    }
    
    @Override
    public void loadWeights(Array[] arrays) {
        kernel = arrays[0];
    }

    @Override
    public Array execute(Array input) {
        int[] kernelShape = kernel.getShape();
        input = padding.equals(Padding.SAME)? Function.padding(input, kernelShape, Double.POSITIVE_INFINITY): input;
        int[] inputShape = input.getShape();
        int[] morphKernelShape = ArrayUtils.addAll(new int[] {1}, Arrays.stream(kernelShape, 0, kernelShape.length - 1).toArray());
        int[] outputShape = Function.outputShape(inputShape, kernel.getShape(), new int[] {1, 1});
        Double[] doubles = new Double[]{};
        // Iterate samples
        for (int i = 0; i < input.getLength(); i++) { 
            // Iterate rows
            for (int j = 0; j < (inputShape[1] - kernelShape[0] + 1); j++) {
                // Iterate columns
                for (int k = 0; k < (inputShape[2] - kernelShape[1] + 1); k++) { 
                    // Iterate kernels
                    Array window = Function.getWindow(input, new int[]{i, j, k, 0}, morphKernelShape);
                    for (int l = 0; l < kernelShape[3]; l++) {
                        Array morphKernel = Function.getMorphKernel(kernel, l);
                        doubles = ArrayUtils.add(doubles, Arrays.stream(Function.erosion(window, morphKernel)).reduce(0.0, (r, d) -> r + d));
                    }
                }
            }
        }
        return activate(Array.create(ArrayUtils.addAll(new int[] {inputShape[0]}, outputShape), doubles));
    }
}
